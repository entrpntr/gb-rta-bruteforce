package dabomstew.rta.entei;

import dabomstew.rta.CrystalAddr;
import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import mrwint.gbtasgen.Gb;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class Miltank {
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
    private static PrintWriter writer;
    private static GscTileMap map = new GscTileMap();
    private static HashSet<GscState> seenStates = new HashSet<>();

    private static void overworldSearch(GscState state) {
        //System.out.println(state.getLogStr());

        if(!seenStates.add(state)) {
            return;
        }

        // figure out the earliest time we can get an encounter (depends on cooldown)
        int cooldown = gb.readMemory(0xD452);
        double gbpTime = gb.getGbpTime();
        double adjGbpTime = (cooldown==0) ? gbpTime : gbpTime + 16.0*((double)(cooldown-1))/59.7275;

        // subtract 62 frames if we saved on the bike (getting off bike wastes the 62 frames pre-save)
        if(state.onBike()) {
            adjGbpTime -= 1.038;
        }

        // cut off if earliest possible encounter is too late or if stepcount exceeds 7
        if((adjGbpTime > 18.25 || gb.readMemory(0xDC73) > 7)) {
            return;
        }

        ByteBuffer oldState = gb.saveState();
        GscTile tile = map.get(0x010D, state.getX(), state.getY());

        for(GscEdge edge : tile.getEdges().get(0)) {
            if(GscAction.isDpad(edge.getAction())) {
                int input = 16 * (int) (Math.pow(2.0, (edge.getAction().ordinal())));
                wrap.injectCrysInput(input);
                int ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                boolean turnframeEnc = true;
                if(ret == CrystalAddr.countStepAddr) {
                    ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.owPlayerInputAddr, CrystalAddr.startWildBattleAddr);
                    turnframeEnc = false;
                }
                if(ret == CrystalAddr.startWildBattleAddr) {
                    wrap.advanceToAddress(CrystalAddr.calcStatsAddr);
                    if(gb.readMemory(0xD206) == 241) {
                        String turnframeStr = turnframeEnc ? " [turnframe]" : "";
                        writer.println(state.getLogStr() + " " + edge.getAction().logStr() + turnframeStr);
                        writer.print("    - DVs = " + getDVs()
                                + " (" + gb.readMemory(0xD219)
                                + "/" + gb.readMemory(0xD21B)
                                + "/" + gb.readMemory(0xD21D)
                                + "/" + gb.readMemory(0xD221)
                                + "/" + gb.readMemory(0xD223)
                                + "/" + gb.readMemory(0xD21F) + ")"
                        );
                        writer.println("  --  Item: " + gb.readMemory(0xD207) + "  --  calcStats: " + gb.getEonTimer());
                        writer.flush();
                    }
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
                int ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                String logStr = state.getLogStr() + " SEL";

                // hold down after getting on bike until encounter or out of grass (simple enough increase in search space)
                while(gb.readMemory(0xDCB7) < 27 && ret == CrystalAddr.owPlayerInputAddr) {
                    int input = DOWN;
                    wrap.injectCrysInput(input);
                    ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.countStepAddr, CrystalAddr.startWildBattleAddr);
                    logStr += " D";
                    boolean turnframeEnc = true;
                    if (ret == CrystalAddr.countStepAddr) {
                        ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.owPlayerInputAddr, CrystalAddr.startWildBattleAddr);
                        turnframeEnc = false;
                    }
                    if (ret == CrystalAddr.startWildBattleAddr) {
                        wrap.advanceToAddress(CrystalAddr.calcStatsAddr);
                        if (gb.readMemory(0xD206) == 241) {
                            String turnframeStr = turnframeEnc ? " [turnframe]" : "";
                            writer.println(logStr + turnframeStr);
                            writer.print("    - DVs = " + getDVs()
                                    + " (" + gb.readMemory(0xD219)
                                    + "/" + gb.readMemory(0xD21B)
                                    + "/" + gb.readMemory(0xD21D)
                                    + "/" + gb.readMemory(0xD221)
                                    + "/" + gb.readMemory(0xD223)
                                    + "/" + gb.readMemory(0xD21F) + ")"
                            );
                            writer.println("  --  Item: " + gb.readMemory(0xD207) + "  --  calcStats: " + gb.getEonTimer());
                            writer.flush();
                        }
                    }
                }
            }
            gb.loadState(oldState);
        }
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
            ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.countStepAddr, CrystalAddr.calcStatsAddr,
                    CrystalAddr.printLetterDelayAddr, CrystalAddr.bonkSoundAddr);

            if(ret == CrystalAddr.countStepAddr) {
                ret = wrap.advanceWithJoypadToAddress(input, CrystalAddr.owPlayerInputAddr, CrystalAddr.calcStatsAddr);
            }
        }
        else if(action == GscAction.SEL) {
            wrap.injectCrysInput(SELECT);
            wrap.advanceFrame(SELECT);
            ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
        }
        else if(action == GscAction.START_B) {
            wrap.injectCrysInput(START);
            wrap.advanceFrame(START);
            wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
            wrap.injectCrysMenuInput(B);
            ret = wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
        }
        return ret;
    }

    private static void executePath(String path, int delay, int f) {
        String[] actions = path.split(" ");
        wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
        wrap.advanceFrame(A);
        wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
        String log = "[" + delay + "] ";
        int ret = CrystalAddr.owPlayerInputAddr;
        for (String action : actions) {
            log += action + " ";
            GscAction owAction = GscAction.fromString(action);
            ret = executeAction(ret, owAction);
        }
        if(ret != CrystalAddr.calcStatsAddr) {
            System.out.println("[" + f + "] NO ENCOUNTER" );
        } else {
            System.out.println("f" + f + " = " + log + ": DVs = " + getDVs()
                    + " (" + gb.readMemory(0xD219)
                    + "/" + gb.readMemory(0xD21B)
                    + "/" + gb.readMemory(0xD21D)
                    + "/" + gb.readMemory(0xD221)
                    + "/" + gb.readMemory(0xD223)
                    + "/" + gb.readMemory(0xD21F) + "), Item = " + gb.readMemory(0xD207)
            );
        }
    }

    private static void igtCheck(int delay, String path) throws IOException {
        for(int f=0; f<60; f++) {
            makeSave(4, 0, 59, 0, true, f);
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/pokecrystal_dvcheck.gbc", false, 3200);
            GBMemory mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);
            wrap.advanceToAddress(0x100);
            wrap.advanceWithJoypadToAddress(START, CrystalAddr.titleScreenAddr);
            wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
            wrap.advanceFrame(START);

            // backout to title screen
            //wrap.advanceWithJoypadToAddress(B, CrystalAddr.readJoypadAddr);
            //wrap.advanceFrame(B);
            //wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
            //wrap.advanceFrame(START);

            wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
            wrap.advanceFrame(A);
            wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
            wrap.advanceFrame();
            for (int dd = 0; dd < delay; dd++) {
                wrap.advanceFrame();
            }

            executePath(path, delay, f);
        }
    }

    private static void search() throws IOException {
        initGrid();
        String ts = Long.toString(System.currentTimeMillis());
        String fileName = "cbt_miltank_" + ts + ".txt";
        System.out.println(fileName);
        writer = new PrintWriter(fileName);
        //writer = new PrintWriter(System.out);

        int[] partySizes = {4};
        for (int partySize : partySizes) {
            for (int gender = 0; gender <= 1; gender++) {
                for (int sm = 59; sm >= 0; sm -= 59) {
                    for (int psc = 0; psc < 4; psc += 2) {
                        for (int bike = 0; bike <= 1; bike++) {
                            makeSave(partySize, gender, sm, psc, (bike == 1), 0);
                            Gb.loadGambatte(1);
                            gb = new Gb(0, false);
                            gb.startEmulator("roms/pokecrystal_dvcheck.gbc", false, 3200);
                            GBMemory mem = new GBMemory(gb);
                            wrap = new GBWrapper(gb, mem);
                            wrap.advanceToAddress(0x100);
                            wrap.advanceWithJoypadToAddress(START, CrystalAddr.titleScreenAddr);
                            wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
                            wrap.advanceFrame(START);
                            int back = 0;
                            ByteBuffer backoutState = gb.saveState();
                            for (; back <= 2; back++) {
                                wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
                                wrap.advanceFrame(A);
                                wrap.advanceToAddress(CrystalAddr.readJoypadAddr);
                                wrap.advanceFrame();
                                ByteBuffer state = gb.saveState();
                                for (int d = 0; d <= 240 && gb.getGbpTime() < 15.75; d++) {
                                    String offset = gb.getEonTimer();
                                    wrap.advanceWithJoypadToAddress(A, CrystalAddr.readJoypadAddr);
                                    wrap.advanceFrame(A);
                                    wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                                    String log = "partysize(" + partySize + "), gender(" + gender + "), startmin(" + sm + "), psc(" + psc + "), bike(" + bike + "), backout(" + back + "), delay(" + d + ")";
                                    log += "  (offset: " + offset + ", frame: " + gb.readMemory(0xFFF0) + ")\n    - Path:";

                                    // if we start out on the bike, force the manip to start with unbiking
                                    if (bike == 1) {
                                        wrap.injectCrysInput(SELECT);
                                        wrap.advanceFrame(SELECT);
                                        wrap.advanceToAddress(CrystalAddr.owPlayerInputAddr);
                                        log += " SEL";
                                    }

                                    GscState owState = new GscState(
                                            log,
                                            (bike == 1),
                                            false,
                                            false,
                                            UP,
                                            gb.getDivState(),
                                            gb.readMemory(0xFFE1),
                                            gb.readMemory(0xFFE2),
                                            gb.readMemory(0xDCB8),
                                            gb.readMemory(0xDCB7),
                                            false,
                                            true,
                                            true);
                                    overworldSearch(owState);
                                    seenStates.clear();
                                    gb.loadState(state);
                                    wrap.advanceFrame();
                                    state = gb.saveState();
                                }
                                gb.loadState(backoutState);
                                wrap.advanceWithJoypadToAddress(B, CrystalAddr.readJoypadAddr);
                                wrap.advanceFrame(B);
                                wrap.advanceWithJoypadToAddress(START, CrystalAddr.readJoypadAddr);
                                wrap.advanceFrame(START);
                                backoutState = gb.saveState();
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    public static void makeSave(int partySize, int gender, int startmin, int psc, boolean biking, int frame) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/cbt_cow_test.sav");

        baseSave[0x3E3D] = (byte) gender;
        baseSave[0x206A] = (byte) gender;

        baseSave[0x2055] = (byte) 0;
        baseSave[0x2056] = (byte) frame; // igt frame

        baseSave[0x2801] = (byte) 0;     // stepcount
        baseSave[0x2802] = (byte) psc;

        int startHour = (startmin == 0) ? 17 : 16;
        baseSave[0x2045] = (byte) startHour; // StartHour
        baseSave[0x2046] = (byte) startmin; // StartMinute
        baseSave[0x2047] = (byte) 0;  // StartSecond

        baseSave[0x24EB] = (byte) (biking ? 1 : 0);

        if(partySize == 3) {
            baseSave[0x2865] = (byte) 3;
            baseSave[0x2869] = (byte) 0xFF;
        }

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
        FileFunctions.writeBytesToFile("roms/pokecrystal_dvcheck.sav", baseSave);
    }

    public static void main(String[] args) throws IOException {
        igtCheck(85, "SEL L R S_B S_B L R SEL D");
        //search();
    }

    private static void initGrid() {
        for (int x = 4; x <= 5; x++) {
            for (int y = 22; y <= 24; y++) {
                if (x != 4 || y != 22) {
                    GscCoord c = new GscCoord(0x010D, x, y);
                    GscTile tile = new GscTile(c);
                    map.put(c, tile);
                }
            }
        }
        for (int x = 4; x <= 5; x++) {
            for (int y = 22; y <= 24; y++) {
                if (x != 4 || y != 22) {
                    GscTile tile = map.get(0x010D, x, y);
                    GscTile tileDown = map.get(0x010D, x, y + 1);
                    GscTile tileLeft = map.get(0x010D, x - 1, y);
                    GscTile tileUp = map.get(0x010D, x, y - 1);
                    GscTile tileRight = map.get(0x010D, x + 1, y);
                    if (y < 24) {
                        tile.addEdge(0, new GscEdge(GscAction.DOWN, tileDown));
                    }
                    if (x == 4) {
                        tile.addEdge(0, new GscEdge(GscAction.RIGHT, tileRight));
                    }
                    if (x == 5 && y != 22) {
                        tile.addEdge(0, new GscEdge(GscAction.LEFT, tileLeft));
                    }
                    if (y == 24 || (x == 5 && y == 23)) {
                        tile.addEdge(0, new GscEdge(GscAction.UP, tileUp));
                    }
                    tile.addEdge(0, new GscEdge(GscAction.START_B, tile));
                    tile.addEdge(0, new GscEdge(GscAction.SEL, tile));
                }
            }
        }
    }

    private static String getDVs() {
        int dvs = (gb.readMemory(0xD20C) << 8) | gb.readMemory(0xD20D);
        return String.format("0x%4s", Integer.toHexString(dvs).toUpperCase()).replace(' ', '0');
    }
}
