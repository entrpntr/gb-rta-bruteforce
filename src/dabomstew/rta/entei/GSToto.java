package dabomstew.rta.entei;

import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.GoldSilverAddr;
import mrwint.gbtasgen.Gb;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class GSToto {
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static Gb gb;
    private static GBWrapper wrap;
    private static GscTileMap map = new GscTileMap();
    private static double maxTotoTime = 13.9;  // equates to maximum ~2.37s of cost
    private static double tileCost = 0.0;
    private static PrintWriter writer;


    public static void makeSave(int startHour, int startMin, int x, int y, int momStep, int audio, int frameType, int menuAccount, int igtFrame) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/gold_toto_" + x + "_" + y + ".sav");

        baseSave[0x2056] = (byte) 58;        // igt second
        baseSave[0x2057] = (byte) igtFrame; // igt frame

        int sc = (int) (baseSave[0x2825]);
        int psc = (int) (baseSave[0x2826]);
        baseSave[0x2825] = (byte) (sc + momStep); // StepCount
        baseSave[0x2826] = (byte) ((psc + momStep) % 4); // PoisonStepCount

        baseSave[0x2000] = (byte) audio;
        baseSave[0x2002] = (byte) frameType;
        baseSave[0x2005] = (byte) menuAccount;

        baseSave[0x2045] = (byte) startHour; // StartHour
        baseSave[0x2046] = (byte) startMin; // StartMinute
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
        if(ret != GoldSilverAddr.owPlayerInputAddr) {
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
            wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.countStepAddr);
            ret = wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.owPlayerInputAddr);
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

    private static void executePath(String path, int delay, int f) {
        String[] actions = null;
        if(path != null && !path.equals("")) {
            actions = path.split(" ");
        }
        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.owPlayerInputAddr);
        String log = "[" + f + "] ";
        int ret = GoldSilverAddr.owPlayerInputAddr;
        String npc = " - npc: ";
        if(actions != null) {
            for (int i=0; i<actions.length; i++) {
                log += actions[i] + " ";
                GscAction owAction = GscAction.fromString(actions[i]);
                ret = executeAction(ret, owAction);
                if(i==3) {
                    int npcDir = gb.readMemory(0xD232);
                    if(npcDir==0)  { npc += "d"; }
                    if(npcDir==4)  { npc += "u"; }
                    if(npcDir==8)  { npc += "l"; }
                    if(npcDir==12) { npc += "r"; }
                }
            }
        }
        wrap.injectGSInput(A);
        wrap.advanceFrame(A);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
        wrap.advanceFrame(B);
        //doTextbox(1, B, true);
        //doTextbox(1, A, true);
        //doTextbox(3, B, true);
        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.buttonSoundAddr);
        wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
        wrap.injectGSMenuInput(A | B);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.yesNoBoxAddr);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
        wrap.injectGSMenuInput(A);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
        wrap.injectGSMenuInput(A | B);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
        wrap.injectGSMenuInput(A | B);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
        wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
        wrap.injectGSMenuInput(A | B);
        wrap.advanceWithJoypadToAddress(A | B, GoldSilverAddr.calcStatsAddr);
        System.out.print(log + ": " + getDVs() + " " + getStats());
        System.out.println(npc);
        //double totoTime = gb.getGbpTime() + tileCost - 20.661;
        //System.out.print(" - totoTime: " + String.format("%.3f", totoTime));
        //System.out.println();
    }

    private static void initGrid() {
        for(int x=4; x<=7; x++) {
            for(int y=2; y<=5; y++) {
                GscCoord c = new GscCoord(0, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for(int x=4; x<=7; x++) {
            for(int y=2; y<=5; y++) {
                GscTile tile = map.get(0, x, y);
                GscTile tileRight = map.get(0, x+1, y);
                GscTile tileDown = map.get(0, x, y+1);
                GscTile tileUp = map.get(0, x, y-1);
                if(!(x==4 && y==2) && !(x==5 && y==3) && x!=7) {
                    tile.addEdge(0, GscAction.RIGHT, tileRight);
                    tile.addEdge(0, GscAction.ARIGHT, tileRight);
                }
                if(y!=5) {
                    tile.addEdge(0, GscAction.DOWN, tileDown);
                    if(!(x==4 && y==2) && !(x==5 && y==3)) {
                        tile.addEdge(0, GscAction.ADOWN, tileDown);
                    }
                }
                if(x+y!=12) {
                    tile.addEdge(0, GscAction.START_B, tile);
                }
                if(x+y==12) {
                    tile.addEdge(1, GscAction.UP, tileUp);
                    tile.addEdge(1, GscAction.START_B, tile);
                }
                if(x==7 && y==4) {
                    tile.addEdge(1, GscAction.START_B, tile);
                }
            }
        }
    }

    private static double getMinTotoTime(int x, int y, int edgeset) {
        double gbpTime = gb.getGbpTime();
        int stepsToToto = 0;
        if(edgeset == 0) {
            stepsToToto = 12-x-y+1;
        } else {
            stepsToToto = x+y-11;
        }
        return gbpTime + 16.0/59.7275*(double)(stepsToToto);
    }

    private static void overworldSearch(GscState state, int edgeset) {
        int x = state.getX();
        int y = state.getY();
        double minTotoTime = getMinTotoTime(x, y, edgeset);
        if(minTotoTime + tileCost > maxTotoTime) {
            return;
        }

        ByteBuffer oldState = gb.saveState();
        GscTile tile = map.get(0, state.getX(), state.getY());
        if(edgeset == 1 && tile.getX()==7 && tile.getY()==4) {
            wrap.injectGSInput(A);
            wrap.advanceFrame(A);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(B);
            //doTextbox(1, B, true);
            //doTextbox(1, A, true);
            //doTextbox(3, B, true);
            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.buttonSoundAddr);
            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(A | B);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.yesNoBoxAddr);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(A);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(A | B);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(A | B);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.buttonSoundAddr);
            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.injectGSMenuInput(A | B);
            wrap.advanceWithJoypadToAddress(A | B, GoldSilverAddr.calcStatsAddr);
            writer.print(state.getLogStr() + ": " + getDVs() + " " + getStats());
            double totoTime = gb.getGbpTime() + tileCost - 20.661;
            writer.print(" - totoTime: " + String.format("%.3f", totoTime));
            int stars = 0;
            if (getAtk() == 15)      { stars += 2; }
            else if (getAtk() >= 12) { stars++;    }
            else if(getAtk() <= 7)   { stars -= 3; }
            else if(getAtk() <= 8)   { stars -= 2; }
            else if(getAtk() <= 9)   { stars--;    }
            if (getDef() >= 13)      { stars += 2; }
            else if(getDef() >= 10)  { stars++;    }
            else if(getDef() <= 5)   { stars -= 3; }
            else                     { stars--;    }
            if (getSpd() == 15)      { stars += 2; }
            else if (getSpd() >= 11) { stars++;    }
            else if(getSpd() <= 3)   { stars -= 3; }
            else if(getSpd() <= 7)   { stars -= 2; }
            else if(getSpd() <= 9)   { stars--;    }
            if (getSpc() == 15)      { stars += 2; }
            else if (getSpc() >= 14) { stars++;    }
            else if(getSpc() <= 7)   { stars -= 3; }
            else if(getSpc() <= 9)   { stars -= 2; }
            else if(getSpc() <= 11)  { stars--;    }

            if (stars == 8) {
                writer.print(" [***]");
            } else if (stars >= 6) {
                writer.print(" [ **]");
            } else if (stars >= 4) {
                writer.print(" [  *]");
            }
            writer.println();
            writer.flush();
            gb.loadState(oldState);
        }

        HashMap<Integer, List<GscEdge>> edgeMap = tile.getEdges();
        if(edgeMap == null) {
            return;
        }

        List<GscEdge> edgeList = edgeMap.get(edgeset);
        if(edgeList == null) {
            return;
        }

        for(GscEdge edge : edgeList) {
            GscAction action = edge.getAction();
            if (GscAction.isDpad(action)) {
                int input;
                boolean isA = false;
                if (action.logStr().startsWith("A")) {
                    input = 1 | (16 * (int) (Math.pow(2.0, (action.ordinal() - 8))));
                    isA = true;
                    if (!state.canSelect()) {
                        continue;
                    }
                } else {
                    input = 16 * (int) (Math.pow(2.0, (action.ordinal())));
                }
                wrap.injectGSInput(input);
                wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.countStepAddr);
                wrap.advanceWithJoypadToAddress(input, GoldSilverAddr.owPlayerInputAddr);
                GscTile nextTile = edge.getNextTile();
                int nextEdgeset = edgeset;
                if (nextTile.getX() == 7 && nextTile.getY() == 5) {
                    nextEdgeset = 1;
                }
                GscState newState = new GscState(
                        state.getLogStr() + " " + edge.getAction().logStr(),
                        state.onBike(),
                        (state.getFacingDir() != input) || state.turnframeStatus(),
                        state.justChangedDir(),
                        input,
                        gb.getDivState(),
                        gb.readMemory(0xFFE1),
                        gb.readMemory(0xFFE2),
                        edge.getNextTile().getX(),
                        edge.getNextTile().getY(),
                        !isA,
                        true,
                        false
                );
                overworldSearch(newState, nextEdgeset);
            } else if (edge.getAction() == GscAction.START_B) {
                if (!state.canStart()) {
                    continue;
                }
                wrap.injectGSInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
                wrap.injectGSMenuInput(B);
                wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.owPlayerInputAddr);
                GscState newState = new GscState(
                        state.getLogStr() + " " + edge.getAction().logStr(),
                        state.onBike(),
                        true,
                        false,
                        state.getFacingDir(),
                        gb.getDivState(),
                        gb.readMemory(0xFFE1),
                        gb.readMemory(0xFFE2),
                        edge.getNextTile().getX(),
                        edge.getNextTile().getY(),
                        true,
                        true,
                        true
                );
                overworldSearch(newState, edgeset);
            }
            gb.loadState(oldState);
        }
    }

    public static void search() throws IOException {
        //TODO: mmback + fsback
        //boolean reachedCheckpoint = false;
        String ts = Long.toString(System.currentTimeMillis());
        String fileName = "gold_toto_" + ts + ".txt";
        System.out.println(fileName);
        writer = new PrintWriter(fileName);
        //writer = new PrintWriter(System.out);
        initGrid();
        GscTile[] startTiles = {map.get(0,7,4), map.get(0,5,3), map.get(0,4,3), map.get(0,4,2)};
        //TODO
        //int[] startHour = {10,0,2,8,16,18};
        int[] startHour = {10,8,2,18};
        //int[] startMin = {51,59,1,11,21,31,41,51};
        int[] startMin = {51,59};
        for(GscTile tile : startTiles) {
            int x = tile.getX();
            int y = tile.getY();
            int edgeset = (x == 7 && y == 4) ? 1 : 0;
            tileCost = (x==7 && y==4) ? 0.0 : (double)(11-x-y)*16.0/-59.7275;
            for (int h : startHour) {
                for (int m : startMin) {
                    for (int ms = 0; ms <= 1; ms++) {
                        if (edgeset == 1 && ms == 1) {
                            continue;
                        }
                        for (int aud = 0xC1; aud <= 0xE1; aud+=0x20) {
                            for (int frameType = 0; frameType <= 7; frameType++) {
                                // TODO
                                for (int ma = 0; ma <= 1; ma++) {
                                    for (int igt = 0; igt < 60; igt += 6) {
                                        /*
                                        if(!reachedCheckpoint) {
                                            if(!(x==4 && y==3 && h==2 && m==51 && ms==1 && aud==0xC1 && igt==24)) {
                                                continue;
                                            }
                                            reachedCheckpoint = true;
                                        }
                                        */
                                        makeSave(h, m, x, y, ms, aud, frameType, ma, igt);
                                        Gb.loadGambatte(1);
                                        gb = new Gb(0, false);
                                        gb.startEmulator("roms/pokegold.gbc", false, 120);
                                        GBMemory mem = new GBMemory(gb);
                                        wrap = new GBWrapper(gb, mem);
                                        wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
                                        wrap.advanceFrame(START);
                                        wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
                                        wrap.advanceFrame(START);
                                        int mmback = 0; // 1.4s
                                        ByteBuffer mmbackState = gb.saveState();
                                        for (; mmback <= 3; mmback++) {
                                            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                                            wrap.advanceFrame(A);
                                            int fsback = 0; // 1.7s
                                            ByteBuffer fsbackState = gb.saveState();
                                            for (; fsback <= 3; fsback++) {
                                                wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.readJoypadAddr);
                                                wrap.advanceFrame(LEFT);
                                                ByteBuffer saveState = gb.saveState();
                                                for (int d = 0; d <= 240; d++) {
                                                    if (gb.getGbpTime() + 0.9 + (tileCost == 0.0 ? 0.0 : 32.0 / 59.7275) > maxTotoTime) {
                                                        break;
                                                    }
                                                    String log = "(x=" + x + ", y=" + y + ", h=" + h + ", m=" + m + ", momStep=" + ms + ", audio=" + Integer.toHexString(aud).toUpperCase() + ", frameType=" + (frameType) + ", menuAcc=" + ma + ", igt=" + igt + ", mmback=" + mmback + ", fsback=" + fsback + ", delay=" + d + ")";
                                                    wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.owPlayerInputAddr);
                                                    GscState owState = new GscState(
                                                            log,
                                                            false,
                                                            false,
                                                            false,
                                                            UP,
                                                            gb.getDivState(),
                                                            gb.readMemory(0xFFE1),
                                                            gb.readMemory(0xFFE2),
                                                            x,
                                                            y,
                                                            false,
                                                            true,
                                                            true);
                                                    overworldSearch(owState, edgeset);
                                                    gb.loadState(saveState);
                                                    wrap.advanceFrame(LEFT);
                                                    saveState = gb.saveState();
                                                }
                                                gb.loadState(fsbackState);
                                                wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
                                                wrap.advanceFrame(B);
                                                wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
                                                wrap.advanceFrame(A);
                                                fsbackState = gb.saveState();
                                            }
                                            gb.loadState(mmbackState);
                                            wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
                                            wrap.advanceFrame(B);
                                            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
                                            wrap.advanceFrame(START);
                                            mmbackState = gb.saveState();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    public static void igtCheck() throws IOException {
        int x=4;
        int y=3;
        int delay=11;
        tileCost = (x==7 && y==4) ? 0.0 : (double)(11-x-y)*16.0/-59.7275;
        for(int f=23; f<30; f++) {
            makeSave(9, 59, x, y, 0, 0xE1, 0x00, 0x00, f);
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokegold.gbc", false, 120);
            GBMemory mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);
            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(START);
            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(START);
            /*wrap.advanceWithJoypadToAddress(B, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(B);
            wrap.advanceWithJoypadToAddress(START, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(START);*/
            wrap.advanceWithJoypadToAddress(A, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(A);
            wrap.advanceWithJoypadToAddress(LEFT, GoldSilverAddr.readJoypadAddr);
            wrap.advanceFrame(LEFT);
            for (int dd = 0; dd < delay; dd++) {
                wrap.advanceFrame(LEFT);
            }
            //System.out.println(gb.getEonTimer());
            executePath("R S_B D A+R R D U", delay, f);
        }
    }

    public static void main(String[] args) throws IOException {
        //search();
        igtCheck();
    }

    private static void doTextbox(int num, int input, boolean clear) {
        for (int i = 0; i < num; i++) {
            boolean cont = true;
            while (cont) {
                wrap.advanceWithJoypadToAddress(2/input, GoldSilverAddr.readJoypadAddr);
                int sp = gb.getRegisters()[1] + 8;
                int ret = gb.readMemory(sp) | (gb.readMemory(sp + 1) << 8);
                if (ret == 0x320D) {
                    wrap.advanceFrame(2/input);
                } else {
                    cont = false;
                }
            }
            if(clear) {
                wrap.injectGSMenuInput(A | B);
                wrap.advanceFrame(A | B);
                wrap.advanceFrame(2/input);
            }
        }
    }

    public static String getDVs() {
        int dvs = (gb.readMemory(0xDA3F) << 8) | gb.readMemory(0xDA40);
        return String.format("0x%4s", Integer.toHexString(dvs).toUpperCase()).replace(' ', '0');
    }

    public static int getAtk() {
        return gb.readMemory(0xDA3F) / 16;
    }

    public static int getDef() {
        return gb.readMemory(0xDA3F) % 16;
    }

    public static int getSpd() {
        return gb.readMemory(0xDA40) / 16;
    }

    public static int getSpc() {
        return gb.readMemory(0xDA40) % 16;
    }

    public static String getStats() {
        return "(" + gb.readMemory(0xDA4F) + "/" + gb.readMemory(0xDA51) + "/"
                + gb.readMemory(0xDA53) + "/" + gb.readMemory(0xDA57) + "/"
                + gb.readMemory(0xDA59) + "/" + gb.readMemory(0xDA55) + ")";
    }
}
