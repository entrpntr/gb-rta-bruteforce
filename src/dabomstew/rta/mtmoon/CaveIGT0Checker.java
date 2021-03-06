package dabomstew.rta.mtmoon;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import mrwint.gbtasgen.Gb;
import dabomstew.rta.RedBlueAddr;
import dabomstew.rta.FileFunctions;
import dabomstew.rta.Func;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.Position;

public class CaveIGT0Checker {

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    public static final int RESET = 0x800;
    public static final int numThreads = 1;

    public static final int maxSecond = 1;

    public static PrintStream ps;

    public static void main(String[] args) throws IOException {
        // Mt.Moonius
        Gb.loadGambatte(numThreads);



//        int[][] segmentPaths =
//                {{64,64,64,64,64,64,64,64,32,32,32,32,32,32,64,64,64,64},{64,64,16,16,64},{128,128,128,128,128,128,128,128,128,129,128,128,16,16,16},{16,128,128,128,128,16,16,16},{128,16,128,128,128,128,16,16,16,16,16,16,16,16,16,16,16,128,128,128},{128,128,16,16,16,16,16,16,16,16,16,16,16,16}};
        int[][] segmentPaths =
                {{64,64,64,64,64,64,64,64,32,32,32,32,32,32,64,64,64,64},{64,64,16,16,64},{128,128,128,128,128,128,128,128,128,129,128,128,16,16,16},{16,128,128,128,128,16,16,16},{128,16,128,128,128,128,16,16,16,16,16,16,16,16,16,16,16,128,128,128},{128,128,16,16,16,16,16,16,16,16,16,16,16,16}};

        String path = "";

        for(int i=0;i<segmentPaths.length;i++) {
            //if(i == 0) { path += "LLLLLLL"; }
            for(int input : segmentPaths[i]) {
                if((input & 1) != 0) {
                    path += "A"+ Func.inputName(input & 0xFE);
                }
                else {
                    path += Func.inputName(input);
                }
            }
            if(i == 0) { path += "U"; }
            //if(i == 1) { path += "L"; }
            //if(i == 8) { path += "LRL"; }
        }
        path = path.replace(" ", "");
        System.out.println(path);

        final String finalPath = path;

        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/pcjack-cave.sav");
        baseSave[0x2CEF] = (byte) 2;
        baseSave[0x2CF0] = (byte) 0;
        baseSave[0x2CF1] = (byte) 0;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/pokeblue-pcjack.sav", baseSave);

        Gb[] gbs = new Gb[numThreads];
        GBMemory[] mems = new GBMemory[numThreads];
        GBWrapper[] wraps = new GBWrapper[numThreads];

        for (int i = 0; i < numThreads; i++) {
            gbs[i] = new Gb(i, false);
            gbs[i].startEmulator("testroms/pokeblue-pcjack.gbc");
            gbs[i].step(0); // let gambatte initialize itself
            mems[i] = new GBMemory(gbs[i]);
            wraps[i] = new GBWrapper(gbs[i], mems[i]);
        }

        // Advance through first part of intro
        // Always use fastest buffering method because there's no way
        // anything else is worth it
        wraps[0].advanceWithJoypadToAddress(UP, RedBlueAddr.biosReadKeypadAddr);
        wraps[0].advanceFrame(UP);
        int[] introInputs = { B | SELECT | UP, B | SELECT | UP, START };
        int introInputCtr = 0;
        while (introInputCtr < 3) {
            wraps[0].advanceToAddress(RedBlueAddr.joypadAddr);
            // inject intro inputs
            wraps[0].injectRBInput(introInputs[introInputCtr++]);
            wraps[0].advanceFrame();
        }

        // Advance to IGT inject point
        wraps[0].advanceToAddress(RedBlueAddr.igtInjectAddr);
        ByteBuffer state = gbs[0].saveState();

        // Setup for threading
        String[][] results = new String[60][60];
        boolean[][] successes = new boolean[60][60];
        boolean[] threadsRunning = new boolean[numThreads];

        // Deal with save
        long startAll = System.currentTimeMillis();
        for (int igtsec = 0; igtsec < maxSecond; igtsec++) {
            for (int igt0 = 0; igt0 < 60; igt0++) {
                boolean started = false;
                while (!started) {
                    synchronized (threadsRunning) {
                        int threadIdx = -1;
                        for (int i = 0; i < numThreads; i++) {
                            if (!threadsRunning[i]) {
                                threadIdx = i;
                                break;
                            }
                        }
                        if (threadIdx >= 0) {
                            started = true;
                            final int num = threadIdx;
                            final int sec = igtsec;
                            final int frm = igt0;
                            threadsRunning[threadIdx] = true;
                            Runnable run = new Runnable() {

                                @Override
                                public void run() {
                                    Gb gb = gbs[num];
                                    GBMemory mem = mems[num];
                                    GBWrapper wrap = wraps[num];
                                    String log = "S" + sec + " F" + frm;
                                    long startFrame = System.currentTimeMillis();
                                    gb.loadState(state);
                                    gb.writeMemory(0xda44, sec);
                                    gb.writeMemory(0xda45, frm);

                                    // skip the rest of the intro
                                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                    wrap.injectRBInput(A);
                                    wrap.advanceFrame();
                                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                    wrap.injectRBInput(A);
                                    wrap.advanceFrame();
                                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                    int pathIdx = 0;

                                    // Process shit
                                    boolean garbage = false;
                                    boolean success = false;
                                    boolean[] itemsPickedUp = new boolean[1];
                                    int itemIdx = 0;
                                    while (pathIdx < finalPath.length() && !garbage) {
                                        int input = inputFromChar(finalPath.charAt(pathIdx++));
                                        // Execute the action
                                        Position dest = getDestination(mem, input);
                                        wrap.injectRBInput(input);
                                        if(input == B) {
                                            wrap.advanceFrame();
                                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                        }
                                        else if(input == START) {
                                            wrap.advanceFrame();
                                            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                        } else {
                                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                                        }

                                        if (travellingToWarp(dest.map, dest.x, dest.y)) {
                                            wrap.advanceToAddress(RedBlueAddr.enterMapAddr);
                                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                        } else if(input != START && input != B){
                                            int result = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr,
                                                    RedBlueAddr.newBattleAddr);

                                            // Did we turnframe or hit an
                                            // ignored input frame after
                                            // a
                                            // warp?
                                            while (mem.getX() != dest.x || mem.getY() != dest.y) {
                                                if (result == RedBlueAddr.newBattleAddr) {
                                                    // Check for garbage
                                                    int result2 = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                                            RedBlueAddr.joypadOverworldAddr);

                                                    if (result2 == RedBlueAddr.encounterTestAddr) {
                                                        // Yes we can. What's up
                                                        // on this tile?
                                                        int hra = mem.getHRA();
                                                        // logLN("hrandom add was "+hra);
                                                        if (hra < 20 && mem.getMap() == 197) {
                                                            wrap.advanceFrame();
                                                            wrap.advanceFrame();
                                                            log += String
                                                                    .format("[F] Encounter at map %d x %d y %d Species %d Level %d DVs %04X [turnframe]",
                                                                            mem.getMap(), mem.getX(), mem.getY(),
                                                                            mem.getEncounterSpecies(),
                                                                            mem.getEncounterLevel(),
                                                                            mem.getEncounterDVs());
                                                            garbage = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                // Do that input again
                                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                                wrap.injectRBInput(input);
                                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                                                result = wrap.advanceToAddress(RedBlueAddr.newBattleAddr,
                                                        RedBlueAddr.joypadOverworldAddr);
                                            }
                                            // Can we get an encounter now?
                                            if (!garbage) {
                                                int result2 = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                                        RedBlueAddr.joypadOverworldAddr);

                                                if (result2 == RedBlueAddr.encounterTestAddr) {

                                                    // Yes we can. What's up on
                                                    // this tile?
                                                    int hra = mem.getHRA();
                                                    // logLN("hrandom add was "+hra);
                                                    if (hra < 20 && mem.getMap() == 197) {
                                                        wrap.advanceFrame();
                                                        wrap.advanceFrame();
                                                        // is this actually
                                                        // good?
                                                        log += String
                                                                .format("[%s] Encounter at map %d x %d y %d Species %d Level %d DVs %04X rdiv %04X hra %02X hrs %02X",
                                                                        success ? "S" : "F", mem.getMap(), mem.getX(),
                                                                        mem.getY(), mem.getEncounterSpecies(),
                                                                        mem.getEncounterLevel(), mem.getEncounterDVs(), gb.getDivState(), mem.getHRA(), mem.getHRS());
                                                        // trash state, throw it
                                                        // in the bin
                                                        // (encounter)
                                                        garbage = true;
                                                    }
                                                }
                                                if (!garbage) {
                                                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

                                                    // Pick up item?
                                                    if (timeToPickUpItem(mem.getMap(), mem.getX(), mem.getY(),
                                                            itemsPickedUp)) {
                                                        // Pick it up
                                                        wrap.injectRBInput(A);
                                                        wrap.advanceWithJoypadToAddress(A,
                                                                RedBlueAddr.textJingleCommandAddr);
                                                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                                        itemsPickedUp[itemIdx++] = true;
                                                    }
                                                }
                                            }

                                        }
                                    }

                                    if (!garbage) {
                                        log += String.format("[S] No encounter at map %d x %d y %d rdiv %04X hra %02X hrs %02X", mem.getMap(),
                                                mem.getX(), mem.getY(), gb.getDivState(), mem.getHRA(), mem.getHRS());
                                        success = true;
                                    }

                                    log += String.format(" (time=%dms)", System.currentTimeMillis() - startFrame);
                                    synchronized (threadsRunning) {
                                        results[sec][frm] = log;
                                        successes[sec][frm] = success;
                                        threadsRunning[num] = false;
                                    }
                                }
                            };
                            new Thread(run).start();
                        }
                    }
                    if (!started) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }

        // wait for all to be completed
        boolean done = false;
        while (!done) {
            synchronized (threadsRunning) {
                done = true;
                for (int i = 0; i < numThreads; i++) {
                    if (threadsRunning[i]) {
                        done = false;
                        break;
                    }
                }
            }
            if (!done) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        }

        // Print results
        int numSuccesses = 0;
        for (int sec = 0; sec < maxSecond; sec++) {
            logLN("Second " + sec);
            for (int frm = 0; frm < 60; frm++) {
                logLN(results[sec][frm]);
                numSuccesses += successes[sec][frm] ? 1 : 0;
            }
        }

        logF("%d worked found (time=%dms).\n", numSuccesses, System.currentTimeMillis() - startAll);
    }

    private static int inputFromChar(char charVal) {
        switch (charVal) {
            case 'U':
                return UP;
            case 'D':
                return DOWN;
            case 'L':
                return LEFT;
            case 'R':
                return RIGHT;
            case 'A':
                return A;
            case 'B':
                return B;
            case 'S':
                return START;
            default:
                return 0;
        }
    }

    private static boolean travellingToWarp(int map, int x, int y) {
        if (map == 13) {
            if (x == 12 && y == 9) {
                return true;
            }
        } else if (map == 46) {
            if(x == 4 && y == 4) {
                return true;
            }
        }
        return false;
    }

    public static boolean timeToPickUpItem(int map, int x, int y, boolean[] pickedUpItems) {
        return false;
    }

    public static Position getDestination(GBMemory mem, int input) {
        if (input == LEFT) {
            return new Position(mem.getMap(), mem.getX() - 1, mem.getY());
        } else if (input == RIGHT) {
            return new Position(mem.getMap(), mem.getX() + 1, mem.getY());
        } else if (input == UP) {
            return new Position(mem.getMap(), mem.getX(), mem.getY() - 1);
        } else if (input == DOWN) {
            return new Position(mem.getMap(), mem.getX(), mem.getY() + 1);
        } else {
            return new Position(mem.getMap(), mem.getX(), mem.getY());
        }
    }

    public static void logLN(String ln) {
        System.out.println(ln);
    }

    public static void logF(String format, Object... args) {
        System.out.printf(format, args);
    }

}
