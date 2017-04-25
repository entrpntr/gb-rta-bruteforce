package dabomstew.rta.ffef;

import dabomstew.rta.*;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.*;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
//import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;

public class NidoBotFFEF {
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

    private static final String gameName;
    private static PrintWriter writer;

    // Graph is only built right now to waste 10 steps, so more would need to be added to make use of >= 204 frames.
    // Also more intros would need to be spelled out.
    private static final int MAX_COST;
    static {
        int RED_COST = 157;
        gameName = "red";
        MAX_COST = (gameName.equals("red")) ? RED_COST : RED_COST - 21;
    }

    // TODO: LOOK AT TUNING THESE MORE
    private static final double DSUM_HIGH_COEF = 0.6969;
    private static final double DSUM_LOW_COEF = 0.6464;
    private static final double DSUM_MARGIN_OF_ERROR = 5.0;

    private static LongArrayList seenStates = new LongArrayList(100000);

    // Sort tiles by starting cost (lower starting cost takes priority),
    // then by starting distance from grass (longer distance takes priority).
    //
    // The idea being that if you get to a state that's already been reached, it has been from a state that has wasted
    // fewer frames to get to that point.
    static class SaveTileComparator implements Comparator<SaveTile> {
        @Override public int compare(SaveTile o1, SaveTile o2) {
            if(o1.getStartCost() != o2.getStartCost()) {
                return o1.getTrueStartCost() - o2.getTrueStartCost();
            } else {
                return o2.getOwPos().getMinStepsToGrass() - o1.getOwPos().getMinStepsToGrass();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!new File("roms").exists()) {
            new File("roms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("roms/poke" + gameName + ".gbc").exists()) {
            System.err.println("Could not find poke" + gameName + ".gbc in roms directory!");
            System.exit(0);
        }

        String ts = Long.toString(System.currentTimeMillis());
        File file = new File(gameName + "_ffef_encounters_" + ts + ".txt");
        writer = new PrintWriter(file);

        // TODO: Programmatically add intros for manips with higher cost caps
        List<IntroSequence> introSequences = new ArrayList<>();
        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(cheatpal, gfSkip, nido0, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, backout, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, backout, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, backout, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, backout, cont, cont));
        introSequences.add(new IntroSequence(cheatpal,gfSkip,nido0,title0,cont,backout,cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(cheatpal, gfSkip, nido1, title0, cont, cont));

        initTiles();

        //boolean startProcessing = false;
        Collections.sort(saveTiles, new SaveTileComparator());
        for(SaveTile saveTile : saveTiles) {
            // Comment these lines out if you want to search the whole space
            //if(saveTile.getOwPos().getMinStepsToGrass() < 35 || saveTile.getOwPos().getMinStepsToGrass() > 40 ) {
            if(saveTile.getOwPos().getMinStepsToGrass() > 22 ) {
                continue;
            }
/*
            if(saveTile.getOwPos().getX()==18 && saveTile.getOwPos().getY()==20) {
                startProcessing = true;
            }

            if(!startProcessing) {continue;}*/

            OverworldTile savePos = saveTile.getOwPos();
            makeSave(savePos.getMap(), savePos.getX(), savePos.getY());

            // Init gambatte with 1 screen
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/poke" + gameName + ".gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            for (int i=0; i<introSequences.size(); i++) {
                //if((i<6) && saveTile.getOwPos().getMap() == 1 && saveTile.getOwPos().getX() == 18 && saveTile.getOwPos().getY() == 20) {
                //    continue;
                //}
                IntroSequence intro = introSequences.get(i);
                int baseCost = saveTile.getStartCost() + intro.cost();
                if (baseCost <= MAX_COST) {
                    intro.execute(wrap);
                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    int hra = mem.getHRA();
                    int hrs = mem.getHRS();
                    int dsum = (hra + hrs) % 256;
                    String header = "------  [" + savePos.getMap() + "#" + savePos.getX() + "," + savePos.getY() + "] " + intro.toString() + " | baseDsum: " + dsum + "; baseCost: " + baseCost + "  ------";
                    System.out.println(header);
                    //writer.println(header);
                    OverworldState owState = new OverworldState(savePos.toString() + " - " + intro.toString() + ":",
                            saveTile.getOwPos(), 1, true, gb.getDivState(), mem.getHRA(), mem.getHRS(),
                            saveTile.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(), baseCost, 0);
                    overworldSearch(owState);
                    gb.step(HARD_RESET);
                }
            }
        }
        writer.close();
    }

//  private static void overworldSearch(OverworldState ow, boolean dsumPrune) {
    private static void overworldSearch(OverworldState ow) {
        if(ow.getWastedFrames() > MAX_COST) {
            return;
        }
/*
        if(!seenStates.add(ow.getFfefUniqId())) {
            return;
        }
*/
///*
        long ffefUid = ow.getFfefUniqId();
        int idx = seenStates.binarySearch(ffefUid);
        if(idx >= 0) {
            return;
        }
        seenStates.addAtIndex(-1*idx-1, ffefUid);
//*/
        if(seenStates.size() % 100000 == 0) {
            System.out.println("# seen states: " + seenStates.size());
        }

        if(ow.getMap() == 33 && ow.getX()==33 && ow.getY()==11 && ow.getWastedFrames()+34>MAX_COST) {
            return;
        }

        if(inferDsum(ow)) {
            return;
        }

        ByteBuffer curSave = gb.saveState();

        for(OverworldEdge edge : ow.getPos().getEdgeList()) {
            OverworldAction edgeAction = edge.getAction();
            if (ow.getMap() == 1 && ow.getX() == 7 && edgeAction == OverworldAction.RIGHT && ow.isViridianNpc()) {
                continue;
            }
            if (ow.aPressCounter() == 2 && (edgeAction == OverworldAction.START_B
                    || edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
                continue;
            }
            if (ow.aPressCounter() > 0 && edgeAction == OverworldAction.A) {
                continue;
            }
            if (!ow.canPressStart() && (edgeAction == OverworldAction.START_B || edgeAction == OverworldAction.S_A_B_S
                    || edgeAction == OverworldAction.S_A_B_A_B_S)) {
                continue;
            }
            int edgeCost = edge.getCost();
            if (ow.getWastedFrames() + edgeCost > MAX_COST) {
                continue;
            }

            int lowFrames = edge.getFrames() +
                    ((OverworldAction.isDpad(edgeAction) && edge.getNextPos().isEncounterTile()) ? 0 : edge.getNextPos().getMinStepsToGrass() * 17);
            int highFrames = edge.getFrames() + 17 * edge.getNextPos().getMinStepsToGrass() + 17 * effectiveWastableSteps(edge.getNextPos()) +
                    MAX_COST - ow.getWastedFrames() - edgeCost;
            double lowDsumChange = ((double)lowFrames)*DSUM_LOW_COEF;
            double highDsumChange = ((double)highFrames)*DSUM_HIGH_COEF;
            double predictLow = ((double)ow.getDsum())+2048.0-lowDsumChange+DSUM_MARGIN_OF_ERROR;
            double predictHigh = ((double)ow.getDsum())+2048.0-highDsumChange-DSUM_MARGIN_OF_ERROR;

            //if(inferDsumDebug(predictHigh, predictLow)) {
            if(inferDsum(predictHigh, predictLow)) {
                continue;
            }

            int initIGT = readIGT();
            int wastedFrames;
            int res;
            OverworldState newState;
            switch (edgeAction) {
                case LEFT:
                case UP:
                case RIGHT:
                case DOWN:
                    int input = 16 * (int) (Math.pow(2.0, (edgeAction.ordinal())));
                    wrap.injectRBInput(input);
                    wrap.advanceWithJoypadToAddress(input, RedBlueAddr.newBattleAddr);
                    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr, RedBlueAddr.joypadOverworldAddr);
                    if (res == RedBlueAddr.encounterTestAddr) {
                        if (mem.getHRA() >= 0 && mem.getHRA() <= 24) {
                            String rngAtEnc = mem.getRNGStateWithDsum();
                            wrap.advanceFrame();
                            wrap.advanceFrame();
                            Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                    mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                            int owFrames = ow.getOverworldFrames() + edge.getFrames();
                          //  String pruneDsum = dsumPrune ? " [*]" : "";
                            String defaultYbf = "";
                            if(enc.species == 3 && enc.level == 4 && (enc.dvs == 0xFFEF || enc.dvs == 0xFFEE)) {
                                wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                wrap.injectRBInput(A);
                                wrap.advanceFrame();
                                wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                wrap.injectRBInput(DOWN | A);
                                wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                wrap.injectRBInput(A | RIGHT);
                                int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                if(res2 == RedBlueAddr.catchSuccessAddr) {
                                    defaultYbf = ", default ybf: [*]";
                                } else {
                                    defaultYbf = ", default ybf: [ ]";
                                }
                            }
                            writer.println(
                                    ow.toString() + " " + edgeAction.logStr() + ", " +
                                            String.format(
                                                    "species %d lv%d DVs %04X rng %s encrng %s",
                                                    enc.species, enc.level, enc.dvs, enc.battleRNG, rngAtEnc
                                            ) + ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames) + defaultYbf
              //                              + pruneDsum
                            );
                            writer.flush();
                        } else {
                            res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                        }
                    }
                    if (res == RedBlueAddr.joypadOverworldAddr) {
                        int waste = ow.getWastedFrames() + edgeCost;
                        while (mem.getMap() == ow.getMap() && mem.getX() == ow.getX() && mem.getY() == ow.getY() && waste <= MAX_COST) {
                            waste += 2;
                            wrap.injectRBInput(input);
                            wrap.advanceFrame(input);
                            wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                        }
                        // Ledgejumps still call joypad on ledge tile
                        if(edge.getFrames() == 40) {
                            wrap.injectRBInput(input);
                            wrap.advanceFrame(input);
                            wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                        }
                        int igt = readIGT();
                        int extraWastedFrames = igt - initIGT - edge.getFrames();
                        boolean newViridianNPC = (ow.getMap() == 1 && ow.getX() == 0 && input == LEFT);
                        //String pruneDsum = prune ? "[*]" : "";
                        newState = new OverworldState(ow.toString() + " " + edgeAction.logStr()
                               // + pruneDsum
                                , edge.getNextPos(), Math.max(0, ow.aPressCounter() - 1), true, gb.getDivState(), mem.getHRA(), mem.getHRS(),
                                ow.isViridianNpc() || newViridianNPC, mem.getTurnFrameStatus(), mem.getNPCTimers(),
                                ow.getWastedFrames() + edgeCost + extraWastedFrames,
                                ow.getOverworldFrames() + edge.getFrames() + extraWastedFrames);
                        //overworldSearch(newState, prune || dsumPrune);
                        overworldSearch(newState);
                    }

                    break;
                case A:
                    wrap.injectRBInput(A);
                    wrap.advanceFrame(A);
                    res = wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadOverworldAddr, RedBlueAddr.printLetterDelayAddr);
                    wastedFrames = readIGT() - initIGT;
                    if (res == RedBlueAddr.joypadOverworldAddr) {
                        newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 2,
                                true, gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(),
                                mem.getNPCTimers(), ow.getWastedFrames() + wastedFrames, ow.getOverworldFrames() + wastedFrames);
                        //overworldSearch(newState, prune || dsumPrune);
                        overworldSearch(newState);
                    }
                    break;
                case START_B:
                    wrap.injectRBInput(START);
                    wrap.advanceFrame(START);
                    wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadOverworldAddr);
                    wastedFrames = readIGT() - initIGT;

                    newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0,
                            true, gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(),
                            mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + wastedFrames,
                            ow.getOverworldFrames() + wastedFrames);
                    //overworldSearch(newState, prune || dsumPrune);
                    overworldSearch(newState);
                    break;
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

                    wastedFrames = readIGT() - initIGT;

                    newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0,
                            false, gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(),
                            ow.getWastedFrames() + wastedFrames, ow.getOverworldFrames() + wastedFrames);
                    //overworldSearch(newState, prune || dsumPrune);
                    overworldSearch(newState);
                    break;
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
                    wastedFrames = readIGT() - initIGT;

                    newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0, false,
                            gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(),
                            ow.getWastedFrames() + wastedFrames, ow.getOverworldFrames() + wastedFrames);
                    //overworldSearch(newState, prune || dsumPrune);
                    overworldSearch(newState);
                    break;
                default:
                    break;
            }
            gb.loadState(curSave);
        }
    }

    //  Need this, otherwise dsum inference gets confused about how much longer the manip can take
    private static int effectiveWastableSteps(OverworldTile pos) {
        int effWastableSteps = 0;
        if(pos.getMap()==33 && pos.getX() >= 30 && pos.getX() <= 33 && pos.getY() >= 8 && pos.getY() <= 12) {
            effWastableSteps = Math.abs(pos.getX() - 33) + Math.abs(pos.getY() - 11) - 1;
        }
        return effWastableSteps;
    }

    private static boolean inferDsumDebug(OverworldState ow, OverworldEdge edge, double predictHigh, double predictLow) {
        if(Math.abs(predictHigh-predictLow)>=256.0) {
            return false;
        } else {
            while(predictHigh>=256.0) {
                predictHigh-=256.0;
                predictLow-=256.0;
            }
            if(gameName.equals("red")) {
                if (predictLow <= 141.0 || (predictHigh >= 170.0 && (predictLow <= 256.0 || predictLow % 256.0 <= 141.0))) {
                    writer.println("PRUNED: " + ow.toString() + " [" + edge.getAction().logStr() + " -> " + edge.getNextPos().getX() + "," + edge.getNextPos().getY() + "]"
                    + " -- High: " + String.format("%.3f", predictHigh) + ", Low: " + String.format("%.3f", (predictLow % 256.0)));
                    writer.flush();
                    return true;
                }
            } else if(gameName.equals("blue")) {
                if (predictLow <= 253.0 && predictHigh >= 4.0) {
                    writer.println("PRUNED: " + ow.toString() + " [" + edge.getAction().logStr() + " -> " + edge.getNextPos().getX() + "," + edge.getNextPos().getY() + "]"
                    + " -- High: " + String.format("%.3f", predictHigh) + ", Low: " + String.format("%.3f", (predictLow % 256.0)));
                    writer.flush();
                    return true;
                }
            }
            return false;
        }
    }

    // returns true if should prune
    private static boolean inferDsum(double predictHigh, double predictLow) {
        if(Math.abs(predictHigh-predictLow)>=256.0) {
            return false;
        } else {
            while(predictHigh>=256.0) {
                predictHigh-=256.0;
                predictLow-=256.0;
            }
            if(gameName.equals("red")) {
                if (predictLow < 141.0 || (predictHigh > 170.0 && (predictLow < 256.0 || predictLow % 256.0 < 141.0))) {
                    return true;
                }
            } else if(gameName.equals("blue")) {
                if (predictLow < 253.0 && predictHigh > 4.0) {
                    return true;
                }
            }
            return false;
        }
    }

    // returns true if should prune
    private static boolean inferDsum(OverworldState ow) {
        int minSteps = ow.getPos().getMinStepsToGrass();
        double dsum = (double)(2048+ow.getHra()+ow.getHrs());
        int effWastableSteps = effectiveWastableSteps(ow.getPos());
        double lowFrames = ((double)(minSteps))*17.0;
        double highFrames = 17.0*((double)(minSteps+effWastableSteps))+MAX_COST-ow.getWastedFrames();
        double predictLow = dsum-lowFrames*DSUM_LOW_COEF+DSUM_MARGIN_OF_ERROR;
        double predictHigh = dsum-highFrames*DSUM_HIGH_COEF-DSUM_MARGIN_OF_ERROR;
        return inferDsum(predictHigh, predictLow);
    }

/*
    private static int readX() {
        return gb.readMemory(0xD362);
    }
    private static int readY() {
        return gb.readMemory(0xD361);
    }
    private static int readMap() {
        return gb.readMemory(0xD35E);
    }
    private static int readHRA() {
        return gb.readMemory(0xFFD3);
    }
    private static int readHRS() {
        return gb.readMemory(0xFFD4);
    }
*/

    private static int readIGT() {
        return 3600*gb.readMemory(0xDA43) + 60*gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
    }

    public static void makeSave(int map, int x, int y) throws IOException {
        String prefix = (map==1) ? "viridian" : "r22";
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/" + prefix + "_ffef_" + gameName + ".sav");
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
        baseSave[0x2CEF] = (byte) 6;
        baseSave[0x2CF0] = (byte) 9;
        baseSave[0x2CF1] = (byte) 12;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("roms/poke" + gameName + ".sav", baseSave);
    }

    static class Strat {
        String name;
        int cost;
        Integer[] addr;
        Integer[] input;
        Integer[] advanceFrames;
        Strat(String name, int cost, Integer[] addr, Integer[] input, Integer[] advanceFrames) {
            this.addr = addr;
            this.cost = cost;
            this.name = name;
            this.input = input;
            this.advanceFrames = advanceFrames;
        }
        public void execute(GBWrapper wrap) {
            for(int i=0; i<addr.length; i++) {
                wrap.advanceToAddress(addr[i]);
                wrap.injectRBInput(input[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame();
                }
            }
        }
    }

    private static class PalStrat extends Strat {
        PalStrat(String name, int cost, Integer[] addr, Integer[] input, Integer[] advanceFrames) {
            super(name, cost, addr, input, advanceFrames);
        }
        @Override public void execute(GBWrapper wrap) {
            for (int i = 0; i < addr.length; i++) {
                wrap.advanceWithJoypadToAddress(input[i], addr[i]);
                wrap.advanceFrame(input[i]);
                for (int j = 0; j < advanceFrames[i]; j++) {
                    wrap.advanceFrame(input[i]);
                }
            }
        }
    }

    static class IntroSequence extends ArrayList<Strat> implements Comparable<IntroSequence> {
        IntroSequence(Strat... strats) {
            super(Arrays.asList(strats));
        }
        IntroSequence(IntroSequence other) {
            super(other);
        }
        @Override public String toString() {
            String ret = gameName;
            for(Strat s : this) {
                ret += s.name;
            }
            return ret;
        }
        void execute(GBWrapper wrap) {
            for(Strat s : this) {
                s.execute(wrap);
            }
        }
        int cost() {
            return this.stream().mapToInt((Strat s) -> s.cost).sum();
        }
        @Override public int compareTo(IntroSequence o) {
            return this.cost() - o.cost();
        }
    }

    private static PalStrat pal = new PalStrat("_pal", 0,
            new Integer[] {RedBlueAddr.biosReadKeypadAddr},
            new Integer[] {UP},
            new Integer[] {1});
    private static PalStrat nopal = new PalStrat("_nopal", 0,
            new Integer[] {RedBlueAddr.biosReadKeypadAddr},
            new Integer[] {NO_INPUT},
            new Integer[] {1});
    private static PalStrat abss = new PalStrat("_nopal(ab)", 0,
            new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr},
            new Integer[] {A, A},
            new Integer[] {0, 0});
    private static PalStrat holdpal = new PalStrat("_pal(hold)", 0,
            new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr},
            new Integer[] {UP, UP},
            new Integer[] {0, 0});
    private static PalStrat cheatpal = new PalStrat("_pal(ab)", 0,
            new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr},
            new Integer[] {UP, UP | A, UP | A},
            new Integer[] {70, 0, 0});

    private static Strat nido0 = new Strat("_hop0", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {UP | SELECT | B},
            new Integer[] {1});
    private static Strat nido1 = new Strat("_hop1", 131,
            new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr},
            new Integer[] {NO_INPUT, NO_INPUT, A},
            new Integer[] {0, 0, 1});

    private static Strat cont = new Strat("", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {A},
            new Integer[] {1});
    //    private static Strat cont = new Strat("_cont", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A}, new Integer[] {1});

    private static Strat backout = new Strat("_backout", 97,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {B},
            new Integer[] {1});

    private static Strat gfSkip = new Strat("", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {UP | SELECT | B},
            new Integer[] {1});
    private static Strat title0 = new Strat("", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {START},
            new Integer[] {1});
    //private static Strat gfSkip = new Strat("_gfskip", 0,
    // new Integer[] {RedBlueAddr.joypadAddr},
    // new Integer[] {UP | SELECT | B},
    // new Integer[] {1});
    //private static Strat title0 = new Strat("_title0", 0,
    // new Integer[] {RedBlueAddr.joypadAddr},
    // new Integer[] {START},
    // new Integer[] {1});

    private static List<SaveTile> saveTiles = new ArrayList<>();
    //private static HashSet<String> seenStates = new HashSet<>();

    private static final OverworldTile[][] pw = new OverworldTile[45][14];

    private static Gb gb;
    private static GBWrapper wrap;
    private static GBMemory mem;

    // Put tiles in terms of pokeworld for sake of sanity
    private static final OverworldTile pw69_182 = new OverworldTile(1, 29, 20);
    private static final OverworldTile pw68_182 = new OverworldTile(1, 28, 20);
    private static final OverworldTile pw67_182 = new OverworldTile(1, 27, 20);
    private static final OverworldTile pw66_182 = new OverworldTile(1, 26, 20);
    private static final OverworldTile pw65_182 = new OverworldTile(1, 25, 20);
    private static final OverworldTile pw64_182 = new OverworldTile(1, 24, 20);
    private static final OverworldTile pw63_182 = new OverworldTile(1, 23, 20);
    private static final OverworldTile pw62_182 = new OverworldTile(1, 22, 20);
    private static final OverworldTile pw61_182 = new OverworldTile(1, 21, 20);
    private static final OverworldTile pw60_182 = new OverworldTile(1, 20, 20);
    private static final OverworldTile pw59_182 = new OverworldTile(1, 19, 20);
    private static final OverworldTile pw58_182 = new OverworldTile(1, 18, 20);
    private static final OverworldTile pw67_181 = new OverworldTile(1, 27, 19);
    private static final OverworldTile pw66_181 = new OverworldTile(1, 26, 19);
    private static final OverworldTile pw65_181 = new OverworldTile(1, 25, 19);
    private static final OverworldTile pw64_181 = new OverworldTile(1, 24, 19);
    private static final OverworldTile pw63_181 = new OverworldTile(1, 23, 19);
    private static final OverworldTile pw62_181 = new OverworldTile(1, 22, 19);
    private static final OverworldTile pw61_181 = new OverworldTile(1, 21, 19);
    private static final OverworldTile pw60_181 = new OverworldTile(1, 20, 19);
    private static final OverworldTile pw59_181 = new OverworldTile(1, 19, 19);
    private static final OverworldTile pw58_181 = new OverworldTile(1, 18, 19);
    private static final OverworldTile pw67_180 = new OverworldTile(1, 27, 18);
    private static final OverworldTile pw66_180 = new OverworldTile(1, 26, 18);
    private static final OverworldTile pw65_180 = new OverworldTile(1, 25, 18);
    private static final OverworldTile pw64_180 = new OverworldTile(1, 24, 18);
    private static final OverworldTile pw63_180 = new OverworldTile(1, 23, 18);
    private static final OverworldTile pw62_180 = new OverworldTile(1, 22, 18);
    private static final OverworldTile pw61_180 = new OverworldTile(1, 21, 18);
    private static final OverworldTile pw60_180 = new OverworldTile(1, 20, 18);
    private static final OverworldTile pw59_180 = new OverworldTile(1, 19, 18);
    private static final OverworldTile pw58_180 = new OverworldTile(1, 18, 18);
    private static final OverworldTile pw47_180 = new OverworldTile(1, 7, 18);
    private static final OverworldTile pw46_180 = new OverworldTile(1, 6, 18);
    private static final OverworldTile pw45_180 = new OverworldTile(1, 5, 18);
    private static final OverworldTile pw44_180 = new OverworldTile(1, 4, 18);
    private static final OverworldTile pw47_179 = new OverworldTile(1, 7, 17);
    private static final OverworldTile pw46_179 = new OverworldTile(1, 6, 17);
    private static final OverworldTile pw45_179 = new OverworldTile(1, 5, 17);
    private static final OverworldTile pw44_179 = new OverworldTile(1, 4, 17);
    private static final OverworldTile pw43_179 = new OverworldTile(1, 3, 17);
    private static final OverworldTile pw42_179 = new OverworldTile(1, 2, 17);
    private static final OverworldTile pw41_179 = new OverworldTile(1, 1, 17);
    private static final OverworldTile pw40_179 = new OverworldTile(1, 0, 17);
    private static final OverworldTile pw39_179 = new OverworldTile(33, 39, 9);
    private static final OverworldTile pw38_179 = new OverworldTile(33, 38, 9);
    private static final OverworldTile pw37_179 = new OverworldTile(33, 37, 9);
    private static final OverworldTile pw36_179 = new OverworldTile(33, 36, 9);
    private static final OverworldTile pw35_179 = new OverworldTile(33, 35, 9);
    private static final OverworldTile pw37_180 = new OverworldTile(33, 37, 10);
    private static final OverworldTile pw36_180 = new OverworldTile(33, 36, 10);
    private static final OverworldTile pw35_180 = new OverworldTile(33, 35, 10);
    private static final OverworldTile pw37_181 = new OverworldTile(33, 37, 11);
    private static final OverworldTile pw36_181 = new OverworldTile(33, 36, 11);
    private static final OverworldTile pw35_181 = new OverworldTile(33, 35, 11);
    private static final OverworldTile pw37_182 = new OverworldTile(33, 37, 12);
    private static final OverworldTile pw36_182 = new OverworldTile(33, 36, 12);
    private static final OverworldTile pw35_182 = new OverworldTile(33, 35, 12);
    private static final OverworldTile pw34_182 = new OverworldTile(33, 34, 12);
    private static final OverworldTile pw33_182 = new OverworldTile(33, 33, 12);

    private static final OverworldTile pw67_178 = new OverworldTile(1, 27, 16);
    private static final OverworldTile pw66_178 = new OverworldTile(1, 26, 16);
    private static final OverworldTile pw65_178 = new OverworldTile(1, 25, 16);
    private static final OverworldTile pw64_178 = new OverworldTile(1, 24, 16);
    private static final OverworldTile pw63_178 = new OverworldTile(1, 23, 16);
    private static final OverworldTile pw62_178 = new OverworldTile(1, 22, 16);
    private static final OverworldTile pw61_178 = new OverworldTile(1, 21, 16);
    private static final OverworldTile pw60_178 = new OverworldTile(1, 20, 16);
    private static final OverworldTile pw59_178 = new OverworldTile(1, 19, 16);
    private static final OverworldTile pw58_178 = new OverworldTile(1, 18, 16);
    private static final OverworldTile pw67_179 = new OverworldTile(1, 27, 17);
    private static final OverworldTile pw66_179 = new OverworldTile(1, 26, 17);
    private static final OverworldTile pw65_179 = new OverworldTile(1, 25, 17);
    private static final OverworldTile pw64_179 = new OverworldTile(1, 24, 17);
    private static final OverworldTile pw59_179 = new OverworldTile(1, 19, 17);
    private static final OverworldTile pw58_179 = new OverworldTile(1, 18, 17);
    private static final OverworldTile pw70_182 = new OverworldTile(1, 30, 20);
    private static final OverworldTile pw64_183 = new OverworldTile(1, 24, 21);
    private static final OverworldTile pw63_183 = new OverworldTile(1, 23, 21);
    private static final OverworldTile pw62_183 = new OverworldTile(1, 22, 21);
    private static final OverworldTile pw61_183 = new OverworldTile(1, 21, 21);
    private static final OverworldTile pw60_183 = new OverworldTile(1, 20, 21);
    private static final OverworldTile pw59_183 = new OverworldTile(1, 19, 21);
    private static final OverworldTile pw58_183 = new OverworldTile(1, 18, 21);
    private static final OverworldTile pw47_178 = new OverworldTile(1, 7, 16);
    private static final OverworldTile pw46_178 = new OverworldTile(1, 6, 16);
    private static final OverworldTile pw45_178 = new OverworldTile(1, 5, 16);
    private static final OverworldTile pw44_178 = new OverworldTile(1, 4, 16);
    private static final OverworldTile pw43_178 = new OverworldTile(1, 3, 16);
    private static final OverworldTile pw42_178 = new OverworldTile(1, 2, 16);
    private static final OverworldTile pw41_178 = new OverworldTile(1, 1, 16);
    private static final OverworldTile pw40_178 = new OverworldTile(1, 0, 16);
    private static final OverworldTile pw39_178 = new OverworldTile(33, 39, 8);
    private static final OverworldTile pw38_178 = new OverworldTile(33, 38, 8);
    private static final OverworldTile pw37_178 = new OverworldTile(33, 37, 8);
    private static final OverworldTile pw36_178 = new OverworldTile(33, 36, 8);
    private static final OverworldTile pw35_178 = new OverworldTile(33, 35, 8);
    private static final OverworldTile pw33_183 = new OverworldTile(33, 33, 13);
    private static final OverworldTile pw32_182 = new OverworldTile(33, 32, 12);
    private static final OverworldTile pw33_181 = new OverworldTile(33, 33, 11, true);
    private static final OverworldTile pw32_181 = new OverworldTile(33, 32, 11, true);
    private static final OverworldTile pw33_180 = new OverworldTile(33, 33, 10, true);

    private static final OverworldTile pw71_182 = new OverworldTile(1, 31, 20);
    private static final OverworldTile pw67_177 = new OverworldTile(1, 27, 15);
    private static final OverworldTile pw66_177 = new OverworldTile(1, 26, 15);
    private static final OverworldTile pw65_177 = new OverworldTile(1, 25, 15);
    private static final OverworldTile pw64_177 = new OverworldTile(1, 24, 15);
    private static final OverworldTile pw59_177 = new OverworldTile(1, 19, 15);
    private static final OverworldTile pw58_177 = new OverworldTile(1, 18, 15);
    private static final OverworldTile pw57_177 = new OverworldTile(1, 17, 15);
    private static final OverworldTile pw61_184 = new OverworldTile(1, 21, 22);
    private static final OverworldTile pw60_184 = new OverworldTile(1, 20, 22);
    private static final OverworldTile pw59_184 = new OverworldTile(1, 19, 22);
    private static final OverworldTile pw58_184 = new OverworldTile(1, 18, 22);
    private static final OverworldTile pw46_177 = new OverworldTile(1, 6, 15);
    private static final OverworldTile pw45_177 = new OverworldTile(1, 5, 15);
    private static final OverworldTile pw44_177 = new OverworldTile(1, 4, 15);
    private static final OverworldTile pw43_177 = new OverworldTile(1, 3, 15);
    private static final OverworldTile pw42_177 = new OverworldTile(1, 2, 15);
    private static final OverworldTile pw41_177 = new OverworldTile(1, 1, 15);
    private static final OverworldTile pw40_177 = new OverworldTile(1, 0, 15);
    private static final OverworldTile pw39_177 = new OverworldTile(33, 39, 7);
    private static final OverworldTile pw38_177 = new OverworldTile(33, 38, 7);
    private static final OverworldTile pw37_177 = new OverworldTile(33, 37, 7);
    private static final OverworldTile pw36_177 = new OverworldTile(33, 36, 7);
    private static final OverworldTile pw35_177 = new OverworldTile(33, 35, 7);
    private static final OverworldTile pw33_184 = new OverworldTile(33, 33, 14);
    private static final OverworldTile pw31_182 = new OverworldTile(33, 31, 12);
    private static final OverworldTile pw31_181 = new OverworldTile(33, 31, 11, true);
    private static final OverworldTile pw32_180 = new OverworldTile(33, 32, 10, true);
    private static final OverworldTile pw33_179 = new OverworldTile(33, 33, 9, true);

    private static final OverworldTile pw37_184 = new OverworldTile(33, 37, 14);
    private static final OverworldTile pw36_184 = new OverworldTile(33, 36, 14);
    private static final OverworldTile pw35_184 = new OverworldTile(33, 35, 14);
    private static final OverworldTile pw34_184 = new OverworldTile(33, 34, 14);

    private static final OverworldTile pw72_182 = new OverworldTile(1, 32, 20);
    private static final OverworldTile pw68_177 = new OverworldTile(1, 28, 15);
    private static final OverworldTile pw67_176 = new OverworldTile(1, 27, 14);
    private static final OverworldTile pw66_176 = new OverworldTile(1, 26, 14);
    private static final OverworldTile pw65_176 = new OverworldTile(1, 25, 14);
    private static final OverworldTile pw64_176 = new OverworldTile(1, 24, 14);
    private static final OverworldTile pw59_176 = new OverworldTile(1, 19, 14);
    private static final OverworldTile pw58_176 = new OverworldTile(1, 18, 14);
    private static final OverworldTile pw57_176 = new OverworldTile(1, 17, 14);
    private static final OverworldTile pw61_185 = new OverworldTile(1, 21, 23);
    private static final OverworldTile pw60_185 = new OverworldTile(1, 20, 23);
    private static final OverworldTile pw59_185 = new OverworldTile(1, 19, 23);
    private static final OverworldTile pw58_185 = new OverworldTile(1, 18, 23);
    private static final OverworldTile pw46_176 = new OverworldTile(1, 6, 14);
    private static final OverworldTile pw45_176 = new OverworldTile(1, 5, 14);
    private static final OverworldTile pw44_176 = new OverworldTile(1, 4, 14);
    private static final OverworldTile pw43_176 = new OverworldTile(1, 3, 14);
    private static final OverworldTile pw42_176 = new OverworldTile(1, 2, 14);
    private static final OverworldTile pw41_176 = new OverworldTile(1, 1, 14);
    private static final OverworldTile pw40_176 = new OverworldTile(1, 0, 14);
    private static final OverworldTile pw39_176 = new OverworldTile(33, 39, 6);
    private static final OverworldTile pw38_176 = new OverworldTile(33, 38, 6);
    private static final OverworldTile pw37_176 = new OverworldTile(33, 37, 6);
    private static final OverworldTile pw36_176 = new OverworldTile(33, 36, 6);
    private static final OverworldTile pw33_185 = new OverworldTile(33, 33, 15);
    private static final OverworldTile pw32_184 = new OverworldTile(33, 32, 14);
    private static final OverworldTile pw30_182 = new OverworldTile(33, 30, 12);
    private static final OverworldTile pw30_181 = new OverworldTile(33, 30, 11, true);
    private static final OverworldTile pw31_180 = new OverworldTile(33, 31, 10, true);
    private static final OverworldTile pw32_179 = new OverworldTile(33, 32, 9, true);
    private static final OverworldTile pw33_178 = new OverworldTile(33, 33, 8, true);

    private static final OverworldTile pw37_185 = new OverworldTile(33, 37, 15);
    private static final OverworldTile pw36_185 = new OverworldTile(33, 36, 15);
    private static final OverworldTile pw35_185 = new OverworldTile(33, 35, 15);
    private static final OverworldTile pw34_185 = new OverworldTile(33, 34, 15);

    private static final OverworldTile pw73_182 = new OverworldTile(1, 33, 20);
    private static final OverworldTile pw72_181 = new OverworldTile(1, 32, 19);
    private static final OverworldTile pw69_177 = new OverworldTile(1, 29, 15);
    private static final OverworldTile pw68_176 = new OverworldTile(1, 28, 14);
    private static final OverworldTile pw61_186 = new OverworldTile(1, 21, 24);
    private static final OverworldTile pw60_186 = new OverworldTile(1, 20, 24);
    private static final OverworldTile pw59_186 = new OverworldTile(1, 19, 24);
    private static final OverworldTile pw58_186 = new OverworldTile(1, 18, 24);
    private static final OverworldTile pw46_175 = new OverworldTile(1, 6, 13);
    private static final OverworldTile pw32_185 = new OverworldTile(33, 32, 15);
    private static final OverworldTile pw31_184 = new OverworldTile(33, 31, 14);
    private static final OverworldTile pw30_180 = new OverworldTile(33, 30, 10, true);
    private static final OverworldTile pw31_179 = new OverworldTile(33, 31, 9, true);
    private static final OverworldTile pw32_178 = new OverworldTile(33, 32, 8, true);

    private static final OverworldTile pw74_182 = new OverworldTile(1, 34, 20);
    private static final OverworldTile pw73_181 = new OverworldTile(1, 33, 19);
    private static final OverworldTile pw72_180 = new OverworldTile(1, 32, 18);
    private static final OverworldTile pw70_177 = new OverworldTile(1, 30, 15);
    private static final OverworldTile pw69_176 = new OverworldTile(1, 29, 14);
    private static final OverworldTile pw61_187 = new OverworldTile(1, 21, 25);
    private static final OverworldTile pw60_187 = new OverworldTile(1, 20, 25);
    private static final OverworldTile pw59_187 = new OverworldTile(1, 19, 25);
    private static final OverworldTile pw58_187 = new OverworldTile(1, 18, 25);
    private static final OverworldTile pw57_187 = new OverworldTile(1, 17, 25);
    private static final OverworldTile pw56_187 = new OverworldTile(1, 16, 25);
    private static final OverworldTile pw55_187 = new OverworldTile(1, 15, 25);
    private static final OverworldTile pw54_187 = new OverworldTile(1, 14, 25);
    private static final OverworldTile pw46_174 = new OverworldTile(1,6, 12);
    private static final OverworldTile pw31_185 = new OverworldTile(33, 31, 15);
    private static final OverworldTile pw30_184 = new OverworldTile(33, 30, 14);
    private static final OverworldTile pw30_179 = new OverworldTile(33, 30, 9, true);
    private static final OverworldTile pw31_178 = new OverworldTile(33, 31, 8, true);

    private static final OverworldTile pw57_174 = new OverworldTile(1, 17, 12);
    private static final OverworldTile pw58_174 = new OverworldTile(1, 18, 12);
    private static final OverworldTile pw59_174 = new OverworldTile(1, 19, 12);
    private static final OverworldTile pw57_175 = new OverworldTile(1, 17, 13);
    private static final OverworldTile pw58_175 = new OverworldTile(1, 18, 13);
    private static final OverworldTile pw59_175 = new OverworldTile(1, 19, 13);
    private static final OverworldTile pw65_183 = new OverworldTile(1, 25, 21);
    private static final OverworldTile pw66_183 = new OverworldTile(1, 26, 21);
    private static final OverworldTile pw67_183 = new OverworldTile(1, 27, 21);
    private static final OverworldTile pw68_183 = new OverworldTile(1, 28, 21);
    private static final OverworldTile pw69_183 = new OverworldTile(1, 29, 21);
    private static final OverworldTile pw70_183 = new OverworldTile(1, 30, 21);
    private static final OverworldTile pw71_183 = new OverworldTile(1, 31, 21);
    private static final OverworldTile pw72_183 = new OverworldTile(1, 32, 21);
    private static final OverworldTile pw73_183 = new OverworldTile(1, 33, 21);
    private static final OverworldTile pw66_184 = new OverworldTile(1, 26, 22);
    private static final OverworldTile pw67_184 = new OverworldTile(1, 27, 22);
    private static final OverworldTile pw68_184 = new OverworldTile(1, 28, 22);
    private static final OverworldTile pw69_184 = new OverworldTile(1, 29, 22);
    private static final OverworldTile pw70_184 = new OverworldTile(1, 30, 22);
    private static final OverworldTile pw71_184 = new OverworldTile(1, 31, 22);
    private static final OverworldTile pw72_184 = new OverworldTile(1, 32, 22);
    private static final OverworldTile pw66_185 = new OverworldTile(1, 26, 23);
    private static final OverworldTile pw67_185 = new OverworldTile(1, 27, 23);
    private static final OverworldTile pw68_185 = new OverworldTile(1, 28, 23);
    private static final OverworldTile pw69_185 = new OverworldTile(1, 29, 23);
    private static final OverworldTile pw70_185 = new OverworldTile(1, 30, 23);
    private static final OverworldTile pw71_185 = new OverworldTile(1, 31, 23);
    private static final OverworldTile pw66_186 = new OverworldTile(1, 26, 24);
    private static final OverworldTile pw67_186 = new OverworldTile(1, 27, 24);
    private static final OverworldTile pw68_186 = new OverworldTile(1, 28, 24);
    private static final OverworldTile pw69_186 = new OverworldTile(1, 29, 24);
    private static final OverworldTile pw70_186 = new OverworldTile(1, 30, 24);
    private static final OverworldTile pw66_187 = new OverworldTile(1, 26, 25);
    private static final OverworldTile pw67_187 = new OverworldTile(1, 27, 25);
    private static final OverworldTile pw68_187 = new OverworldTile(1, 28, 25);
    private static final OverworldTile pw69_187 = new OverworldTile(1, 29, 25);
    private static final OverworldTile pw56_178 = new OverworldTile(1, 16, 16);
    private static final OverworldTile pw57_178 = new OverworldTile(1, 17, 16);
    private static final OverworldTile pw48_179 = new OverworldTile(1, 8, 17);
    private static final OverworldTile pw49_179 = new OverworldTile(1, 9, 17);
    private static final OverworldTile pw50_179 = new OverworldTile(1, 10, 17);
    private static final OverworldTile pw51_179 = new OverworldTile(1, 11, 17);
    private static final OverworldTile pw52_179 = new OverworldTile(1, 12, 17);
    private static final OverworldTile pw53_179 = new OverworldTile(1, 13, 17);
    private static final OverworldTile pw54_179 = new OverworldTile(1, 14, 17);
    private static final OverworldTile pw55_179 = new OverworldTile(1, 15, 17);
    private static final OverworldTile pw56_179 = new OverworldTile(1, 16, 17);
    private static final OverworldTile pw48_180 = new OverworldTile(1, 8, 18);
    private static final OverworldTile pw49_180 = new OverworldTile(1, 9, 18);
    private static final OverworldTile pw50_180 = new OverworldTile(1, 10, 18);
    private static final OverworldTile pw51_180 = new OverworldTile(1, 11, 18);
    private static final OverworldTile pw52_180 = new OverworldTile(1, 12, 18);
    private static final OverworldTile pw53_180 = new OverworldTile(1, 13, 18);
    private static final OverworldTile pw54_180 = new OverworldTile(1, 14, 18);
    private static final OverworldTile pw55_180 = new OverworldTile(1, 15, 18);
    private static final OverworldTile pw56_180 = new OverworldTile(1, 16, 18);
    private static final OverworldTile pw57_180 = new OverworldTile(1, 17, 18);
    private static final OverworldTile pw48_181 = new OverworldTile(1, 8, 19);
    private static final OverworldTile pw49_181 = new OverworldTile(1, 9, 19);
    private static final OverworldTile pw50_181 = new OverworldTile(1, 10, 19);
    private static final OverworldTile pw51_181 = new OverworldTile(1, 11, 19);
    private static final OverworldTile pw52_181 = new OverworldTile(1, 12, 19);
    private static final OverworldTile pw53_181 = new OverworldTile(1, 13, 19);
    private static final OverworldTile pw54_181 = new OverworldTile(1, 14, 19);
    private static final OverworldTile pw55_181 = new OverworldTile(1, 15, 19);
    private static final OverworldTile pw56_181 = new OverworldTile(1, 16, 19);
    private static final OverworldTile pw57_181 = new OverworldTile(1, 17, 19);
    private static final OverworldTile pw48_182 = new OverworldTile(1, 8, 20);
    private static final OverworldTile pw49_182 = new OverworldTile(1, 9, 20);
    private static final OverworldTile pw50_182 = new OverworldTile(1, 10, 20);
    private static final OverworldTile pw51_182 = new OverworldTile(1, 11, 20);
    private static final OverworldTile pw52_182 = new OverworldTile(1, 12, 20);
    private static final OverworldTile pw53_182 = new OverworldTile(1, 13, 20);
    private static final OverworldTile pw54_182 = new OverworldTile(1, 14, 20);
    private static final OverworldTile pw55_182 = new OverworldTile(1, 15, 20);
    private static final OverworldTile pw56_182 = new OverworldTile(1, 16, 20);
    private static final OverworldTile pw57_182 = new OverworldTile(1, 17, 20);
    private static final OverworldTile pw48_183 = new OverworldTile(1, 8, 21);
    private static final OverworldTile pw49_183 = new OverworldTile(1, 9, 21);
    private static final OverworldTile pw50_183 = new OverworldTile(1, 10, 21);
    private static final OverworldTile pw51_183 = new OverworldTile(1, 11, 21);
    private static final OverworldTile pw52_183 = new OverworldTile(1, 12, 21);
    private static final OverworldTile pw53_183 = new OverworldTile(1, 13, 21);
    private static final OverworldTile pw54_183 = new OverworldTile(1, 14, 21);
    private static final OverworldTile pw55_183 = new OverworldTile(1, 15, 21);
    private static final OverworldTile pw56_183 = new OverworldTile(1, 16, 21);
    private static final OverworldTile pw57_183 = new OverworldTile(1, 17, 21);
    private static final OverworldTile pw49_184 = new OverworldTile(1, 9, 22);
    private static final OverworldTile pw50_184 = new OverworldTile(1, 10, 22);
    private static final OverworldTile pw51_184 = new OverworldTile(1, 11, 22);
    private static final OverworldTile pw52_184 = new OverworldTile(1, 12, 22);
    private static final OverworldTile pw53_184 = new OverworldTile(1, 13, 22);
    private static final OverworldTile pw54_184 = new OverworldTile(1, 14, 22);
    private static final OverworldTile pw55_184 = new OverworldTile(1, 15, 22);
    private static final OverworldTile pw56_184 = new OverworldTile(1, 16, 22);
    private static final OverworldTile pw57_184 = new OverworldTile(1, 17, 22);
    private static final OverworldTile pw50_185 = new OverworldTile(1, 10, 23);
    private static final OverworldTile pw51_185 = new OverworldTile(1, 11, 23);
    private static final OverworldTile pw52_185 = new OverworldTile(1, 12, 23);
    private static final OverworldTile pw53_185 = new OverworldTile(1, 13, 23);
    private static final OverworldTile pw54_185 = new OverworldTile(1, 14, 23);
    private static final OverworldTile pw55_185 = new OverworldTile(1, 15, 23);
    private static final OverworldTile pw56_185 = new OverworldTile(1, 16, 23);
    private static final OverworldTile pw57_185 = new OverworldTile(1, 17, 23);
    private static final OverworldTile pw54_186 = new OverworldTile(1, 14, 24);
    private static final OverworldTile pw55_186 = new OverworldTile(1, 15, 24);
    private static final OverworldTile pw56_186 = new OverworldTile(1, 16, 24);
    private static final OverworldTile pw57_186 = new OverworldTile(1, 17, 24);
    private static final OverworldTile pw44_181 = new OverworldTile(1, 4, 19);
    private static final OverworldTile pw45_181 = new OverworldTile(1, 5, 19);
    private static final OverworldTile pw46_181 = new OverworldTile(1, 6, 19);
    private static final OverworldTile pw47_181 = new OverworldTile(1, 7, 19);
    private static final OverworldTile pw44_182 = new OverworldTile(1, 4, 20);
    private static final OverworldTile pw45_182 = new OverworldTile(1, 5, 20);
    private static final OverworldTile pw46_182 = new OverworldTile(1, 6, 20);
    private static final OverworldTile pw47_182 = new OverworldTile(1, 7, 20);

    private static void initTiles() {
        saveTiles.add(new SaveTile(pw69_182, 0, false));
        saveTiles.add(new SaveTile(pw68_182, 0, false));
        saveTiles.add(new SaveTile(pw67_182, 0, false));
        saveTiles.add(new SaveTile(pw66_182, 0, false));
        saveTiles.add(new SaveTile(pw65_182, 0, false));
        saveTiles.add(new SaveTile(pw64_182, 0, false));
        saveTiles.add(new SaveTile(pw63_182, 0, false));
        saveTiles.add(new SaveTile(pw62_182, 0, false));
        saveTiles.add(new SaveTile(pw61_182, 0, false));
        saveTiles.add(new SaveTile(pw60_182, 0, false));
        saveTiles.add(new SaveTile(pw59_182, 0, false));
        saveTiles.add(new SaveTile(pw58_182, 0, false));
        saveTiles.add(new SaveTile(pw67_181, 0, false));
        saveTiles.add(new SaveTile(pw66_181, 0, false));
        saveTiles.add(new SaveTile(pw65_181, 0, false));
        saveTiles.add(new SaveTile(pw64_181, 0, false));
        saveTiles.add(new SaveTile(pw63_181, 0, false));
        saveTiles.add(new SaveTile(pw62_181, 0, false));
        saveTiles.add(new SaveTile(pw61_181, 0, false));
        saveTiles.add(new SaveTile(pw60_181, 0, false));
        saveTiles.add(new SaveTile(pw59_181, 0, false));
        saveTiles.add(new SaveTile(pw58_181, 0, false));
        saveTiles.add(new SaveTile(pw67_180, 0, false));
        saveTiles.add(new SaveTile(pw66_180, 0, false));
        saveTiles.add(new SaveTile(pw65_180, 0, false));
        saveTiles.add(new SaveTile(pw64_180, 0, false));
        saveTiles.add(new SaveTile(pw63_180, 0, false));
        saveTiles.add(new SaveTile(pw62_180, 0, false));
        saveTiles.add(new SaveTile(pw61_180, 0, false));
        saveTiles.add(new SaveTile(pw60_180, 0, false));
        saveTiles.add(new SaveTile(pw59_180, 0, false));
        saveTiles.add(new SaveTile(pw58_180, 0, false));
        saveTiles.add(new SaveTile(pw47_180, 0, true));
        saveTiles.add(new SaveTile(pw46_180, 0, true));
        saveTiles.add(new SaveTile(pw45_180, 0, true));
        saveTiles.add(new SaveTile(pw44_180, 0, true));
        saveTiles.add(new SaveTile(pw47_179, 0, true));
        saveTiles.add(new SaveTile(pw46_179, 0, true));
        saveTiles.add(new SaveTile(pw45_179, 0, true));
        saveTiles.add(new SaveTile(pw44_179, 0, true));
        saveTiles.add(new SaveTile(pw43_179, 0, true));
        saveTiles.add(new SaveTile(pw42_179, 0, true));
        saveTiles.add(new SaveTile(pw41_179, 0, true));
        saveTiles.add(new SaveTile(pw40_179, 0, true));
        saveTiles.add(new SaveTile(pw39_179, 0, false));
        saveTiles.add(new SaveTile(pw38_179, 0, false));
        saveTiles.add(new SaveTile(pw37_179, 0, false));
        saveTiles.add(new SaveTile(pw36_179, 0, false));
        saveTiles.add(new SaveTile(pw35_179, 0, false));
        saveTiles.add(new SaveTile(pw37_180, 0, false));
        saveTiles.add(new SaveTile(pw36_180, 0, false));
        saveTiles.add(new SaveTile(pw35_180, 0, false));
        saveTiles.add(new SaveTile(pw37_181, 0, false));
        saveTiles.add(new SaveTile(pw36_181, 0, false));
        saveTiles.add(new SaveTile(pw35_181, 0, false));
        saveTiles.add(new SaveTile(pw37_182, 0, false));
        saveTiles.add(new SaveTile(pw36_182, 0, false));
        saveTiles.add(new SaveTile(pw35_182, 0, false));
        saveTiles.add(new SaveTile(pw34_182, 0, false));
        saveTiles.add(new SaveTile(pw33_182, 0, false));
        saveTiles.add(new SaveTile(pw67_178, 34, false));
        saveTiles.add(new SaveTile(pw66_178, 34, false));
        saveTiles.add(new SaveTile(pw65_178, 34, false));
        saveTiles.add(new SaveTile(pw64_178, 34, false));
        saveTiles.add(new SaveTile(pw63_178, 34, false));
        saveTiles.add(new SaveTile(pw62_178, 34, false));
        saveTiles.add(new SaveTile(pw61_178, 34, false));
        saveTiles.add(new SaveTile(pw60_178, 34, false));
        saveTiles.add(new SaveTile(pw59_178, 34, false));
        saveTiles.add(new SaveTile(pw58_178, 34, false));
        saveTiles.add(new SaveTile(pw67_179, 34, false));
        saveTiles.add(new SaveTile(pw66_179, 34, false));
        saveTiles.add(new SaveTile(pw65_179, 34, false));
        saveTiles.add(new SaveTile(pw64_179, 34, false));
        saveTiles.add(new SaveTile(pw59_179, 34, false));
        saveTiles.add(new SaveTile(pw58_179, 34, false));
        saveTiles.add(new SaveTile(pw70_182, 34, false));
        saveTiles.add(new SaveTile(pw64_183, 34, false));
        saveTiles.add(new SaveTile(pw63_183, 34, false));
        saveTiles.add(new SaveTile(pw62_183, 34, false));
        saveTiles.add(new SaveTile(pw61_183, 34, false));
        saveTiles.add(new SaveTile(pw60_183, 34, false));
        saveTiles.add(new SaveTile(pw59_183, 34, false));
        saveTiles.add(new SaveTile(pw58_183, 34, false));
        saveTiles.add(new SaveTile(pw47_178, 34, true));
        saveTiles.add(new SaveTile(pw46_178, 34, true));
        saveTiles.add(new SaveTile(pw45_178, 34, true));
        saveTiles.add(new SaveTile(pw44_178, 34, true));
        saveTiles.add(new SaveTile(pw43_178, 34, true));
        saveTiles.add(new SaveTile(pw42_178, 34, true));
        saveTiles.add(new SaveTile(pw41_178, 34, true));
        saveTiles.add(new SaveTile(pw40_178, 34, true));
        saveTiles.add(new SaveTile(pw39_178, 34, false));
        saveTiles.add(new SaveTile(pw38_178, 34, false));
        saveTiles.add(new SaveTile(pw37_178, 34, false));
        saveTiles.add(new SaveTile(pw36_178, 34, false));
        saveTiles.add(new SaveTile(pw35_178, 34, false));
        saveTiles.add(new SaveTile(pw33_183, 34, false));
        saveTiles.add(new SaveTile(pw32_182, 34, false));
        saveTiles.add(new SaveTile(pw33_181, 0, false)); // has to be coded 0, but effectively 34
        saveTiles.add(new SaveTile(pw32_181, 34, false));
        saveTiles.add(new SaveTile(pw33_180, 34, false));
        saveTiles.add(new SaveTile(pw71_182, 68, false));
        saveTiles.add(new SaveTile(pw67_177, 68, false));
        saveTiles.add(new SaveTile(pw66_177, 68, false));
        saveTiles.add(new SaveTile(pw65_177, 68, false));
        saveTiles.add(new SaveTile(pw64_177, 68, false));
        saveTiles.add(new SaveTile(pw59_177, 68, false));
        saveTiles.add(new SaveTile(pw58_177, 68, false));
        saveTiles.add(new SaveTile(pw57_177, 68, false));
        saveTiles.add(new SaveTile(pw61_184, 68, false));
        saveTiles.add(new SaveTile(pw60_184, 68, false));
        saveTiles.add(new SaveTile(pw59_184, 68, false));
        saveTiles.add(new SaveTile(pw58_184, 68, false));
        saveTiles.add(new SaveTile(pw46_177, 68, true));
        saveTiles.add(new SaveTile(pw45_177, 68, true));
        saveTiles.add(new SaveTile(pw44_177, 68, true));
        saveTiles.add(new SaveTile(pw43_177, 68, true));
        saveTiles.add(new SaveTile(pw42_177, 68, true));
        saveTiles.add(new SaveTile(pw41_177, 68, true));
        saveTiles.add(new SaveTile(pw40_177, 68, true));
        saveTiles.add(new SaveTile(pw39_177, 68, false));
        saveTiles.add(new SaveTile(pw38_177, 68, false));
        saveTiles.add(new SaveTile(pw37_177, 68, false));
        saveTiles.add(new SaveTile(pw36_177, 68, false));
        saveTiles.add(new SaveTile(pw35_177, 68, false));
        saveTiles.add(new SaveTile(pw33_184, 68, false));
        saveTiles.add(new SaveTile(pw31_182, 68, false));
        saveTiles.add(new SaveTile(pw31_181, 68, false));
        saveTiles.add(new SaveTile(pw32_180, 68, false));
        saveTiles.add(new SaveTile(pw33_179, 68, false));

        saveTiles.add(new SaveTile(pw37_184, 74, false));
        saveTiles.add(new SaveTile(pw36_184, 74, false));
        saveTiles.add(new SaveTile(pw35_184, 74, false));
        saveTiles.add(new SaveTile(pw34_184, 74, false));

        saveTiles.add(new SaveTile(pw72_182, 102, false));
        saveTiles.add(new SaveTile(pw68_177, 102, false));
        saveTiles.add(new SaveTile(pw67_176, 102, false));
        saveTiles.add(new SaveTile(pw66_176, 102, false));
        saveTiles.add(new SaveTile(pw65_176, 102, false));
        saveTiles.add(new SaveTile(pw64_176, 102, false));
        saveTiles.add(new SaveTile(pw59_176, 102, false));
        saveTiles.add(new SaveTile(pw58_176, 102, false));
        saveTiles.add(new SaveTile(pw57_176, 102, false));
        saveTiles.add(new SaveTile(pw61_185, 102, false));
        saveTiles.add(new SaveTile(pw60_185, 102, false));
        saveTiles.add(new SaveTile(pw59_185, 102, false));
        saveTiles.add(new SaveTile(pw58_185, 102, false));
        saveTiles.add(new SaveTile(pw46_176, 102, true));
        saveTiles.add(new SaveTile(pw45_176, 102, true));
        saveTiles.add(new SaveTile(pw44_176, 102, true));
        saveTiles.add(new SaveTile(pw43_176, 102, true));
        saveTiles.add(new SaveTile(pw42_176, 102, true));
        saveTiles.add(new SaveTile(pw41_176, 102, true));
        saveTiles.add(new SaveTile(pw40_176, 102, true));
        saveTiles.add(new SaveTile(pw39_176, 102, false));
        saveTiles.add(new SaveTile(pw38_176, 102, false));
        saveTiles.add(new SaveTile(pw37_176, 102, false));
        saveTiles.add(new SaveTile(pw36_176, 102, false));
        saveTiles.add(new SaveTile(pw33_185, 102, false));
        saveTiles.add(new SaveTile(pw32_184, 102, false));
        saveTiles.add(new SaveTile(pw30_182, 102, false));
        saveTiles.add(new SaveTile(pw30_181, 102, false));
        saveTiles.add(new SaveTile(pw31_180, 102, false));
        saveTiles.add(new SaveTile(pw32_179, 102, false));
        saveTiles.add(new SaveTile(pw33_178, 102, false));

        saveTiles.add(new SaveTile(pw37_185, 108, false));
        saveTiles.add(new SaveTile(pw36_185, 108, false));
        saveTiles.add(new SaveTile(pw35_185, 108, false));
        saveTiles.add(new SaveTile(pw34_185, 108, false));

        saveTiles.add(new SaveTile(pw73_182, 136, false));
        saveTiles.add(new SaveTile(pw72_181, 136, false));
        saveTiles.add(new SaveTile(pw69_177, 136, false));
        saveTiles.add(new SaveTile(pw68_176, 136, false));
        saveTiles.add(new SaveTile(pw61_186, 136, false));
        saveTiles.add(new SaveTile(pw60_186, 136, false));
        saveTiles.add(new SaveTile(pw59_186, 136, false));
        saveTiles.add(new SaveTile(pw58_186, 136, false));
        saveTiles.add(new SaveTile(pw46_175, 136, true));
        saveTiles.add(new SaveTile(pw32_185, 136, false));
        saveTiles.add(new SaveTile(pw31_184, 136, false));
        saveTiles.add(new SaveTile(pw30_180, 136, false));
        saveTiles.add(new SaveTile(pw31_179, 136, false));
        saveTiles.add(new SaveTile(pw32_178, 136, false));
/*
        saveTiles.add(new SaveTile(pw74_182, 170, false));
        saveTiles.add(new SaveTile(pw73_181, 170, false));
        saveTiles.add(new SaveTile(pw72_180, 170, false));
        saveTiles.add(new SaveTile(pw70_177, 170, false));
        saveTiles.add(new SaveTile(pw69_176, 170, false));
        saveTiles.add(new SaveTile(pw61_187, 170, false));
        saveTiles.add(new SaveTile(pw60_187, 170, false));
        saveTiles.add(new SaveTile(pw59_187, 170, false));
        saveTiles.add(new SaveTile(pw58_187, 170, false));
        saveTiles.add(new SaveTile(pw57_187, 170, false));
        saveTiles.add(new SaveTile(pw56_187, 170, false));
        saveTiles.add(new SaveTile(pw55_187, 170, false));
        saveTiles.add(new SaveTile(pw54_187, 170, false));
        saveTiles.add(new SaveTile(pw46_174, 170, true));
        saveTiles.add(new SaveTile(pw31_185, 170, false));
        saveTiles.add(new SaveTile(pw30_184, 170, false));
        saveTiles.add(new SaveTile(pw30_179, 170, false));
        saveTiles.add(new SaveTile(pw31_178, 170, false));
*/
        for(int x=0; x<=44; x++) {
            for(int y=0; y<=13; y++) {
                pw[x][y] = null;
            }
        }

        // Typed out 30 and 174 in every index so I could reference pokeworld sanely
        pw[69-30][182-174] = pw69_182;
        pw[68-30][182-174] = pw68_182;
        pw[67-30][182-174] = pw67_182;
        pw[66-30][182-174] = pw66_182;
        pw[65-30][182-174] = pw65_182;
        pw[64-30][182-174] = pw64_182;
        pw[63-30][182-174] = pw63_182;
        pw[62-30][182-174] = pw62_182;
        pw[61-30][182-174] = pw61_182;
        pw[60-30][182-174] = pw60_182;
        pw[59-30][182-174] = pw59_182;
        pw[58-30][182-174] = pw58_182;
        pw[67-30][181-174] = pw67_181;
        pw[66-30][181-174] = pw66_181;
        pw[65-30][181-174] = pw65_181;
        pw[64-30][181-174] = pw64_181;
        pw[63-30][181-174] = pw63_181;
        pw[62-30][181-174] = pw62_181;
        pw[61-30][181-174] = pw61_181;
        pw[60-30][181-174] = pw60_181;
        pw[59-30][181-174] = pw59_181;
        pw[58-30][181-174] = pw58_181;
        pw[67-30][180-174] = pw67_180;
        pw[66-30][180-174] = pw66_180;
        pw[65-30][180-174] = pw65_180;
        pw[64-30][180-174] = pw64_180;
        pw[63-30][180-174] = pw63_180;
        pw[62-30][180-174] = pw62_180;
        pw[61-30][180-174] = pw61_180;
        pw[60-30][180-174] = pw60_180;
        pw[59-30][180-174] = pw59_180;
        pw[58-30][180-174] = pw58_180;
        pw[47-30][180-174] = pw47_180;
        pw[46-30][180-174] = pw46_180;
        pw[45-30][180-174] = pw45_180;
        pw[44-30][180-174] = pw44_180;
        pw[47-30][179-174] = pw47_179;
        pw[46-30][179-174] = pw46_179;
        pw[45-30][179-174] = pw45_179;
        pw[44-30][179-174] = pw44_179;
        pw[43-30][179-174] = pw43_179;
        pw[42-30][179-174] = pw42_179;
        pw[41-30][179-174] = pw41_179;
        pw[40-30][179-174] = pw40_179;
        pw[39-30][179-174] = pw39_179;
        pw[38-30][179-174] = pw38_179;
        pw[37-30][179-174] = pw37_179;
        pw[36-30][179-174] = pw36_179;
        pw[35-30][179-174] = pw35_179;
        pw[37-30][180-174] = pw37_180;
        pw[36-30][180-174] = pw36_180;
        pw[35-30][180-174] = pw35_180;
        pw[37-30][181-174] = pw37_181;
        pw[36-30][181-174] = pw36_181;
        pw[35-30][181-174] = pw35_181;
        pw[37-30][182-174] = pw37_182;
        pw[36-30][182-174] = pw36_182;
        pw[35-30][182-174] = pw35_182;
        pw[34-30][182-174] = pw34_182;
        pw[33-30][182-174] = pw33_182;
        pw[67-30][178-174] = pw67_178;
        pw[66-30][178-174] = pw66_178;
        pw[65-30][178-174] = pw65_178;
        pw[64-30][178-174] = pw64_178;
        pw[63-30][178-174] = pw63_178;
        pw[62-30][178-174] = pw62_178;
        pw[61-30][178-174] = pw61_178;
        pw[60-30][178-174] = pw60_178;
        pw[59-30][178-174] = pw59_178;
        pw[58-30][178-174] = pw58_178;
        pw[67-30][179-174] = pw67_179;
        pw[66-30][179-174] = pw66_179;
        pw[65-30][179-174] = pw65_179;
        pw[64-30][179-174] = pw64_179;
        pw[59-30][179-174] = pw59_179;
        pw[58-30][179-174] = pw58_179;
        pw[70-30][182-174] = pw70_182;
        pw[64-30][183-174] = pw64_183;
        pw[63-30][183-174] = pw63_183;
        pw[62-30][183-174] = pw62_183;
        pw[61-30][183-174] = pw61_183;
        pw[60-30][183-174] = pw60_183;
        pw[59-30][183-174] = pw59_183;
        pw[58-30][183-174] = pw58_183;
        pw[47-30][178-174] = pw47_178;
        pw[46-30][178-174] = pw46_178;
        pw[45-30][178-174] = pw45_178;
        pw[44-30][178-174] = pw44_178;
        pw[43-30][178-174] = pw43_178;
        pw[42-30][178-174] = pw42_178;
        pw[41-30][178-174] = pw41_178;
        pw[40-30][178-174] = pw40_178;
        pw[39-30][178-174] = pw39_178;
        pw[38-30][178-174] = pw38_178;
        pw[37-30][178-174] = pw37_178;
        pw[36-30][178-174] = pw36_178;
        pw[35-30][178-174] = pw35_178;
        pw[33-30][183-174] = pw33_183;
        pw[32-30][182-174] = pw32_182;
        pw[33-30][181-174] = pw33_181;
        pw[32-30][181-174] = pw32_181;
        pw[33-30][180-174] = pw33_180;
        pw[71-30][182-174] = pw71_182;
        pw[67-30][177-174] = pw67_177;
        pw[66-30][177-174] = pw66_177;
        pw[65-30][177-174] = pw65_177;
        pw[64-30][177-174] = pw64_177;
        pw[59-30][177-174] = pw59_177;
        pw[58-30][177-174] = pw58_177;
        pw[57-30][177-174] = pw57_177;
        pw[61-30][184-174] = pw61_184;
        pw[60-30][184-174] = pw60_184;
        pw[59-30][184-174] = pw59_184;
        pw[58-30][184-174] = pw58_184;
        pw[46-30][177-174] = pw46_177;
        pw[45-30][177-174] = pw45_177;
        pw[44-30][177-174] = pw44_177;
        pw[43-30][177-174] = pw43_177;
        pw[42-30][177-174] = pw42_177;
        pw[41-30][177-174] = pw41_177;
        pw[40-30][177-174] = pw40_177;
        pw[39-30][177-174] = pw39_177;
        pw[38-30][177-174] = pw38_177;
        pw[37-30][177-174] = pw37_177;
        pw[36-30][177-174] = pw36_177;
        pw[35-30][177-174] = pw35_177;
        pw[33-30][184-174] = pw33_184;
        pw[31-30][182-174] = pw31_182;
        pw[31-30][181-174] = pw31_181;
        pw[32-30][180-174] = pw32_180;
        pw[33-30][179-174] = pw33_179;
        pw[37-30][184-174] = pw37_184;
        pw[36-30][184-174] = pw36_184;
        pw[35-30][184-174] = pw35_184;
        pw[34-30][184-174] = pw34_184;
        pw[72-30][182-174] = pw72_182;
        pw[68-30][177-174] = pw68_177;
        pw[67-30][176-174] = pw67_176;
        pw[66-30][176-174] = pw66_176;
        pw[65-30][176-174] = pw65_176;
        pw[64-30][176-174] = pw64_176;
        pw[59-30][176-174] = pw59_176;
        pw[58-30][176-174] = pw58_176;
        pw[57-30][176-174] = pw57_176;
        pw[61-30][185-174] = pw61_185;
        pw[60-30][185-174] = pw60_185;
        pw[59-30][185-174] = pw59_185;
        pw[58-30][185-174] = pw58_185;
        pw[46-30][176-174] = pw46_176;
        pw[45-30][176-174] = pw45_176;
        pw[44-30][176-174] = pw44_176;
        pw[43-30][176-174] = pw43_176;
        pw[42-30][176-174] = pw42_176;
        pw[41-30][176-174] = pw41_176;
        pw[40-30][176-174] = pw40_176;
        pw[39-30][176-174] = pw39_176;
        pw[38-30][176-174] = pw38_176;
        pw[37-30][176-174] = pw37_176;
        pw[36-30][176-174] = pw36_176;
        pw[33-30][185-174] = pw33_185;
        pw[32-30][184-174] = pw32_184;
        pw[30-30][182-174] = pw30_182;
        pw[30-30][181-174] = pw30_181;
        pw[31-30][180-174] = pw31_180;
        pw[32-30][179-174] = pw32_179;
        pw[33-30][178-174] = pw33_178;
        pw[37-30][185-174] = pw37_185;
        pw[36-30][185-174] = pw36_185;
        pw[35-30][185-174] = pw35_185;
        pw[34-30][185-174] = pw34_185;
        pw[73-30][182-174] = pw73_182;
        pw[72-30][181-174] = pw72_181;
        pw[69-30][177-174] = pw69_177;
        pw[68-30][176-174] = pw68_176;
        pw[61-30][186-174] = pw61_186;
        pw[60-30][186-174] = pw60_186;
        pw[59-30][186-174] = pw59_186;
        pw[58-30][186-174] = pw58_186;
        pw[46-30][175-174] = pw46_175;
        pw[32-30][185-174] = pw32_185;
        pw[31-30][184-174] = pw31_184;
        pw[30-30][180-174] = pw30_180;
        pw[31-30][179-174] = pw31_179;
        pw[32-30][178-174] = pw32_178;
        pw[74-30][182-174] = pw74_182;
        pw[73-30][181-174] = pw73_181;
        pw[72-30][180-174] = pw72_180;
        pw[70-30][177-174] = pw70_177;
        pw[69-30][176-174] = pw69_176;
        pw[61-30][187-174] = pw61_187;
        pw[60-30][187-174] = pw60_187;
        pw[59-30][187-174] = pw59_187;
        pw[58-30][187-174] = pw58_187;
        pw[57-30][187-174] = pw57_187;
        pw[56-30][187-174] = pw56_187;
        pw[55-30][187-174] = pw55_187;
        pw[54-30][187-174] = pw54_187;
        pw[46-30][174-174] = pw46_174;
        pw[31-30][185-174] = pw31_185;
        pw[30-30][184-174] = pw30_184;
        pw[30-30][179-174] = pw30_179;
        pw[31-30][178-174] = pw31_178;
        pw[57-30][174-174] = pw57_174;
        pw[58-30][174-174] = pw58_174;
        pw[59-30][174-174] = pw59_174;
        pw[57-30][175-174] = pw57_175;
        pw[58-30][175-174] = pw58_175;
        pw[59-30][175-174] = pw59_175;
        pw[65-30][183-174] = pw65_183;
        pw[66-30][183-174] = pw66_183;
        pw[67-30][183-174] = pw67_183;
        pw[68-30][183-174] = pw68_183;
        pw[69-30][183-174] = pw69_183;
        pw[70-30][183-174] = pw70_183;
        pw[71-30][183-174] = pw71_183;
        pw[72-30][183-174] = pw72_183;
        pw[73-30][183-174] = pw73_183;
        pw[66-30][184-174] = pw66_184;
        pw[67-30][184-174] = pw67_184;
        pw[68-30][184-174] = pw68_184;
        pw[69-30][184-174] = pw69_184;
        pw[70-30][184-174] = pw70_184;
        pw[71-30][184-174] = pw71_184;
        pw[72-30][184-174] = pw72_184;
        pw[66-30][185-174] = pw66_185;
        pw[67-30][185-174] = pw67_185;
        pw[68-30][185-174] = pw68_185;
        pw[69-30][185-174] = pw69_185;
        pw[70-30][185-174] = pw70_185;
        pw[71-30][185-174] = pw71_185;
        pw[66-30][186-174] = pw66_186;
        pw[67-30][186-174] = pw67_186;
        pw[68-30][186-174] = pw68_186;
        pw[69-30][186-174] = pw69_186;
        pw[70-30][186-174] = pw70_186;
        pw[66-30][187-174] = pw66_187;
        pw[67-30][187-174] = pw67_187;
        pw[68-30][187-174] = pw68_187;
        pw[69-30][187-174] = pw69_187;
        pw[56-30][178-174] = pw56_178;
        pw[57-30][178-174] = pw57_178;
        pw[48-30][179-174] = pw48_179;
        pw[49-30][179-174] = pw49_179;
        pw[50-30][179-174] = pw50_179;
        pw[51-30][179-174] = pw51_179;
        pw[52-30][179-174] = pw52_179;
        pw[53-30][179-174] = pw53_179;
        pw[54-30][179-174] = pw54_179;
        pw[55-30][179-174] = pw55_179;
        pw[56-30][179-174] = pw56_179;
        pw[48-30][180-174] = pw48_180;
        pw[49-30][180-174] = pw49_180;
        pw[50-30][180-174] = pw50_180;
        pw[51-30][180-174] = pw51_180;
        pw[52-30][180-174] = pw52_180;
        pw[53-30][180-174] = pw53_180;
        pw[54-30][180-174] = pw54_180;
        pw[55-30][180-174] = pw55_180;
        pw[56-30][180-174] = pw56_180;
        pw[57-30][180-174] = pw57_180;
        pw[48-30][181-174] = pw48_181;
        pw[49-30][181-174] = pw49_181;
        pw[50-30][181-174] = pw50_181;
        pw[51-30][181-174] = pw51_181;
        pw[52-30][181-174] = pw52_181;
        pw[53-30][181-174] = pw53_181;
        pw[54-30][181-174] = pw54_181;
        pw[55-30][181-174] = pw55_181;
        pw[56-30][181-174] = pw56_181;
        pw[57-30][181-174] = pw57_181;
        pw[48-30][182-174] = pw48_182;
        pw[49-30][182-174] = pw49_182;
        pw[50-30][182-174] = pw50_182;
        pw[51-30][182-174] = pw51_182;
        pw[52-30][182-174] = pw52_182;
        pw[53-30][182-174] = pw53_182;
        pw[54-30][182-174] = pw54_182;
        pw[55-30][182-174] = pw55_182;
        pw[56-30][182-174] = pw56_182;
        pw[57-30][182-174] = pw57_182;
        pw[48-30][183-174] = pw48_183;
        pw[49-30][183-174] = pw49_183;
        pw[50-30][183-174] = pw50_183;
        pw[51-30][183-174] = pw51_183;
        pw[52-30][183-174] = pw52_183;
        pw[53-30][183-174] = pw53_183;
        pw[54-30][183-174] = pw54_183;
        pw[55-30][183-174] = pw55_183;
        pw[56-30][183-174] = pw56_183;
        pw[57-30][183-174] = pw57_183;
        pw[49-30][184-174] = pw49_184;
        pw[50-30][184-174] = pw50_184;
        pw[51-30][184-174] = pw51_184;
        pw[52-30][184-174] = pw52_184;
        pw[53-30][184-174] = pw53_184;
        pw[54-30][184-174] = pw54_184;
        pw[55-30][184-174] = pw55_184;
        pw[56-30][184-174] = pw56_184;
        pw[57-30][184-174] = pw57_184;
        pw[50-30][185-174] = pw50_185;
        pw[51-30][185-174] = pw51_185;
        pw[52-30][185-174] = pw52_185;
        pw[53-30][185-174] = pw53_185;
        pw[54-30][185-174] = pw54_185;
        pw[55-30][185-174] = pw55_185;
        pw[56-30][185-174] = pw56_185;
        pw[57-30][185-174] = pw57_185;
        pw[54-30][186-174] = pw54_186;
        pw[55-30][186-174] = pw55_186;
        pw[56-30][186-174] = pw56_186;
        pw[57-30][186-174] = pw57_186;
        pw[44-30][181-174] = pw44_181;
        pw[45-30][181-174] = pw45_181;
        pw[46-30][181-174] = pw46_181;
        pw[47-30][181-174] = pw47_181;
        pw[44-30][182-174] = pw44_182;
        pw[45-30][182-174] = pw45_182;
        pw[46-30][182-174] = pw46_182;
        pw[47-30][182-174] = pw47_182;

        // BUILD THE GRAPH (without typing 1000 more lines of code)
        for(int x=0; x<pw.length; x++) {
            for(int y=0; y<pw[0].length; y++) {
                if(pw[x][y] != null) {
                    // LEFT
                    if(x!=0 && (pw[x-1][y] != null)) {
                        if(x >= 34-30) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw[x-1][y]));
                        } else if(x<=33-30) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw[x-1][y]));
                        }
                    }
                    // UP
                    if(y!=0 && (pw[x][y-1] != null)) {
                        if(x>=72-30 && y<=182-174) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw[x][y-1]));
                        } else if((y==180-174) && (x==58-30 || x==59-30 || (x>=64-30 && x<=67-30))) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw[x][y-1]));
                        } else if((y==179-174 && (x==58-30 || x==59-30 || (x>=64-30 && x<=67-30)))) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw[x][y-1]));
                        } else if(x>=35-30 && y<=179-174) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw[x][y-1]));
                        } else if(x>=44-30 && y>=180-174) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw[x][y-1]));
                        } else if((x>=35-30 && x<=37-30) && (y>=180-174 && y<=182-174)) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw[x][y-1]));
                        } else if(y>=182-174) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw[x][y-1]));
                        } else if(y<=181-174) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw[x][y-1]));
                        }
                    }
                    // RIGHT
                    if(x!=pw.length-1 && (pw[x+1][y] != null)) {
                        if(x==39-30) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.RIGHT, 35, 18, pw[x+1][y]));
                        } else if(x>=33-30) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw[x+1][y]));
                        } else if(x<=32-30) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw[x+1][y]));
                        }
                    }
                    // DOWN
                    if(y!=pw[0].length-1) {
                        if((x>=30-30 && x<=37-30 && x!=33-30) && (y==182-174) && pw[x][y+2]!= null) {
                            pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 74, 40, pw[x][y+2]));
                        } else if(pw[x][y+1]!= null) {
                            if(x>=72-30 && y<=181-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 40, pw[x][y+1]));
                            } else if((y==178-174) && (x==58-30 || x==59-30 || (x>=64-30 && x<=67-30))) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw[x][y+1]));
                            } else if((y==179-174 && (x==58-30 || x==59-30 || (x>=64-30 && x<=67-30)))) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 17, pw[x][y+1]));
                            } else if(x>=38-30 && y>=179-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw[x][y+1]));
                            } else if(x>=38-30 && y<=178-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 17, pw[x][y+1]));
                            } else if((x>=35-30 && x<=37-30) && y<=181-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 17, pw[x][y+1]));
                            } else if(y>=181-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw[x][y+1]));
                            } else if(y<=180-174) {
                                pw[x][y].addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 17, pw[x][y+1]));
                            }
                        }
                    }
                    // A
                    pw[x][y].addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw[x][y]));
                    // START_B, S_A_B_S, S_A_B_A_B_S (any more starts to get unreasonable imo)
                    int sbcost = (x <= 39-30) ? 53 : 54;
                    pw[x][y].addEdge(new OverworldEdge(OverworldAction.START_B, sbcost, sbcost, pw[x][y]));
                    pw[x][y].addEdge(new OverworldEdge(OverworldAction.S_A_B_S, sbcost+29, sbcost+30, pw[x][y]));
                    //pw[x][y].addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, sbcost+58, sbcost+60, pw[x][y]));
                    Collections.sort(pw[x][y].getEdgeList());

                    // MIN_STEPS_TO_GRASS
                    if(x >= 72-30 && y<=181-174) {
                        pw[x][y].setMinStepsToGrass(Math.abs(x-72) + Math.abs(y-182) + (72-33) + 7);
                    } else if((y==179-174 && (x==58-30 || x==59-30 || (x>=64-30 && x<=67-30)))) {
                        pw[x][y].setMinStepsToGrass(2 + Math.abs(x-(33-30)) + 4);
                    } else if(x>=38-30) {
                        pw[x][y].setMinStepsToGrass(Math.abs(x-(33-30)) + Math.abs(y-(179-174)) + 4);
                    } else if((x>=34-30 && x<=37-30) && (y>=176-174 && y<=182-174)) {
                        pw[x][y].setMinStepsToGrass(Math.abs(x-(34-30)) + Math.abs(y-(182-174)) + 2);
                    } else if((x>=30-30 && x<=33-30) && (y>=178-174 && y<=182-174)) {
                        pw[x][y].setMinStepsToGrass(1);
                    } else if((x>=30-30 && x<=37-30) && (y>=183-174 && y<=185-174)) {
                        pw[x][y].setMinStepsToGrass(Math.abs(x-(33-30)) + Math.abs(y-(182-174)) + 1);
                    }
                }
            }
        }
    }
}
