package dabomstew.rta.trashcans;

import dabomstew.rta.*;
import dabomstew.rta.ffef.OverworldAction;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;

public class CansIGT0Checker {
    private static final int NO_INPUT = 0x00;

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static final int HARD_RESET = 0x800;

    // TODO: READ THIS STUFF FROM LOGS
    private static String gameName = "red";

    private static int x = 0xF;
    private static int y = 0x13;
    private static String path = "D A L L L A U R U U U U U A";
    private static GBWrapper wrap;
    private static GBMemory mem;

    public static void main(String[] args) throws IOException {
        if (!new File("testroms").exists()) {
            new File("testroms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("testroms/poke" + gameName + ".gbc").exists()) {
            System.err.println("Could not find poke" + gameName + ".gbc in testroms directory!");
            System.exit(0);
        }

        Gb.loadGambatte(1);

        String[] actions = path.split(" ");
        int successes = 60;
        for(int i=0; i<=59; i++) {
            makeSave(x, y, i);
            Gb gb = new Gb(0, false);
            gb.startEmulator("testroms/poke" + gameName + ".gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            // TODO: DON'T HARDCODE INTRO; ABSTRACT INTRO STRATS TO BE USABLE BY SEARCHER AND CHECKER
            //wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.biosReadKeypadAddr);
            //wrap.advanceFrame(UP);
            //wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.initAddr);
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(UP | SELECT | B);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(UP | SELECT | B);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
/*
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(B);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(B);
            wrap.advanceFrame();
*/
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(START);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
            //wrap.writeMemory(0xD2B5, 0x80);
            //wrap.writeMemory(0xD2B6, 0x50);
            //wrap.writeMemory(0xD158, 0x80);
            //wrap.writeMemory(0xD159, 0x50);
            //wrap.writeMemory(0xD31D, 0x04);
            //wrap.writeMemory(0xD320, 0x0D);
            //wrap.writeMemory(0xD322, 0x12);
            //wrap.writeMemory(0xD324, 0x09);
            //wrap.writeMemory(0xD325, 0x01);
            //wrap.writeMemory(0xD326, 0xFF);
            //wrap.writeMemory(0xD16D, 1);
            String f = (i < 10) ? " " + i : "" + i;
            System.out.print("[" + f + "] ");
            for(int j=0; j<actions.length; j++) {
                OverworldAction owAction = OverworldAction.fromString(actions[j]);
                boolean lastAction = j==actions.length-1;
                if(!execute(owAction, lastAction)) {
                    System.out.println(" - FAILURE");
                    successes--;
                    break;
                } else if(lastAction) {
                    System.out.println();
                }
            }
            //  gb.step(HARD_RESET);
        }
    }

    // TODO: ABSTRACT THIS OUT TO BE USABLE BY SEARCHER AND CHECKER
    private static boolean execute(OverworldAction owAction, boolean lastAction) {
        int res;
        System.out.print(owAction.logStr());
        switch(owAction) {
            case LEFT:
            case UP:
            case RIGHT:
            case DOWN:
                int input = 16 * (int) (Math.pow(2.0, (owAction.ordinal())));
                wrap.injectRBInput(input);
                Position dest = getDestination(mem, input);
                //System.out.println(dest.map + "#" + dest.x + "," + dest.y );
                if (travellingToWarp(dest.map, dest.x, dest.y)) {
                    wrap.advanceWithJoypadToAddress(input, RedBlueAddr.enterMapAddr);
                    //System.out.println("TEST 1");
                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    //System.out.println("TEST 2");
                } else {
                    int result = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr,
                            RedBlueAddr.newBattleAddr);
                    while (mem.getX() != dest.x || mem.getY() != dest.y) {
                        if (result == RedBlueAddr.newBattleAddr) {
                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                        }
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                        wrap.injectRBInput(input);
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                        result = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    }
                }
                //System.out.println("Test");
                //if(res == RedBlueAddr.newBattleAddr) {
                //    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr, RedBlueAddr.joypadOverworldAddr);
                //} else {
                //    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                //}
                //System.out.print("Test 2");
                return true;
            case A:
                wrap.injectRBInput(A);
                wrap.advanceFrame(A);
                res = wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadOverworldAddr, RedBlueAddr.printLetterDelayAddr);
                if (res == RedBlueAddr.joypadOverworldAddr) {
                    return true;
                } else {
                    //System.out.println("REACHED PRINTLETTERDELAY");
                    //return false;
                    System.out.print(", Cans: " + mem.getCans());
                    return true;
                }
            case START_B:
                //System.out.println(mem.getIGT());
                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(B);
                wrap.advanceFrame(B);
                wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadOverworldAddr);
                //System.out.println(mem.getIGT());
                return true;
            case S_A_B_S:
                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceToAddress(RedBlueAddr.joypadAddr);

                wrap.injectRBInput(A);
                wrap.advanceFrame(A);
                wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);

                wrap.injectRBInput(B);
                wrap.advanceFrame(B);
                wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);

                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadOverworldAddr);
                return true;
            case S_A_B_A_B_S:
                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(A);
                wrap.advanceFrame(A);
                wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(B);
                wrap.advanceFrame(B);
                wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(A);
                wrap.advanceFrame(A);
                wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(B);
                wrap.advanceFrame(B);
                wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadOverworldAddr);
                return true;
            default:
                return false;
        }
    }

    private static void makeSave(int x, int y, int igtFrames) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/cans_" + gameName + ".sav");
        int mapWidth = 20;
        int baseX = x;
        int baseY = y;
        int tlPointer = 0xC6E8 + (baseY / 2 + 1) * (mapWidth + 6) + (baseX / 2 + 1);
        baseSave[0x260B] = (byte) (tlPointer & 0xFF);
        baseSave[0x260C] = (byte) (tlPointer >> 8);
        baseSave[0x260D] = (byte) baseY;
        baseSave[0x260E] = (byte) baseX;
        baseSave[0x260F] = (byte) (baseY % 2);
        baseSave[0x2610] = (byte) (baseX % 2);
        baseSave[0x2CEF] = (byte) 45;
        baseSave[0x2CF0] = (byte) 0;
        baseSave[0x2CF1] = (byte) igtFrames;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/poke" + gameName + ".sav", baseSave);
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

    private static boolean travellingToWarp(int map, int x, int y) {
        if (map == 59) {
            if (x == 5 && y == 5) {
                return true;
            } else if (x == 17 && y == 11) {
                return true;
            }
        } else if (map == 60) {
            if (x == 25 && y == 9) {
                return true;
            } else if (x == 17 && y == 11) {
                return true;
            } else if (x == 21 && y == 17) {
                return true;
            }
        } else if(map == 5 && x == 0x0C && y == 0x13) {
            return true;
        }
        else {
            if (x == 25 && y == 9) {
                return true;
            }
        }
        return false;
    }
}
