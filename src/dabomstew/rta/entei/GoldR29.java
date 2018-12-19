package dabomstew.rta.entei;

import dabomstew.rta.GoldSilverAddr;
import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import mrwint.gbtasgen.Gb;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GoldR29 {
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static GscTileMap map = new GscTileMap();
    private static Gb gb;
    private static GBWrapper wrap;

    private static final String gameName;
    private static final int titleScreenAddr;

    static {
        /* Change this to "gold" or "silver" before running */
        gameName = "gold";

        if (gameName.equals("gold")) {
            titleScreenAddr = GoldSilverAddr.goldTitleScreenAddr;
        } else {
            titleScreenAddr = GoldSilverAddr.silverTitleScreenAddr;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int frame = 15;
        int passNum = 2;
        boolean igt60CreateStates = false;
        boolean searchFromStart = false;
        boolean searchFixedFrame = true;   // change at beginning if you want to search random frames

        String curSegment = "";
        //curSegment = "pass2-cherrygrove";
        curSegment = "pass2-mikey";
        //curSegment = "pass1-r29p3";
        //curSegment = "pass1-r30p1";

        // build out prospective paths here
        String segPath = "";
        segPath += "";

        //igt60(frame, passNum, curSegment, segPath, igt60CreateStates);
        search(frame, passNum, curSegment, searchFromStart, searchFixedFrame);
    }

    public static void search(int frame, int passNum, String curSegment, boolean fromStart, boolean fixedFrame) throws IOException {
        initGrid(passNum);

        PathState init = null;
        List<GscTile> endTiles = new ArrayList<>();

        if(fromStart) {
            init = new PathState(1, 0, map.get(0x1803, 56, 9), new ArrayList<>());   // **start

            // after r29/patch1
            //endTiles.add(map.get(0x1803, 38, 16));

            // after r29/patch2
            //endTiles.add(map.get(0x1803, 31, 6));
            //endTiles.add(map.get(0x1803, 31, 7));

            // before r29/patch3
            //endTiles.add(map.get(0x1803, 20, 4));

            // after r29/patch3
            //endTiles.add(map.get(0x1803, 9, 6));
            //endTiles.add(map.get(0x1803, 9, 7));

            // before cherrygrove/npc1
            endTiles.add(map.get(0x1A03, 33, 7));
        }

        else {
            if (curSegment.contains("cherrygrove")) {
                init = new PathState(0, 0, map.get(0x1A03, 33, 7), new ArrayList<>());   // **before cherrygrove/npc1
            }

            if (curSegment.contains("pass2-mikey")) {
                init = new PathState(0, 0, map.get(0x1A01, 12, 36), new ArrayList<>());  // **middle r30/patch2 (3rd to last grass tile, left)
            }

            //init = new PathState(0, 0, map.get(0x1803, 38, 16), new ArrayList<>());  // after  r29/patch1
            //init = new PathState(0, 0, map.get(0x1803, 20, 4), new ArrayList<>());   // before r29/patch3

            if (curSegment.contains("pass1-r29p3")) {
                init = new PathState(0, 0, map.get(0x1803, 9, 7), new ArrayList<>());    // after  r29/patch3 (bottom tile out of grass)
            }

            if (curSegment.contains("pass1-r30p1")) {
                init = new PathState(0, 0, map.get(0x1A01, 12, 46), new ArrayList<>());
            }
        }

        // after r30/patch1 (pass1)
        if(curSegment.contains("pass1-r29p3")) {
            endTiles.add(map.get(0x1A01, 12, 45));
            endTiles.add(map.get(0x1A01, 13, 45));
        }

        // after r30/patch2 (pass1)
        /*
        endTiles.add(map.get(0x1A01, 15, 20));
        endTiles.add(map.get(0x1A01, 15, 21));
        */

        // after r30/patch2 (pass2)
        /*
        if(curSegment.contains("pass2-cherrygrove")) {
            endTiles.add(map.get(0x1A01, 9, 34));
            endTiles.add(map.get(0x1A01, 9, 35));
            endTiles.add(map.get(0x1A01, 10, 33));
            endTiles.add(map.get(0x1A01, 11, 33));
            endTiles.add(map.get(0x1A01, 12, 33));
        }
        */

        if(curSegment.contains("pass2-cherrygrove")) {
            endTiles.add(map.get(0x1A01, 12, 36));
        }

        // pass1 end
        if(curSegment.contains("pass1")) {
            endTiles.add(map.get(0x1A01, 17, 11));
        }

        // pass2 end
        if(curSegment.contains("pass2")) {
            endTiles.add(map.get(0x1A01, 5, 24));
        }

        makeSave(0, passNum);
        Gb.loadGambatte(1);
        gb = new Gb(0, false);
        gb.startEmulator("roms/pokegold.gbc", false, 295);
        GBMemory mem = new GBMemory(gb);
        wrap = new GBWrapper(gb, mem);
        wrap.advanceWithJoypadToAddress(LEFT,0x100);

        if(fromStart) {
            wrap.advanceWithJoypadToAddress(START, titleScreenAddr);
            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(START);
            wrap.advanceFrame(START);
            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(A);
            wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(LEFT);
        }

        ByteBuffer state1 = gb.saveState();
        ArrayList<GscAction> actions;
        int ss=60;    // discard manips that average less than ss/60 on the frames searched (always search 60/60 now)

        int paths=0;
        int successes=0;
        int lrsucc;
        int selsucc;
        int goodpaths=0;

        int clusterSize=1;
        ByteBuffer[] state2 = null;
        int minDelay=0;
        int maxDelay=0;

        int delay=frame;

        if(fromStart) {
            state2 = new ByteBuffer[clusterSize];
        }

        if(!fixedFrame) {
            minDelay = 0;
            maxDelay = 16;
        }

        while(successes < ss*clusterSize || goodpaths < 10) {
            if(fromStart) {
                if(!fixedFrame) {
                    delay = ThreadLocalRandom.current().nextInt(minDelay, maxDelay - clusterSize + 2);
                }
                for (int dd0 = 0; dd0 < delay; dd0++) {
                    wrap.advanceFrame(LEFT);
                }
                for (int d = 0; d < clusterSize; d++) {
                    state2[d] = gb.saveState();
                    wrap.advanceFrame(LEFT);
                }
            }

            successes = 60*clusterSize;
            lrsucc = successes;
            selsucc = successes;
            actions = randomPath(init, endTiles, 0);
            String hRands = "";
            for (int i = 0; i < 60 && successes >= ss * clusterSize; i++) {
                for (int d = 0; d < clusterSize && successes >= ss * clusterSize; d++) {
                    int ret = GoldSilverAddr.owPlayerInputAddr;
                    if(fromStart) {
                        gb.loadState(state2[d]);
                        gb.writeMemory(0xD1EF, i);
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                        wrap.advanceFrame(A);
                        wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.owPlayerInputAddr);
                    }

                    else {
                        ByteBuffer state = loadByteBufferFromFile("states/" + gameName + "/" + curSegment + "/" + delay + "/" + i + ".gqs");
                        gb.loadState(state);
                    }

                    for (GscAction action : actions) {
                        ret = executeAction(ret, action);
                        if(ret != GoldSilverAddr.owPlayerInputAddr && !(ret == GoldSilverAddr.printLetterDelayAddr && gb.readMemory(0xDA03) == 5)) {
                            successes--;
                            break;
                        } else if(i==0 && gb.readMemory(0xDA02) == 36 && gb.readMemory(0xDA03) == 12) {
                            hRands = getRandom(gb);
                        }
                    }

                    if(ret == GoldSilverAddr.printLetterDelayAddr && gb.readMemory(0xDA03) == 5) {
                        doTextbox(4, A, true);
                        doTextbox(1, B, false);
                        wrap.injectGSMenuInput(A);
                        wrap.advanceFrame(A);
                        ByteBuffer battleState = gb.saveState();
                        wrap.advanceWithJoypadToAddress(SELECT, GoldSilverAddr.readJoypadAddr);
                        wrap.advanceFrame(SELECT);
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.doPlayerDamageAddr);
                        if(gb.readMemory(0xD100) != 0) {
                            selsucc--;
                        }
                        gb.loadState(battleState);
                        wrap.advanceWithJoypadToAddress(RIGHT, GoldSilverAddr.readJoypadAddr);
                        wrap.advanceFrame(RIGHT);
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.doPlayerDamageAddr);
                        if(gb.readMemory(0xD100) != 0) {
                            lrsucc--;
                        }
                    }

                    else if(i == 0 && hRands.equals("")) {
                        hRands = getRandom(gb);
                    }
                }
            }
            paths++;
            if(successes >= ss*clusterSize) {
                if(clusterSize>1) {
                    System.out.print("" + paths + ": [" + delay + "-" + (delay + clusterSize - 1) + "] ");
                }
                else {
                    System.out.print("" + paths + ": [" + delay + "] ");
                }
                for(GscAction action : actions) {
                    System.out.print(action.logStr() + " ");
                }
                System.out.println();
                System.out.print("  > Successes: " + successes + "/" + (60*clusterSize));
                if(gb.readMemory(0xDA03) == 5) {
                    System.out.print(", sel: " + selsucc + "/" + (60*clusterSize));
                    System.out.print(", l/r: " + lrsucc  + "/" + (60*clusterSize));
                } else {
                    System.out.print(" -- AddSub[f0]: " + hRands);
                }
                System.out.println();
                goodpaths++;
            }
            if(paths % 100 == 0) {
            //if(paths % 1000 == 0) {
                System.out.println("# paths tried: " + paths);
            }
            gb.loadState(state1);
        }
    }

    private static void igt60(int delay, int passNum, String curSegment, String segPath, boolean createStates) throws IOException {
        for (int i = 0; i < 60; i++) {
            System.out.println("IGT: " + i);
            System.out.println("--------");
            makeSave(i, passNum);
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokegold.gbc", false, 295);
            GBMemory mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);
            wrap.advanceWithJoypadToAddress(START, titleScreenAddr);
            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(START);
            wrap.advanceFrame(START);
            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(A);
            wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(LEFT);
            int clusterSize = 1;
            for (int k = 0; k < delay; k++) {
                wrap.advanceFrame(LEFT);
            }
            ByteBuffer state = gb.saveState();

            // put finalized paths here
            String path = "";
            if(delay <= 14) {
                path += "L L L L L A+L D D L L D L L D L L D D L D L L L L L ";
                path += "L L L L L U L L U U U R R U A+R R R U U U U L U L L L L L L L L L L L L L L U U L ";
                path += "L D L L A+D D L L L L A+L L L L ";
                path += "A+L L L L L L L L L L L L L L L L ";
                if(passNum == 1) {
                    path += "L U U L L L L U L L L L L L L A+L L L U U U U A+U U U U U U R R R R R R U U ";
                    path += "U U U U U U U U U U U U U U U U R R U U U U U U U U U U R R U U R U U U U L U U U ";
                }
                if(passNum == 2) {
                    path += "L U L A+L U L L U A+L L A+L L L L L L L L U U U U U U U U U R R R R R U U U U U U U U U U U U U ";
                    path += "U L L U U L U U U U L A+L U U A+U L L U U ";
                }
            }
            if(delay == 15 || delay == 18) {
                path += "L L L L L A+L D L L L L L L D D D D A+D L D L L L L L ";
                path += "L L L L L U L L U U U U U R R R R R U U U U L L L L L L L L L L L L L L L U U L ";
                path += "L L L L D D A+D L L L L L L L ";
                path += "A+L L L L L L L L L L L L L L L L ";
                if(passNum == 1) {
                    path += "L U U L L L L U A+L L L L L L L A+L L L U U U U U U U U U R R R R R R U U U ";
                    path += "U U U U U U U U U U U U U U U U R R U U U U U U U U U U R R U U R U U U U L U U U ";
                }
                if(passNum == 2) {
                    path += "L L L U U U L A+L L L L L L A+L L L L A+L U U U U A+U U U U U ";
                    path += "U R R R A+R R U U U U U U U U U U U U ";
                    path += "U U U L L L L U U U L U U L L U A+U U U ";
                }
            }
            if(delay == 16 || delay == 17) {
                path += "L L L L L A+L D D L L L D D L D D L A+L L L L L L L D ";
                path += "L L L L L U U L L U U U U R R R R R U U U U L L L L L L L L L L L L L L L U U L ";
                path += "L L D L L A+D D L A+L L A+L L L L ";
                path += "L L L L L L L A+L L L L L L L L L ";
                if(passNum == 1) {
                    path += "L U L L L U U L L L L L L L L L L L U U U U U U U U U R R R R R R U U U ";
                    path += "U U U U U U U U U R R U U U U U U U U U U U U U U U U U R U U R R U U U U L U U U ";
                }
                if(passNum == 2) {
                    path += "L U U U L L L L A+L L L L L L L L L L U U U U U U U U U R U R R R R U U U U U U U U U U U U ";
                    path += "U L U U U U U L L U U A+L L L U L U U U ";
                }
            }
            path += segPath;

            String[] actions = path.split(" ");
            int d = delay;
            for (; d < delay + clusterSize; d++) {
                wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                wrap.advanceFrame(A);
                wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.owPlayerInputAddr);
                //System.out.println("FFF0: " + gb.readMemory(0xFFF0));
                String log = "[" + d + "] ";
                int ret = GoldSilverAddr.owPlayerInputAddr;
                for (String action : actions) {
                    log += action + " ";
                    GscAction owAction = GscAction.fromString(action);
                    ret = executeAction(ret, owAction);
                    System.out.println("    - [" + d + "] " + gb.readMemory(0xD22E) + " " + gb.readMemory(0xD22D)
                            + " " + gb.readMemory(0xD22F) + ", " + gb.readMemory(0xD235)
                            + " " + gb.readMemory(0xD236));
                    if (ret != GoldSilverAddr.owPlayerInputAddr) {
                        break;
                    }
                }

                System.out.println(log);
                if (ret == GoldSilverAddr.owPlayerInputAddr || (gb.readMemory(0xDA03) == 5 && ret == GoldSilverAddr.printLetterDelayAddr)) {
                    System.out.println("  > SUCCESS!");
                    System.out.println("  > Cycles: " + gb.getCycleCount());
                    System.out.println("  > IGT   : " + gb.readMemory(0xD1ED) + " " + gb.readMemory(0xD1EE) + " " + gb.readMemory(0xD1EF));
                    System.out.println("  > Time  : " + gb.getEonTimer());
                    System.out.println("  > AddSub: " + getRandom(gb));
                    System.out.println("  > mm#x,y: " + gb.readMemory(0xDA00) + "|" + gb.readMemory(0xDA01) + "#" + gb.readMemory(0xDA03) + "," + gb.readMemory(0xDA02));
                    System.out.println("  > StepCt: " + gb.readMemory(0xD9BD));
                    ///*
                    if(passNum == 2 && gb.readMemory(0xDA03) == 5) {
                        // For potential Up+A or Down+A buffers:
                        // DA2C = 2B (Leer)
                        // DA2D = 0A (Scratch)
                        doTextbox(4, A, true);
                        doTextbox(1, B, false);
                        wrap.injectGSMenuInput(A);
                        wrap.advanceFrame(A);
                        ByteBuffer battleState = gb.saveState();
                        wrap.advanceWithJoypadToAddress(SELECT, GoldSilverAddr.readJoypadAddr);
                        wrap.advanceFrame(SELECT);
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.criticalCheckAddr);
                        System.out.println("hrs (sel crit): " + gb.readMemory(0xFFE4));
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.doPlayerDamageAddr);
                        System.out.println("(sel) HP: " + gb.readMemory(0xD100));
                        gb.loadState(battleState);
                        wrap.advanceWithJoypadToAddress(RIGHT, GoldSilverAddr.readJoypadAddr);
                        wrap.advanceFrame(RIGHT);
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.criticalCheckAddr);
                        System.out.println("hrs (l/r crit): " + gb.readMemory(0xFFE4));
                        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.doPlayerDamageAddr);
                        System.out.println("(l/r) HP: " + gb.readMemory(0xD100));
                    }
                    //*/
                    if(createStates) {
                        ByteBuffer bb = gb.saveState();
                        writeBytesToFile("states/" + gameName + "/" + curSegment + "/" + delay, "" + i + ".gqs", bb);
                    }
                } else {
                    System.out.println("  > FAILURE!");
                }
                gb.loadState(state);
                wrap.advanceFrame(LEFT);
                state = gb.saveState();
            }
            System.out.println();
        }
    }

    public static void makeSave(int igtFrame, int passNum) throws IOException {
        byte[] baseSave;
        if(passNum == 2) {
            baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/gold_r29_pass2.sav");
        } else {
            baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/gold_r29_pass1.sav");
        }

        //baseSave[0x2000] = (byte) 0xC1;

        baseSave[0x2056] = (byte) 0;        // igt second
        baseSave[0x2057] = (byte) igtFrame; // igt frame

        //baseSave[0x2825] = (byte) 0xC6; // StepCount
        //baseSave[0x2826] = (byte) 2;  // PSNStepCount

        baseSave[0x2045] = (byte) 9; // StartHour
        baseSave[0x2046] = (byte) 59; // StartMinute
        baseSave[0x2047] = (byte) 0;  // StartSecond

        baseSave[0x28A7] = (byte) 0xFD;
        baseSave[0x28A8] = (byte) 0xFF;

        if(passNum == 2) {
            baseSave[0x28B9] = (byte) 14;
            baseSave[0x28BB] = (byte) 14;
            baseSave[0x28BD] = (byte) 12;
            baseSave[0x28BF] = (byte) 12;
            baseSave[0x28C1] = (byte) 12;
        }

        int checksum = 0;
        for(int i = 0x2009; i < 0x2D69; i++) {
            checksum += (baseSave[i] & 0xFF);
        }
        baseSave[0x2D69] = (byte)((checksum) & 0xFF);
        baseSave[0x2D6A] = (byte)((checksum >> 8) & 0xFF);
        
        FileFunctions.writeBytesToFile("roms/pokegold.sav", baseSave);
    }

    private static int executeAction(int ret, GscAction action) {
        if(ret != GoldSilverAddr.owPlayerInputAddr || action == null) {
            return ret;
        }
        if(GscAction.isDpad(action)) {
            int input;
            if(action.logStr().startsWith("A")) {
                input = 1 | (16 * (int) (Math.pow(2.0, (action.ordinal()-8))));
            }
            else {
                input = 16 * (int) (Math.pow(2.0, (action.ordinal())));
            }
            wrap.injectGSInput(input);
            ret = wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.countStepAddr, GoldSilverAddr.startWildBattleAddr,
                    GoldSilverAddr.printLetterDelayAddr, GoldSilverAddr.bonkSoundAddr);

            if(ret == GoldSilverAddr.countStepAddr) {
                ret = wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.owPlayerInputAddr, GoldSilverAddr.startWildBattleAddr);
            }
        }
        return ret;
    }

    private static ArrayList<GscAction> randomPath(PathState state, List<GscTile> endTiles, int endEdgeset) {
        ArrayList<GscAction> curPath = new ArrayList<>(state.getCurPath());
        List<GscEdge> edges = state.getCurTile().getEdges().get(state.getEdgeSet());
        //System.out.println("" + state.getCurTile().getMap() + "#" + state.getCurTile().getX() + "," + state.getCurTile().getY());
        if(endTiles.contains(state.getCurTile()) && state.getEdgeSet() == endEdgeset) {
            return curPath;
        }
        boolean edgesContainsA = edges.stream().anyMatch(x -> (x.getAction().logStr().startsWith("A")));
        boolean removeAPresses = edgesContainsA && !state.canPressA();
        int high = (removeAPresses) ? edges.size() / 2 : edges.size();
        int randInt = ThreadLocalRandom.current().nextInt(high);
        GscEdge edge = removeAPresses ? edges.get(2*randInt) : edges.get(randInt);
        int newAPress;
        if(edge.getAction().logStr().startsWith("A") || state.getCurTile().getMap() != edge.getNextTile().getMap()) {
            newAPress = 1;
        } else {
            newAPress = 0;
        }
        curPath.add(edge.getAction());
        PathState newState = new PathState(newAPress, state.getEdgeSet(), edge.getNextTile(), curPath);
        return randomPath(newState, endTiles, endEdgeset);
    }

    // Maybe one day I'll be smart and make this generic / generated from ROM data
    private static void initGrid(int passNum) {
        for (int x = 0; x <= 56; x++) {
            for (int y = 4; y <= 16; y++) {
                GscCoord c = new GscCoord(0x1803, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x = 0; x<=39; x++) {
            for(int y=0; y<=8; y++) {
                GscCoord c = new GscCoord(0x1A03, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x = 4; x<=18; x++) {
            for(int y=0; y<=53; y++) {
                GscCoord c = new GscCoord(0x1A01, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x=38; x<=56; x++) {
            for (int y=9; y<=16; y++) {
                GscTile tile = map.get(0x1803, x, y);
                GscTile tileLeft = map.get(0x1803, x-1, y);
                GscTile tileDown = map.get(0x1803, x, y+1);
                if(!((x==44 && y<=13) || (x==51 && y==12) || (x==38 && y<16))) {
                    tile.addEdge(0, GscAction.LEFT, tileLeft);
                    //if(x!=55 || y!=9) {
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    //}
                }
                //if(!((x>53) || (y==16) || (y==13 && x>=48) || (y==15 && x>=44) || (x==50 && y==11))) {
                if(!((x>51) || (y==16) || (y==13 && x>=48) || (y==15 && x>=44) || (x==50 && y==11))) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    if(x!=55 || y!=9) {
                        tile.addEdge(0, GscAction.ADOWN, tileDown);
                    }
                }
            }
        }
        for(int x=21; x<=37; x++) {
            for(int y=4; y<=16; y++) {
                GscTile tile = map.get(0x1803, x, y);
                GscTile tileUp = map.get(0x1803, x, y-1);
                GscTile tileRight = map.get(0x1803, x+1, y);
                GscTile tileLeft = map.get(0x1803, x-1, y);
                if(y>=13) {
                    if(x>=32) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                    if(x<=33 && !(y==14 && x>=32)) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
                else if(y>=8) {
                    if(x<=35 && !(x==34 && y==12)) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                    if(!(x<=35 && y==10)) {
                        tile.addEdge(0, GscAction.UP,  tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
                else {
                    if(!((y==4) || (y==5 && x>=23) || (y==6 && x>=31))) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                    if(!((y==7 && x==23) || (x==21 && y>=5))) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                }
            }
        }
        for(int x=9; x<=20; x++) {
            for(int y=4; y<=7; y++) {
                GscTile tile = map.get(0x1803, x, y);
                GscTile tileLeft;
                if(x==9) {
                    tileLeft = map.get(0x1803, x-2, y);
                } else {
                    tileLeft = map.get(0x1803, x-1, y);
                }
                GscTile tileDown = map.get(0x1803, x, y+1);
                if(!((x==16 && y<=5))) {
                    tile.addEdge(0, GscAction.LEFT, tileLeft);
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                }
                if(!((x>=20) || (y==5 && x>=18) || (y==7))) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    tile.addEdge(0, GscAction.ADOWN, tileDown);
                }
            }
        }
        for(int x=0; x<=7; x++) {
            for(int y=6; y<=7; y++) {
                GscTile tile = map.get(0x1803, x, y);
                GscTile tileLeft;
                if(x == 0) {
                    tileLeft = map.get(0x1A03, 39, y);
                } else {
                    tileLeft = map.get(0x1803, x-1, y);
                }
                GscTile tileDown = map.get(0x1803, x, y+1);
                if(x != 7 || y != 6) {
                    tile.addEdge(0, GscAction.LEFT, tileLeft);
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                }
                if(y != 7) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    tile.addEdge(0, GscAction.ADOWN, tileDown);
                }
            }
        }
        for(int x=17; x<=39; x++) {
            for(int y=0; y<=7; y++) {
                GscTile tile = map.get(0x1A03, x, y);
                GscTile tileLeft = map.get(0x1A03, x-1, y);
                GscTile tileUp;
                if(y==0) {
                    tileUp = map.get(0x1A01, x-10, 53);
                } else {
                    tileUp = map.get(0x1A03, x, y-1);
                }
                GscTile tileDown = map.get(0x1A03, x, y+1);
                if(x>=33 && y==6) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    if(x!=33) {
                        tile.addEdge(0, GscAction.ADOWN, tileDown);
                    }
                }
                //if(!((x==33 && y==6) || (x==26 && y==7) || (x==20 && y==6) || x==17)) {
                if(!((x==33 && y==6) || (x==26 && y==7) || (x==20 && y==6) || x==17 || (x==25 && y>=5))) {
                    tile.addEdge(0, GscAction.LEFT, tileLeft);
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                }
                if(!((x>=32) || (y==4 && x>=18))) {
                    tile.addEdge(0, GscAction.UP, tileUp);
                    tile.addEdge(0, GscAction.AUP, tileUp);
                }
            }
        }

        if(passNum==1) {
            for (int x = 7; x <= 18; x++) {
                for (int y = 16; y <= 53; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileRight = map.get(0x1A01, x + 1, y);
                    if (!((x == 7 && y >= 50) || (x == 13 && y >= 38) || (x == 12 && y == 29) || (x == 15 && y >= 22) || (x == 17 && y >= 20) || (x == 18))) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                    if (!((y == 48 && x <= 11) || (x == 13 && y == 30) || (y == 28 && x <= 13) || (y == 20 && x <= 15) || (y == 18 && x <= 17))) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
            }
            for (int x = 17; x <= 18; x++) {
                for (int y = 12; y <= 15; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y-1);
                    GscTile tileLeft = map.get(0x1A01, x-1, y);
                    if(x != 17) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                    if(!(y == 14 && x >= 18)) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
            }
        }

        if(passNum==2) {
            for (int x = 7; x <= 12; x++) {
                for (int y = 36; y <= 53; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileRight = map.get(0x1A01, x + 1, y);
                    if (!((x == 7 && y >= 50) || (x == 12))) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                    if (!(y == 48 && x <= 11)) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
            }
            for (int x = 5; x <= 12; x++) {
                for (int y = 24; y <= 35; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileLeft = map.get(0x1A01, x - 1, y);
                    if (!((y == 30 && x >= 10) || (y == 28 && x >= 8) || (y == 26 && x >= 6))) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                    if (!((x==8 && y>=34) || (x == 7 && y >= 29) || x == 5)) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                }
            }
        }
    }

    private static String getRandom(Gb gb) {
        int hRandom = (gb.readMemory(0xFFE3) << 8) | gb.readMemory(0xFFE4);
        return String.format("0x%4s", Integer.toHexString(hRandom).toUpperCase()).replace(' ', '0');
    }

    private static void doTextbox(int num, int input, boolean clear) {
        for (int i = 0; i < num; i++) {
            boolean cont = true;
            while (cont) {
                wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.readJoypadAddr);
                int sp = gb.getRegisters()[1] + 8;
                int ret = gb.readMemory(sp) | (gb.readMemory(sp + 1) << 8);
                if (ret == 0x320D) {
                    wrap.advanceFrame(input);
                } else {
                    cont = false;
                }
            }
            if(clear) {
                wrap.injectGSMenuInput(A | B);
                wrap.advanceFrame(A | B);
                wrap.advanceFrame(input);
            }
        }
    }

    // The following functions were pillaged from Stringflow The Great
    public static void writeBytesToFile(String dir, String fileName, ByteBuffer buffer) throws IOException {
        File file = new File(dir);
        file.mkdirs();
        FileChannel channel = new FileOutputStream(dir + "/" + fileName).getChannel();
        channel.write(buffer);
        channel.close();
    }

    public static ByteBuffer loadByteBufferFromFile(String filename) throws IOException {
        byte[] byteArray = readBytesFromFile(filename);
        ByteBuffer res = ByteBuffer.allocateDirect(byteArray.length).order(ByteOrder.nativeOrder());
        for(int i = 0; i < byteArray.length; i++) {
            res.put(byteArray[i]);
        }
        return res;
    }

    private static byte[] readBytesFromFile(String fileName) throws IOException {
        File fh = new File(fileName);
        if(!fh.exists() || !fh.isFile() || !fh.canRead()) {
            throw new FileNotFoundException(fileName);
        }
        long fileSize = fh.length();
        if(fileSize > Integer.MAX_VALUE) {
            throw new IOException(fileName + " is too long to read in as a byte-array.");
        }
        FileInputStream fis = new FileInputStream(fileName);
        byte[] result = new byte[fis.available()];
        fis.read(result);
        fis.close();
        return result;
    }
}