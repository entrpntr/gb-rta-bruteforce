package dabomstew.rta.entei;

import dabomstew.rta.CrystalAddr;
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

public class R29 {
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

    public static void main(String[] args) throws IOException, InterruptedException {
        int gender = 0; // boy
        int frame = 10;
        int passNum = 2;
        boolean createStates = false;

        String curSegment = "";
        //curSegment = "pass1-r29end";
        //curSegment = "pass1-r30";
        //curSegment = "pass2-r29end";
        curSegment = "pass2-mikey";

        igt60(gender, frame, passNum, curSegment, createStates);
        //search(gender, frame, passNum, curSegment);
    }

    public static void search(int gender, int frame, int passNum, String curSegment) throws IOException {
        initGrid(passNum);

        PathState init;
        //init = new PathState(1, 0, map.get(0x1803, 56, 9), new ArrayList<>());   // start
        //init = new PathState(0, 0, map.get(0x1803, 38, 16), new ArrayList<>());  // after  r29/patch1
        //init = new PathState(0, 0, map.get(0x1803, 32, 7), new ArrayList<>());   // end of r29/patch2 (bottom left grass tile)
        //init = new PathState(0, 0, map.get(0x1803, 20, 4), new ArrayList<>());   // before r29/patch3
        //init = new PathState(0, 0, map.get(0x1803, 9, 7), new ArrayList<>());    // after  r29/patch3 (bottom tile out of grass)
        //init = new PathState(0, 0, map.get(0x1803, 10, 6), new ArrayList<>());   // end of r29/patch3 (top left grass tile)
        //init = new PathState(0, 0, map.get(0x1A01, 7, 49), new ArrayList<>());   // before r30/patch1 (bottom tile)
        init = new PathState(0, 0, map.get(0x1A01, 12, 36), new ArrayList<>());  // middle r30/patch2 (3rd to last grass tile, left)

        List<GscTile> endTiles = new ArrayList<>();

        // after r29/patch1
        //endTiles.add(map.get(0x1803, 38, 16));

        // after r29/patch2
        //endTiles.add(map.get(0x1803, 31, 6));
        //endTiles.add(map.get(0x1803, 31, 7));

        // before r29/patch 3
        //endTiles.add(map.get(0x1803, 20, 4));

        // after r29/patch3
        //endTiles.add(map.get(0x1803, 9, 6));
        //endTiles.add(map.get(0x1803, 9, 7));

        // after r30/patch1 (left tile only)
        //endTiles.add(map.get(0x1A01, 12, 45));

        // after r30/patch2 (pass1)
        /*
        endTiles.add(map.get(0x1A01, 12, 33));
        endTiles.add(map.get(0x1A01, 13, 33));
        endTiles.add(map.get(0x1A01, 14, 31));
        */

        // after r30/patch2 (pass2)
        /*
        endTiles.add(map.get(0x1A01, 9, 34));;
        endTiles.add(map.get(0x1A01, 9, 35));;
        endTiles.add(map.get(0x1A01, 10, 33));
        endTiles.add(map.get(0x1A01, 11, 33));
        endTiles.add(map.get(0x1A01, 12, 33));
        */

        // pass1 end
        //endTiles.add(map.get(0x1A01, 12, 13));
        //endTiles.add(map.get(0x1A01, 13, 13));

        // pass2 end
        endTiles.add(map.get(0x1A01, 5, 27));

        makeSave(0, gender, passNum);
        Gb.loadGambatte(1);
        gb = new Gb(0, false);
        gb.startEmulator("roms/pokecrystal.gbc", false, 295);
        GBMemory mem = new GBMemory(gb);
        wrap = new GBWrapper(gb, mem);
        wrap.advanceToAddress(0x100);

        // Only necessary at the start of the manip, when not working from savestates
        /*
        wrap.advanceWithJoypadToAddress(START, CrystalAddr.titleScreenAddr);
        wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
        wrap.advanceFrame(START);
        wrap.advanceFrame(START);
        wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
        wrap.advanceFrame(A);
        wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
        wrap.advanceFrame();
        */

        ByteBuffer state1 = gb.saveState();
        ArrayList<GscAction> actions;
        int ss=60;    // discard manips that average less than ss/60 on the frames searched

        int paths=0;
        int successes=0;
        int goodpaths=0;

        int clusterSize=1;
        int delay=frame;

        // Only necessary at the start of the manip, when not working from savestates
        /*
        int delay;
        int clusterSize=3;
        ByteBuffer[] state2 = new ByteBuffer[clusterSize];
        int minDelay=0;
        int maxDelay=14;
        */

        while(successes < ss*clusterSize || goodpaths < 10) {
            // Only necessary at the start of the manip, when not working from savestates
            /*
            delay = ThreadLocalRandom.current().nextInt(minDelay, maxDelay-clusterSize+2);
            for(int dd0=0; dd0<delay; dd0++) {
                wrap.advanceFrame();
            }

            for(int d=0; d<clusterSize; d++) {
                state2[d] = gb.saveState();
                wrap.advanceFrame();
            }
            */

            successes = 60*clusterSize;
            actions = randomPath(init, endTiles, 0);
            for (int i = 0; i < 60 && successes >= ss * clusterSize; i++) {
                for (int d = 0; d < clusterSize && successes >= ss * clusterSize; d++) {
                    int ret = CrystalAddr.owPlayerInputAddr;

                    // Only necessary at the start of the manip, when not working from savestates
                    /*
                    gb.loadState(state2[d]);
                    gb.writeMemory(0xD4C8, i);
                    wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
                    wrap.advanceFrame(A);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                    */

                    ByteBuffer state = loadByteBufferFromFile("states/" + curSegment + "/" + delay + "/" + i + ".gqs");
                    gb.loadState(state);

                    for (GscAction action : actions) {
                        ret = executeAction(ret, action);
                        if(ret != CrystalAddr.owPlayerInputAddr) {
                            successes--;
                            break;
                        }
                    }
                }
            }
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
                System.out.println("  > Successes: " + successes + "/" + (60*clusterSize));
                goodpaths++;
            }
            paths++;
            if(paths % 100 == 0) {
                System.out.println("# paths tried: " + paths);
            }
            gb.loadState(state1);
        }
    }

    private static void igt60(int gender, int delay, int passNum, String curSegment, boolean createStates) throws IOException {
        for (int i = 0; i < 60; i++) {
            System.out.println("IGT: " + i);
            System.out.println("--------");
            makeSave(i, gender, passNum);
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokecrystal.gbc", false, 295);
            GBMemory mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);
            wrap.advanceWithJoypadToAddress(START, CrystalAddr.titleScreenAddr);
            wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
            wrap.advanceFrame(START);
            wrap.advanceFrame(START);
            wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
            wrap.advanceFrame(A);
            wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
            wrap.advanceFrame();
            int clusterSize = 1;
            for (int k = 0; k < delay; k++) {
                wrap.advanceFrame();
            }
            ByteBuffer state = gb.saveState();

            String path = "L A+L L L L L L L L D L L A+D D D D L L L L L L D D L "; // 8-10

            if(delay == 8) {
                path += "L L L L L L A+L U U U U U U R R R R R U U U U L L L L L L L L L L L L L L L U U L L "; // 8
                path += "L L D D A+L L L L A+L L L "; // 8
                path += "L A+L D L L L L L L L L L L A+L L L L L L L L L A+L U U U L L L L L L L L L L A+U U U U U U U U U R R R R R U U U U U U U U U U U U U "; // 8
                if (passNum == 1) {
                    path += "R U U U R U U U U U U U U L U U L L U U U U U U R R U U U U "; // 8.1
                }
                if (passNum == 2) {
                    path += "U U U L L L L U U U U U L U L L"; // 8.2
                }
            }

            if(delay == 9) {
                path += "L L L L L U L L U U U U U R R R R R U U U U L L L L L L L L L L L L L L L U U L L "; // 9
                path += "D L L L A+D D L L L A+L L A+L L "; // 9
                if(passNum == 1) {
                    path += "A+L L L L L L L L L L L L L L L L A+L U U U L L L L L L L L L L L L L L U U U U U U U U U R R R R R U U R U U U U U U U U U U R U U U U U U "; // 9.1
                    path += "U U U U U U L U U L L U U U U U U R U U U U R"; // 9.1
                }
                if(passNum == 2) {
                    path += "A+L L L L L L L L L L L L L L L L L U U U L L L L L L L L L L L L L L U U U U U U U U U R R R R R U U U U U U U U U U U U U "; // 9.2
                    path += "U U U U U U L L L L U U L U L L"; // 9.2
                }
            }

            if(delay == 10) {
                path += "L L L L L U U L L U U U U R R R R R U U U U L L L L L L L L L L L L L L L U U L L "; // 10
                path += "D L L D D L L L L A+L L A+L L "; // 10
                path += "A+L L L L L L L L L L L L L L L L L U U U L L L L L L L L L L A+L L L L U U U U U U U U U U R R R R R U U U U U U U U U U U U "; // 10
                if (passNum == 1) {
                    path += "U A+U U U U U U U R R U U U U U L L L U U U U U U R R U U U U"; // 10.1
                }
                if (passNum == 2) {
                    path += "U A+U L L L U U U U U L L U U L L "; // 10.2
                }
            }

            String[] actions = path.split(" ");
            int d = delay;
            for (; d < delay + clusterSize; d++) {
                wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
                wrap.advanceFrame(A);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                String log = "[" + d + "] ";
                int ret = CrystalAddr.owPlayerInputAddr;
                for (String action : actions) {
                    log += action + " ";
                    GscAction owAction = GscAction.fromString(action);
                    ret = executeAction(ret, owAction);
                    System.out.println("    - [" + d + "] " + gb.readMemory(0xD507) + " " + gb.readMemory(0xD506)
                            + " " + gb.readMemory(0xD508) + ", " + gb.readMemory(0xD50E)
                            + " " + gb.readMemory(0xD50F));
                    if (ret != CrystalAddr.owPlayerInputAddr) {
                        break;
                    }
                }

                System.out.println(log);
                if (ret == CrystalAddr.owPlayerInputAddr) {
                    System.out.println("  > SUCCESS!");
                    System.out.println("  > Cycles: " + gb.getCycleCount());
                    System.out.println("  > IGT   : " + gb.readMemory(0xD4C6) + " " + gb.readMemory(0xD4C7) + " " + gb.readMemory(0xD4C8));
                    System.out.println("  > Time  : " + gb.getEonTimer());
                    System.out.println("  > AddSub: " + getRandom(gb));
                    System.out.println("  > mm#x,y: " + gb.readMemory(0xDCB5) + "|" + gb.readMemory(0xDCB6) + "#" + gb.readMemory(0xDCB8)
                            + "," + gb.readMemory(0xDCB7));
                    System.out.println("  > StepCt: " + gb.readMemory(0xDC73));
                    if(createStates) {
                        ByteBuffer bb = gb.saveState();
                        writeBytesToFile("states/" + curSegment + "/" + delay, "" + i + ".gqs", bb);
                    }
                } else {
                    System.out.println("  > FAILURE!");
                }
                gb.loadState(state);
                wrap.advanceFrame();
                state = gb.saveState();
            }
            System.out.println();
        }
    }

    public static void makeSave(int igtFrame, int gender, int passNum) throws IOException {
        byte[] baseSave;
        if(passNum == 2) {
            baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/crysr29pass2ace.sav");
        } else {
            baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/crysglitchless_r29test4.sav");
        }

        baseSave[0x2055] = (byte) 0;        // igt second
        baseSave[0x2056] = (byte) igtFrame; // igt frame

        //baseSave[0x2801] = (byte) 48; // StepCount
        //baseSave[0x2802] = (byte) 2;  // PSNStepCount

        baseSave[0x2045] = (byte) 16; // StartHour
        baseSave[0x2046] = (byte) 59; // StartMinute
        baseSave[0x2047] = (byte) 0;  // StartSecond

        baseSave[0x3E3D] = (byte) gender; // boy/girl sprite
        baseSave[0x206A] = (byte) gender; // palette

        int csum1 = 0;
        for (int i = 0x2009; i <= 0x2B82; i++) {
            csum1 += baseSave[i] & 0xFF;
        }
        csum1 = (csum1 & 0xFFFF) ^ 0xFFFF;
        baseSave[0x2D0E] = (byte) ((csum1/256 & 0xFF) ^ 0xFF);
        baseSave[0x2D0D] = (byte) ((csum1%256 & 0xFF) ^ 0xFF);

        int csum2 = 0;
        for (int j = 0x1209; j <= 0x1D82; j++) {
            csum2 += baseSave[j] & 0xFF;
        }
        csum2 = (csum2 & 0xFFFF) ^ 0xFFFF;
        baseSave[0x1F0E] = (byte) ((csum2/256 & 0xFF) ^ 0xFF);
        baseSave[0x1F0D] = (byte) ((csum2%256 & 0xFF) ^ 0xFF);

        FileFunctions.writeBytesToFile("roms/pokecrystal.sav", baseSave);
    }

    private static int executeAction(int ret, GscAction action) {
        if(ret != CrystalAddr.owPlayerInputAddr) {
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
            wrap.injectCrysInput(input);
            ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr,
                    CrystalAddr.printLetterDelayAddr, CrystalAddr.bonkSoundAddr);

            if(ret == CrystalAddr.countStepAddr) {
                ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.owPlayerInputAddr, CrystalAddr.startWildBattleAddr);
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
        for(int x = 4; x<=16; x++) {
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
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                }
                if(!((x>53) || (y==16) || (y==13 && x>=48) || (y==15 && x>=44) || (x==50 && y==11))) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    tile.addEdge(0, GscAction.ADOWN, tileDown);
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
                    tile.addEdge(0, GscAction.ADOWN, tileDown);
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
            for (int x = 7; x <= 14; x++) {
                for (int y = 26; y <= 53; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileRight = map.get(0x1A01, x + 1, y);
                    if (!((x == 7 && y >= 50) || (x == 13 && y >= 38) || (x == 12 && y == 29) || (x == 14))) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                    if (!((y == 48 && x <= 11) || (x == 13 && y == 30) || (y == 28 && x <= 13))) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
            }
            for (int x = 11; x <= 14; x++) {
                for (int y = 14; y <= 25; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileLeft = map.get(0x1A01, x - 1, y);
                    GscTile tileRight = map.get(0x1A01, x + 1, y);
                    if (!((y == 22 && x >= 12) || (y == 17 && x == 11))) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                    if ((y >= 22) && (x >= 12) && !((x == 12 && y >= 24))) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                    if (y <= 17 && x != 13 && !(x == 12 && y == 14)) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
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
                for (int y = 27; y <= 35; y++) {
                    GscTile tile = map.get(0x1A01, x, y);
                    GscTile tileUp = map.get(0x1A01, x, y - 1);
                    GscTile tileLeft = map.get(0x1A01, x - 1, y);
                    if (!((y == 30 && x >= 10) || (y == 28 && x >= 8) || y == 27)) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                    if (!((x == 9 && y == 35) || (x == 6 && y >= 28))) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                }
            }
        }
    }

    private static String getRandom(Gb gb) {
        int hRandom = (gb.readMemory(0xFFE1) << 8) | gb.readMemory(0xFFE2);
        return String.format("0x%4s", Integer.toHexString(hRandom).toUpperCase()).replace(' ', '0');
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