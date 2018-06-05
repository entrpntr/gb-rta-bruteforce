package dabomstew.rta.entei;

import dabomstew.rta.*;
import mrwint.gbtasgen.Gb;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class Entei {
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static GscTileMap map = new GscTileMap();
    private static HashSet<GscState> seenStates = new HashSet<>();
    private static double r37entrance;
    private static Gb gb;
    private static GBWrapper wrap;
    private static PrintWriter writer;

    public static void makeSave(int frame) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/entei_13_28.sav");
        ///*
        baseSave[0x2000] = (byte) 0xE1;  // options
        //baseSave[0x2005] = (byte) 0x00;  // menu account

        baseSave[0x2055] = (byte) 0;     // igt second
        baseSave[0x2056] = (byte) frame; // igt frame

        //int poisonStepCount = baseSave[0x2802];
        int poisonStepCount = (baseSave[0x2802] + 3) % 4;  // Made all baseSaves without repel wearing off
        baseSave[0x2802] = (byte) poisonStepCount; // poison step count
        System.out.println("PoisonStepCount: " + poisonStepCount);

        int playerState = 1;          // 0 = walking, 1 = biking
        baseSave[0x24EB] = (byte) playerState;

        int facingDir = 0x0C;         // 0x00 = down, 0x04 = up, 0x08 = left, 0x0C = right
        baseSave[0x206C] = (byte) facingDir;

        //baseSave[0x206F] = (byte) 1;  // PlayerAction

        int repelEffect = 0x64;
        baseSave[0x282F] = (byte) repelEffect;

        //baseSave[0x2865] = (byte) 2;     // party count
        //baseSave[0x2868] = (byte) 0xFF;  // party terminator (if party count=2)

        baseSave[0x2865] = (byte) 3;       // party count
        baseSave[0x2868] = (byte) 0x3C;    // party3species
        baseSave[0x2869] = (byte) 0xFF;    // party terminator (if party count=3)
        int[] poliStruct = {
            0x3C, 0x00, 0x91, 0x00, 0x00, 0x00, 0x6B, 0xE7, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0xED, 0x86, 0x1E, 0x00, 0x00, 0x00, 0x53, 0x00, 0xC4, 0x84, 0x04,
            0x00, 0x00, 0x00, 0x11, 0x00, 0x11, 0x00, 0x0A, 0x00, 0x09, 0x00, 0x0C, 0x00, 0x08, 0x00, 0x08
        };
        for(int i=0; i<poliStruct.length; i++) {
            baseSave[0x28CD + i] = (byte) poliStruct[i];
        }
        int[] poliNick = {0x8F, 0x8E, 0x8B, 0x88, 0x96, 0x80, 0x86, 0x50, 0x50, 0x50, 0x50};
        for(int i=0; i<poliNick.length; i++) {
            baseSave[0x29E5 + i] = (byte) poliNick[i];
        }
        int[] poliOTName = {0x80, 0x50, 0x50, 0x50, 0x50, 0x50, 0x50, 0x50, 0x50, 0x50, 0x50};
        for(int i=0; i<poliOTName.length; i++) {
            baseSave[0x29A3 + i] = (byte) poliOTName[i];
        }

        //baseSave[0x2687] = (byte) 0x40;    // Greg defeated event flag

        baseSave[0x2045] = (byte) 17; // StartHour
        baseSave[0x2046] = (byte) 53; // StartMinute
        baseSave[0x2047] = (byte) 0;  // StartSecond

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
        //*/
        FileFunctions.writeBytesToFile("roms/pokecrystal.sav", baseSave);
    }

    private static void overworldSearch(GscState state) {
        if(!seenStates.add(state)) {
            return;
        }

        double gbpTime = gb.getGbpTime();
        String r37Str = state.getLogStr().substring(state.getLogStr().indexOf("//"));
        if((gbpTime > r37entrance + 3.5) || (gbpTime > r37entrance + 2.5 && !r37Str.contains("S_B"))) {
            return;
        }

        ByteBuffer oldState = gb.saveState();

        GscTile tile = map.get(0x0A04, state.getX(), state.getY());
        for(GscEdge edge : tile.getEdges().get(0)) {
            if(GscAction.isDpad(edge.getAction())) {
                int input = 16 * (int) (Math.pow(2.0, (edge.getAction().ordinal())));
                if(state.onBike() && state.justChangedDir() && state.prevChangedDir() && (input != state.getFacingDir() || state.turnframeStatus())) {
                    continue;
                }
                wrap.injectCrysInput(input);
                int ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                boolean turnframeEnc = true;
                if(ret == CrystalAddr.countStepAddr) {
                    ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.owPlayerInputAddr, CrystalAddr.startWildBattleAddr);
                    turnframeEnc = false;
                }
                if(ret == CrystalAddr.startWildBattleAddr) {
                    String turnframeStr = turnframeEnc ? " [turnframe]" : "";
                    writer.println(state.getLogStr() + " " + edge.getAction().logStr() + turnframeStr + "   (" + gb.getEonTimer() + ")");
                    wrap.advanceWithJoypadToAddress(input, CrystalAddr.calcStatsAddr);
                    writer.println("    - DVs: " + getDVs(gb)
                            + " (" + gb.readMemory(0xD219)
                            +  "/" + gb.readMemory(0xD21B)
                            +  "/" + gb.readMemory(0xD21D)
                            +  "/" + gb.readMemory(0xD221)
                            +  "/" + gb.readMemory(0xD223)
                            +  "/" + gb.readMemory(0xD21F)
                            +  ")" + getScoreStr(gb)
                    );
                    writer.println("    - HP = " + getHiddenPower(gb));
                    writer.flush();
                }
                else {
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
                            true,
                            true,
                            false
                    );
                    overworldSearch(newState);
                }
            }
            else if(edge.getAction() == GscAction.START_B) {
                if(!state.canStart()) {
                    continue;
                }
                wrap.injectCrysInput(START);
                wrap.advanceFrame(START);
                wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
                wrap.injectCrysMenuInput(B);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
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
                overworldSearch(newState);
            }
            else if(edge.getAction() == GscAction.SEL) {
                if(!state.canSelect()) {
                    continue;
                }
                wrap.injectCrysInput(SELECT);
                wrap.advanceFrame(SELECT);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                GscState newState = new GscState(
                        state.getLogStr() + " " + edge.getAction().logStr(),
                        !state.onBike(),
                        true,
                        false,
                        state.getFacingDir(),
                        gb.getDivState(),
                        gb.readMemory(0xFFE1),
                        gb.readMemory(0xFFE2),
                        edge.getNextTile().getX(),
                        edge.getNextTile().getY(),
                        false,
                        true,
                        true
                );
                overworldSearch(newState);
            }
            gb.loadState(oldState);
        }
    }

    public static void main(String[] args) throws IOException {
        initGrid();
        String ts = Long.toString(System.currentTimeMillis());
        String fileName = "entei_" + ts + ".txt";
        System.out.println(fileName);
        //writer = new PrintWriter(fileName);
        writer = new PrintWriter(System.out);
        int i = 1;

        /*
        for(i=0; i<60; i++) {
            System.out.println("IGT: " + i);
            System.out.println("--------");
        */

        makeSave(i);
        Gb.loadGambatte(1);
        gb = new Gb(0, false);
        gb.startEmulator("roms/pokecrystal.gbc", false, 2880);
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

        ///*
        int delay = 77;
        int d = delay;
        for (int k = 0; k < d; k++) {
            wrap.advanceFrame();
        }
        //*/

        ByteBuffer state = gb.saveState();

        //for(int d=0; d<=200; d++) {
        for(;d<=delay+2;d++) {

        wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
        System.out.println("[" + d + "] File select A press: " + gb.getEonTimer());
        wrap.advanceFrame(A);
        wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);

        int ret = 0;
        String log = "";

        int x = gb.readMemory(0xDCB8);
        int y = gb.readMemory(0xDCB7);
        System.out.println("" + x + "," + y);

        String entei = getEntei(gb);
        String raikou = getRaikou(gb);
        System.out.println("(1) " + entei + "   (Raikou: " + raikou + ")");

        /*
        //if(raikou.equals("Route 36") || raikou.equals("Route 38") || raikou.equals("Route 42")) {
        if(entei.equals("Route 36") || entei.equals("Route 38") || entei.equals("Route 42")) {
            System.out.println("[" + d + "] " + entei + "    (Raikou: " + raikou + ")");
        }
        */

        ///*
        for (int c = 0; c < 17 - x; c++) {
            wrap.injectCrysInput(RIGHT);
            wrap.advanceWithJoypadToAddress(RIGHT, CrystalAddr.countStepAddr);
            wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
            System.out.println("    - [" + d + "] " + gb.readMemory(0xD507) + " " + gb.readMemory(0xD506)
                    + " " + gb.readMemory(0xD508) + ", " + gb.readMemory(0xD50E)
                    + " " + gb.readMemory(0xD50F));
            log += "R ";
        }

        for (int c = 0; c < 35 - y; c++) {
            wrap.injectCrysInput(DOWN);
            wrap.advanceWithJoypadToAddress(DOWN, CrystalAddr.countStepAddr);
            wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
            System.out.println("    - [" + d + "] " + gb.readMemory(0xD507) + " " + gb.readMemory(0xD506)
                    + " " + gb.readMemory(0xD508) + ", " + gb.readMemory(0xD50E)
                    + " " + gb.readMemory(0xD50F));
            log += "D ";
        }

        if(d==77 || d==79) {
            for (int c = 0; c <= 3; c++) {
                wrap.injectCrysInput(UP);
                wrap.advanceToAddress(CrystalAddr.countStepAddr);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                System.out.println("    - [" + d + "] " + gb.readMemory(0xD507) + " " + gb.readMemory(0xD506)
                        + " " + gb.readMemory(0xD508) + ", " + gb.readMemory(0xD50E)
                        + " " + gb.readMemory(0xD50F));
                log += "U ";
            }

            for (int c = 0; c <= 3; c++) {
                wrap.injectCrysInput(DOWN);
                wrap.advanceToAddress(CrystalAddr.countStepAddr);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                System.out.println("    - [" + d + "] " + gb.readMemory(0xD507) + " " + gb.readMemory(0xD506)
                        + " " + gb.readMemory(0xD508) + ", " + gb.readMemory(0xD50E)
                        + " " + gb.readMemory(0xD50F));
                log += "D ";
            }
        }
        //*/

        wrap.injectCrysInput(SELECT);
        wrap.advanceFrame(SELECT);
        wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
        log += "SEL ";

        ///*
        if(d==79) {
            wrap.injectCrysInput(RIGHT);
            wrap.advanceWithJoypadToAddress(RIGHT, CrystalAddr.countStepAddr);
            wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
            log += "R ";
        }
        //*/

        wrap.injectCrysInput(DOWN);
        wrap.advanceWithJoypadToAddress(DOWN, CrystalAddr.countStepAddr);
        wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
        log += "D ";

        ///*
        String entei2 = getEntei(gb);
        String raikou2 = getRaikou(gb);
        System.out.println("(2) " + entei2 + "   (Raikou: " + raikou2 + ")");
        r37entrance = gb.getGbpTime();
        System.out.println("r37 entrance: " + gb.getEonTimer());
        System.out.println("==================================");
        //*/

        ///*
        if(d==77) {
            for(int c=0; c<2; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<2; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = UP;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = RIGHT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = RIGHT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = LEFT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }
        }
        //*/

        ///*
        if(d==78) {
            for(int c=0; c<3; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = RIGHT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<1; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for(int c=0; c<2; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = LEFT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            wrap.injectCrysInput(START);
            wrap.advanceFrame(START);
            wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
            wrap.injectCrysMenuInput(B);
            wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);

            for(int c=0; c<2; c++) {
                if(ret != CrystalAddr.calcStatsAddr) {
                    int dir = LEFT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }
        }
        //*/

        ///*
        if(d==79) {
            for (int c = 0; c < 1; c++) {
                if (ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for (int c = 0; c < 1; c++) {
                if (ret != CrystalAddr.calcStatsAddr) {
                    int dir = LEFT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for (int c = 0; c < 1; c++) {
                if (ret != CrystalAddr.calcStatsAddr) {
                    int dir = DOWN;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for (int c = 0; c < 1; c++) {
                if (ret != CrystalAddr.calcStatsAddr) {
                    int dir = LEFT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }

            for (int c = 0; c < 2; c++) {
                wrap.injectCrysInput(START);
                wrap.advanceFrame(START);
                wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
                wrap.injectCrysMenuInput(B);
                wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
            }

            for (int c = 0; c < 1; c++) {
                if (ret != CrystalAddr.calcStatsAddr) {
                    int dir = RIGHT;
                    wrap.injectCrysInput(dir);
                    wrap.advanceWithJoypadToAddress(dir, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
                }
            }
        }
        //*/

        /*
        GscState owState = new GscState(
                "[" + delay + "] " + log + "//",
                gb.readMemory(0xD95D) == 1,
                false,
                false,
                DOWN,
                gb.getDivState(),
                gb.readMemory(0xFFE1),
                gb.readMemory(0xFFE2),
                gb.readMemory(0xDCB8),
                gb.readMemory(0xDCB7),
                false,
                false,
                false);
        overworldSearch(owState);
        */

        ///*
        System.out.println("    - DVs: " + getDVs(gb)
                + " (" + gb.readMemory(0xD219)
                +  "/" + gb.readMemory(0xD21B)
                +  "/" + gb.readMemory(0xD21D)
                +  "/" + gb.readMemory(0xD221)
                +  "/" + gb.readMemory(0xD223)
                +  "/" + gb.readMemory(0xD21F)
                +  ")" + getScoreStr(gb)
        );
        System.out.println("    - HP = " + getHiddenPower(gb));
        System.out.println();
        //*/

        ///*
        gb.loadState(state);
        wrap.advanceFrame();
        state = gb.saveState();
        }
        //*/

        /*
        System.out.println();
        System.out.println();
        }
        */

        writer.close();
    }

    private static void initGrid() {
        for (int x = 4; x <= 13; x++) {
            for (int y = 0; y <= 4; y++) {
                GscCoord c = new GscCoord(0x0A04, x, y);
                GscTile tile = new GscTile(c);
                map.put(c, tile);
            }
        }
        for (int y = 0; y <= 0; y++) {
            for (int x = 7; x <= 13; x++) {
                GscTile tile = map.get(0x0A04, x, y);
                GscTile tileDown = map.get(0x0A04, x, y + 1);
                tile.addEdge(0, new GscEdge(GscAction.DOWN, tileDown));
                if (x > 7) {
                    GscTile tileLeft = map.get(0x0A04, x - 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.LEFT, tileLeft));
                }
                if (x < 13) {
                    GscTile tileRight = map.get(0x0A04, x + 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.RIGHT, tileRight));
                }
                tile.addEdge(0, new GscEdge(GscAction.START_B, tile));
                tile.addEdge(0, new GscEdge(GscAction.SEL, tile));
            }
        }
        for (int y = 1; y <= 1; y++) {
            for (int x = 7; x <= 13; x++) {
                GscTile tile = map.get(0x0A04, x, y);
                if (x <= 9 || x >= 12) {
                    GscTile tileDown = map.get(0x0A04, x, y + 1);
                    tile.addEdge(0, new GscEdge(GscAction.DOWN, tileDown));
                }
                if (x > 7) {
                    GscTile tileLeft = map.get(0x0A04, x - 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.LEFT, tileLeft));
                }
                if (x < 13) {
                    GscTile tileRight = map.get(0x0A04, x + 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.RIGHT, tileRight));
                }
                GscTile tileUp = map.get(0x0A04, x, y - 1);
                tile.addEdge(0, new GscEdge(GscAction.UP, tileUp));
                tile.addEdge(0, new GscEdge(GscAction.START_B, tile));
                tile.addEdge(0, new GscEdge(GscAction.SEL, tile));
            }
        }
        for (int y = 2; y <= 4; y++) {
            for (int x = 4; x <= 9; x++) {
                GscTile tile = map.get(0x0A04, x, y);
                if ((y == 2 && x != 5) || (y == 3)) {
                    GscTile tileDown = map.get(0x0A04, x, y + 1);
                    tile.addEdge(0, new GscEdge(GscAction.DOWN, tileDown));
                }
                if ((x != 4) && ((y == 2) || (y == 3 && x != 6) || (y == 4))) {
                    GscTile tileLeft = map.get(0x0A04, x - 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.LEFT, tileLeft));
                }
                if ((x != 9) && ((y == 2) || (y == 3 && x != 4) || (y == 4))) {
                    GscTile tileRight = map.get(0x0A04, x + 1, y);
                    tile.addEdge(0, new GscEdge(GscAction.RIGHT, tileRight));
                }
                if ((y == 2 && x > 6) || (y == 3) || (y == 4 && x != 5)) {
                    GscTile tileUp = map.get(0x0A04, x, y - 1);
                    tile.addEdge(0, new GscEdge(GscAction.UP, tileUp));
                }
                tile.addEdge(0, new GscEdge(GscAction.START_B, tile));
                tile.addEdge(0, new GscEdge(GscAction.SEL, tile));
            }
            if (y == 2) {
                for (int x = 12; x <= 13; x++) {
                    GscTile tile = map.get(0x0A04, x, y);
                    if (x == 13) {
                        GscTile tileLeft = map.get(0x0A04, x - 1, y);
                        tile.addEdge(0, new GscEdge(GscAction.LEFT, tileLeft));
                    }
                    if (x == 12) {
                        GscTile tileRight = map.get(0x0A04, x + 1, y);
                        tile.addEdge(0, new GscEdge(GscAction.RIGHT, tileRight));
                    }
                    GscTile tileUp = map.get(0x0A04, x, y - 1);
                    tile.addEdge(0, new GscEdge(GscAction.UP, tileUp));
                    tile.addEdge(0, new GscEdge(GscAction.START_B, tile));
                    tile.addEdge(0, new GscEdge(GscAction.SEL, tile));
                }
            }
        }
    }

    private static String getRandom(Gb gb) {
        int hRandom = (gb.readMemory(0xFFE1) << 8) | gb.readMemory(0xFFE2);
        return String.format("0x%4s", Integer.toHexString(hRandom).toUpperCase()).replace(' ', '0');
    }

    private static String getRoamMap(int mapGrp, int mapNum) {
        String str = String.format("%2s", Integer.toHexString(mapGrp)).toUpperCase().replace(' ', '0');
        str += " " + String.format("%2s", Integer.toHexString(mapNum)).toUpperCase().replace(' ', '0');
        if(str.equals("01 0C")) { str = "Route 38"; }
        if(str.equals("0A 03")) { str = "Route 36"; }
        if(str.equals("02 05")) { str = "Route 42"; }
        if(str.equals("02 06")) { str = "Route 44"; }
        if(str.equals("1A 02")) { str = "Route 31"; }
        if(str.equals("18 03")) { str = "Route 29"; }
        if(str.equals("05 09")) { str = "Route 46"; }
        if(str.equals("0A 01")) { str = "Route 32"; }
        if(str.equals("0B 01")) { str = "Route 34"; }
        if(str.equals("05 08")) { str = "Route 45"; }
        if(str.equals("0A 02")) { str = "Route 35"; }
        if(str.equals("08 06")) { str = "Route 33"; }
        if(str.equals("0A 04")) { str = "Route 37"; }
        if(str.equals("1A 01")) { str = "Route 30"; }
        if(str.equals("09 05")) { str = "Route 43"; }
        if(str.equals("01 0D")) { str = "Route 39"; }
        return str;
    }

    private static String getRaikou(Gb gb) {
        return getRoamMap(gb.readMemory(0xDFD1), gb.readMemory(0xDFD2));
    }

    private static String getEntei(Gb gb) {
        return getRoamMap(gb.readMemory(0xDFD8), gb.readMemory(0xDFD9));
    }

    private static String getDVs(Gb gb) {
        int dvs = (gb.readMemory(0xD20C) << 8) | gb.readMemory(0xD20D);
        return String.format("0x%4s", Integer.toHexString(dvs).toUpperCase()).replace(' ', '0');
    }

    private static boolean avoidsEspeed(int def, int spc) {
        return (spc<=13 && def>=13) || (spc<=11 && def>=10) || (spc<=7 && def>=7)
            || (spc<=5  && def>=4)  || (spc<=3  && def>=2)  || (spc==0);
    }

    private static int scoreEntei(Gb gb) {
        int score = 0;
        int atk = gb.readMemory(0xD20C) / 16;
        int def = gb.readMemory(0xD20C) % 16;
        int spd = gb.readMemory(0xD20D) / 16;
        int spc = gb.readMemory(0xD20D) % 16;
    /*
     *      |    +2 |    +1 |    ±0 |    -1 |    -2
     *  -------------------------------------------
     *  Atk |    15 |    14 | 12-13 | 10-11 |  0- 9
     *  Spd |       | 14-15 |  0-13 |       |
     *  Spc | 14-15 | 12-13 |  9-11 |  6- 8 |  0- 5
     *
     *  Avoids Extremespeed: +1
     */
        if(atk==15) { score += 2; }
        if(atk==14) { score += 1; }
        if(atk<=11) { score -= 1; }
        if(atk<= 9) { score -= 1; }
        if(spd>=14) { score += 1; }
        if(spc>=12) { score += 1; }
        if(spc>=14) { score += 1; }
        if(spc<= 8) { score -= 1; }
        if(spc<= 5) { score -= 1; }
        if(avoidsEspeed(def,spc)) { score += 1; }
        return score;
    }

    private static String getScoreStr(Gb gb) {
        String str = "";
        int score = scoreEntei(gb);
        if(score >= 5) { str += " [***]"; }
        if(score == 4) { str += " [ **]"; }
        if(score == 3) { str += " [  *]"; }
        return str;
    }

    private static String getHiddenPower(Gb gb) {
        String type;
        int atkDv = gb.readMemory(0xD20C) / 16;
        int defDv = gb.readMemory(0xD20C) % 16;
        int spdDv = gb.readMemory(0xD20D) / 16;
        int spcDv = gb.readMemory(0xD20D) % 16;
        int idx = 4*(atkDv%4) + (defDv%4);
        if     (idx ==  0) { type = "Fighting"; }
        else if(idx ==  1) { type = "Flying";   }
        else if(idx ==  2) { type = "Poison";   }
        else if(idx ==  3) { type = "Ground";   }
        else if(idx ==  4) { type = "Rock";     }
        else if(idx ==  5) { type = "Bug";      }
        else if(idx ==  6) { type = "Ghost";    }
        else if(idx ==  7) { type = "Steel";    }
        else if(idx ==  8) { type = "Fire";     }
        else if(idx ==  9) { type = "Water";    }
        else if(idx == 10) { type = "Grass";    }
        else if(idx == 11) { type = "Electric"; }
        else if(idx == 12) { type = "Psychic";  }
        else if(idx == 13) { type = "Ice";      }
        else if(idx == 14) { type = "Dragon";   }
        else               { type = "Dark";     } // (idx == 15)
        int atkHP = (atkDv >>> 3) << 3;
        int defHP = (defDv >>> 3) << 2;
        int spdHP = (spdDv >>> 3) << 1;
        int spcHP = (spcDv >>> 3);
        int power = 31+(5*(spcHP+spdHP+defHP+atkHP)+(spcDv%4))/2;
        return "" + type + " " + power;
    }
}