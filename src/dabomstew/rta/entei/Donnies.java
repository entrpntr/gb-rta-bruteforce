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

public class Donnies {
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
        int x = 3;
        int y = 22;
        boolean igt60CreateStates = false;
        boolean searchFromStart = false;
        boolean searchFixedFrame = true;   // change at beginning if you want to search random frames

        String curSegment = "donnies";

        // build out prospective paths here
        String segPath = "L U U U U U U U L U U U U A+U R U U U R U R A+R ";

        // 12
        //segPath += "R U U A+U U R U U U U U U A+R R R R R U A+U U U U ";
        //segPath += "L L L A+L L L L L L L L D L L L L D A+L L L L U L L L L U U ";

        // 13
        //segPath += "U U R U U U R U S_B U U U R R R R R U U U U U U ";
        //segPath += "L L L L L A+L L L L L L D L L L L L D A+L L A+L L U U L L L U ";

        // 14
        //segPath += "U U R U U U S_B R U U U U R U R R R R U A+U U U U ";
        //segPath += "L L L L L L L L L L L D L L L L L D L L A+L L U L U A+L L U ";

        // 15
        segPath += "R U U R U U U S_B U U U U U R R R R R U A+U U U U ";
        segPath += "L L L L L L L L L L L D L L L L L D L L L L L L L U U U ";

        igt60(frame, x, y, curSegment, segPath, igt60CreateStates);
        //search(frame, curSegment, searchFromStart, searchFixedFrame);
    }

    public static void search(int frame, String curSegment, boolean fromStart, boolean fixedFrame) throws IOException {
        initGrid();

        PathState init = null;
        ArrayList<GscTile> startTiles = new ArrayList<>();
        /*
        startTiles.add(map.get(0x1A01, 5, 24));
        startTiles.add(map.get(0x1A01, 4, 24));
        startTiles.add(map.get(0x1A01, 3, 24));
        startTiles.add(map.get(0x1A01, 4, 23));
        startTiles.add(map.get(0x1A01, 3, 23));
        startTiles.add(map.get(0x1A01, 2, 23));
        startTiles.add(map.get(0x1A01, 4, 22));
        startTiles.add(map.get(0x1A01, 2, 22));
        startTiles.add(map.get(0x1A01, 4, 21));
        startTiles.add(map.get(0x1A01, 3, 20));
        startTiles.add(map.get(0x1A01, 2, 20));
        startTiles.add(map.get(0x1A01, 4, 19));
        */
        startTiles.add(map.get(0x1A01, 3, 22));
        //startTiles.add(map.get(0x1A01, 2, 21));
        //startTiles.add(map.get(0x1A01, 4, 20));

        List<GscTile> endTiles = new ArrayList<>();

        if(fromStart) {
            endTiles.add(map.get(0x1A01, 5, 4));
            endTiles.add(map.get(0x1A01, 5, 5));
            endTiles.add(map.get(0x1A01, 5, 6));
            endTiles.add(map.get(0x1A01, 6, 6));
            endTiles.add(map.get(0x1A01, 7, 6));
        } else if (curSegment.equals("donnies")) {
            init = new PathState(1, 0, map.get(0x1A01, 5, 6), new ArrayList<>());
            endTiles.add(map.get(0x1A02, 32, 9));
            //endTiles.add(map.get(0x1A02, 9, 9));
        } else if (curSegment.equals("donnies2")) {
            init = new PathState(0, 0, map.get(0x1A02, 32, 9), new ArrayList<>());
            endTiles.add(map.get(0x1A02, 9, 9));
        }

        ByteBuffer[] state1 = new ByteBuffer[startTiles.size()];

        for(int t=0; t<startTiles.size(); t++) {
            GscTile tile = startTiles.get(t);
            makeSave(0, tile.getX(), tile.getY());
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokegold.gbc", false, 900);
            GBMemory mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);
            wrap.advanceWithJoypadToAddress(LEFT, 0x100);

            if (fromStart) {
                wrap.advanceWithJoypadToAddress(START, titleScreenAddr);
                wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
                wrap.advanceFrame(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                wrap.advanceFrame(A);
                wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.readJoypadAddr);
                wrap.advanceFrame(LEFT);

                state1[t] = gb.saveState();
            }
        }

        ArrayList<GscAction> actions;
        int ss = 58;    // discard manips that average less than ss/60 on the frames searched (always search 60/60 now)
        int ssOff = 0;  // fractional part of ss to eke out a bit more IGT success ((ss + ssOff/clusterSize)/60.0)

        int paths = 0;
        int successes = 0;
        int goodpaths = 0;

        int clusterSize = 1;
        int minDelay = 0;
        int maxDelay = 0;

        int delay = frame;
        ByteBuffer[] state2 = null;
        if(fromStart) {
            state2 = new ByteBuffer[clusterSize];
        }

        if(!fixedFrame) {
            minDelay = 0;
            maxDelay = 17;
        }

        while(successes < (ss*clusterSize + ssOff) || goodpaths < 100) {
            GscTile startTile = null;
            if(fromStart) {
                int idx = ThreadLocalRandom.current().nextInt(0, startTiles.size());
                gb.loadState(state1[idx]);
                startTile = startTiles.get(idx);
                init = new PathState(1, 0, startTile, new ArrayList<>());   // **start
            } else {
                startTile = init.getCurTile();
            }

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
            if(fixedFrame && (delay==12 || ((delay==13 || delay==14) && curSegment.equals("donnies2")))) {
                successes--;
            }
            actions = randomPath(init, endTiles, 0);
            String hRands = "";
            for (int i = 0; i < 60 && successes >= (ss * clusterSize + ssOff); i++) {
                for (int d = 0; d < clusterSize && successes >= (ss * clusterSize + ssOff); d++) {
                    if(!(delay==12 && i==46 && fixedFrame) ||
                        (delay==14 && i== 1 && fixedFrame && curSegment.equals("donnies2"))
                    )
                    {
                        int ret = GoldSilverAddr.owPlayerInputAddr;
                        if (fromStart) {
                            gb.loadState(state2[d]);
                            gb.writeMemory(0xD1EF, i);
                            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                            wrap.advanceFrame(A);
                            wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.owPlayerInputAddr);
                        } else {
                            ByteBuffer state = loadByteBufferFromFile("states/" + gameName + "/" + curSegment + "/" + delay + "/" + i + ".gqs");
                            gb.loadState(state);
                        }

                        for (GscAction action : actions) {
                            ret = executeAction(ret, action);
                            if (ret != GoldSilverAddr.owPlayerInputAddr) {
                                successes--;
                                break;
                            } else if(i==0) {
                                hRands = getRandom(gb);
                            }
                        }
                    }
                }
            }
            paths++;
            /*
            System.out.print("" + paths + ": <x=" + startTile.getX() + ", y=" + startTile.getY() + ", delay=" + delay + "> ");
            for(GscAction action : actions) {
                System.out.print(action.logStr() + " ");
            }
            System.out.println();
            */
            if(successes >= (ss*clusterSize + ssOff)) {
                if(clusterSize>1) {
                    System.out.print("" + paths + ": <x=" + startTile.getX() + ", y=" + startTile.getY() + ", delay=" + + delay + "-" + (delay + clusterSize - 1) + "> ");
                }
                else {
                    System.out.print("" + paths + ": <x=" + startTile.getX() + ", y=" + startTile.getY() + ", delay=" + delay + "> ");
                }
                for(GscAction action : actions) {
                    System.out.print(action.logStr() + " ");
                }
                System.out.println();
                System.out.print("  > Successes: " + successes + "/" + (60*clusterSize));
                System.out.print(" -- AddSub[f0]: " + hRands);
                System.out.println();
                goodpaths++;
            }
            //if(paths % 100 == 0) {
            if(paths % 1000 == 0) {
                System.out.println("# paths tried: " + paths);
            }
        }
    }

    private static void igt60(int delay, int x, int y, String curSegment, String segPath, boolean createStates) throws IOException {
        for (int i = 0; i < 60; i++) {
            System.out.println("IGT: " + i);
            System.out.println("--------");
            makeSave(i, x, y);
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokegold.gbc", false, 900);
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
                    if(gb.readMemory(0xDA01) == 0x01) {
                        System.out.println("    - [" + d + "] " + gb.readMemory(0xD256) + " " + gb.readMemory(0xD255)
                                + " " + gb.readMemory(0xD257));
                    }
                    if (ret != GoldSilverAddr.owPlayerInputAddr) {
                        break;
                    }
                }

                System.out.println(log);
                if (ret == GoldSilverAddr.owPlayerInputAddr) {
                    System.out.println("  > SUCCESS!");
                    System.out.println("  > Cycles: " + gb.getCycleCount());
                    System.out.println("  > IGT   : " + gb.readMemory(0xD1ED) + " " + gb.readMemory(0xD1EE) + " " + gb.readMemory(0xD1EF));
                    System.out.println("  > Time  : " + gb.getEonTimer());
                    System.out.println("  > AddSub: " + getRandom(gb));
                    System.out.println("  > mm#x,y: " + gb.readMemory(0xDA00) + "|" + gb.readMemory(0xDA01) + "#" + gb.readMemory(0xDA03) + "," + gb.readMemory(0xDA02));
                    System.out.println("  > StepCt: " + gb.readMemory(0xD9BD));
                } else {
                    //doTextbox(1, A, true);
                    //doTextbox(1, B, false);
                    System.out.println("  > FAILURE!");
                }
                if(createStates) {
                    ByteBuffer bb = gb.saveState();
                    writeBytesToFile("states/" + gameName + "/" + curSegment + "/" + delay, "" + i + ".gqs", bb);
                }
                gb.loadState(state);
                wrap.advanceFrame(LEFT);
                state = gb.saveState();
            }
            System.out.println();
        }
    }

    public static void makeSave(int igtFrame, int x, int y) throws IOException {
        byte[] baseSave;
        baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/gold_don_" + x + "_" + y + ".sav");

        baseSave[0x2056] = (byte) 0;        // igt second
        baseSave[0x2057] = (byte) igtFrame; // igt frame

        baseSave[0x2045] = (byte) 9; // StartHour
        baseSave[0x2046] = (byte) 59; // StartMinute
        baseSave[0x2047] = (byte) 0;  // StartSecond

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
        else if(action == GscAction.START_B) {
            wrap.injectGSInput(START);
            wrap.advanceFrame(START);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(B);
            ret = wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.owPlayerInputAddr);
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
        boolean edgesContainsStart = edges.stream().anyMatch(x -> (x.getAction().logStr().startsWith("S_B")));
        boolean removeStart = state.getCurPath().contains(GscAction.START_B);
        int sbInc = removeStart ? 0 : 1;
        int sbInc2 = (removeStart && edgesContainsStart) ? 1 : 0;
        int high = (removeAPresses) ? (edges.size()+sbInc) / 2 : (edges.size()-sbInc2);
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
    private static void initGrid() {
        for (int x = 0; x <= 7; x++) {
            for (int y = 0; y <= 24; y++) {
                GscCoord c = new GscCoord(0x1A01, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x = 9; x<=32; x++) {
            for(int y=8; y<=17; y++) {
                GscCoord c = new GscCoord(0x1A02, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x=1; x<=5; x++) {
            for (int y=11; y<=24; y++) {
                GscTile tile = map.get(0x1A01, x, y);
                GscTile tileLeft = map.get(0x1A01, x-1, y);
                GscTile tileUp = map.get(0x1A01, x, y-1);
                if(!((x==4 && y==21) || (x==3 && y==13) || (x==2 && y>=16) || (x==1))) {
                    tile.addEdge(0, GscAction.LEFT, tileLeft);
                    tile.addEdge(0, GscAction.ALEFT, tileLeft);
                }
                if(!((y==24 && x==5) || (y==22 && x==3) || (y==18 && x==4) || (y==14 && x==2) || (y==12 && x>=2))) {
                    tile.addEdge(0, GscAction.UP, tileUp);
                    tile.addEdge(0, GscAction.AUP, tileUp);
                }
            }
        }
        for(int x=1; x<=7; x++) {
            for(int y=0; y<=10; y++) {
                GscTile tile = map.get(0x1A01, x, y);
                GscTile tileUp;
                if(y!=0) {
                    tileUp = map.get(0x1A01, x, y-1);
                } else {
                    tileUp = map.get(0x1A02, x+20, 17);
                }
                GscTile tileRight = map.get(0x1A01, x+1, y);
                if(!((x==3 && y==7) || (x==5 && y>=9) || (x==6 && y==8) || (x==7))) {
                    for(int z=0; z<18; z++) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                }
                if(!((y==8 && x==4) || (y==6 && x==1) || (y==5 && x<=3) || (y==4 && x<=5))) {
                    for(int z=0; z<18; z++) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
                if(x>=6 && y<=5) {
                    tile.addEdge(0, GscAction.START_B, tile);
                }
            }
        }
        for(int x=26; x<=32; x++) {
            for(int y=10; y<=17; y++) {
                GscTile tile = map.get(0x1A02, x, y);
                GscTile tileUp = map.get(0x1A02, x, y-1);
                GscTile tileRight = map.get(0x1A02, x+1, y);
                if(!((x==27 && y>=16) || (x==32))) {
                    for(int z=0; z<18; z++) {
                        tile.addEdge(0, GscAction.RIGHT, tileRight);
                        tile.addEdge(0, GscAction.ARIGHT, tileRight);
                    }
                }
                if(!((y==14 && x<=31))) {
                    for(int z=0; z<18; z++) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
                if(y!=17) {
                    tile.addEdge(0, GscAction.START_B, tile);
                }
            }
        }
        for(int x=14; x<=32; x++) {
            for(int y=9; y<=12; y++) {
                GscTile tile = map.get(0x1A02, x, y);
                GscTile tileLeft = map.get(0x1A02, x-1, y);
                GscTile tileDown;
                if(x!=19 && y==10) {
                    tileDown = map.get(0x1A02, x, y+2);
                } else {
                    tileDown = map.get(0x1A02, x, y+1);
                }
                if(!((x==16 && y<=11) || (x==32 && y>=10))) {
                    for(int z=0; z<36; z++) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                }
                if(!((x>=22) || (y==10 && x>=18) || (y==12))) {
                    for(int z=0; z<36; z++) {
                        tile.addEdge(0, GscAction.DOWN, tileDown);
                        tile.addEdge(0, GscAction.ADOWN, tileDown);
                    }
                }
                if(!((x==32 && y>=10))) {
                    tile.addEdge(0, GscAction.START_B, tile);
                }
            }
        }
        for(int x=9; x<=13; x++) {
            for(int y=10; y<=12; y++) {
                GscTile tile = map.get(0x1A02, x, y);
                GscTile tileLeft = map.get(0x1A02, x-1, y);
                GscTile tileUp = map.get(0x1A02, x, y-1);
                if(x>=10) {
                    for(int z=0; z<36; z++) {
                        tile.addEdge(0, GscAction.LEFT, tileLeft);
                        tile.addEdge(0, GscAction.ALEFT, tileLeft);
                    }
                }
                if(!((y==10 && x>=10))) {
                    for(int z=0; z<36; z++) {
                        tile.addEdge(0, GscAction.UP, tileUp);
                        tile.addEdge(0, GscAction.AUP, tileUp);
                    }
                }
                tile.addEdge(0, GscAction.START_B, tile);
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