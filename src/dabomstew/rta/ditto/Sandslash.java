package dabomstew.rta.ditto;

import dabomstew.rta.*;
import dabomstew.rta.ffef.*;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.*;

public class Sandslash {
    private static final int NO_INPUT = 0x00;
    private static final int A = 0x01;
    private static final int B = 0x02;
    private static final int SELECT = 0x04;
    private static final int START = 0x08;
    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static final int HARD_RESET = 0x800;

    /* Change this to increase/decrease number of intro sequence combinations processed */
    private static final int MAX_COST = 90;

    private static PrintWriter writer;
    private static HashSet<String> seenStates = new HashSet<>();
    private static List<SaveTile> saveTiles = new ArrayList<>();

    private static Gb gb;
    private static GBWrapper wrap;
    private static GBMemory mem;

    private static void overworldSearch(OverworldState ow) {
        //System.out.println(ow.toString());
        if(ow.getWastedFrames() > MAX_COST) {
            return;
        }

        if(!seenStates.add(ow.getUniqId())) {
            return;
        }

        //if(inferDsum(ow)) {
        //    return;
        //}

        ByteBuffer curSave = gb.saveState();

        for(OverworldEdge edge : ow.getPos().getEdgeList()) {
            OverworldAction edgeAction = edge.getAction();

            if (ow.aPressCounter() > 0 && (edgeAction == OverworldAction.A || edgeAction == OverworldAction.START_B
                    || edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
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
                    ((OverworldAction.isDpad(edgeAction) && edge.getNextPos().isEncounterTile()) ? 0 : edge.getNextPos().getMinStepsToGrass() * 9);
            //int highFrames = edge.getFrames() + 9 * edge.getNextPos().getMinStepsToGrass() +
            //        MAX_COST - ow.getWastedFrames() - edgeCost;
            int highFrames = MAX_COST - ow.getWastedFrames();
            double lowDsumChange = ((double)lowFrames)*DSUM_LOW_COEF;
            double highDsumChange = ((double)highFrames)*DSUM_HIGH_COEF;
            double predictLow = ((double)ow.getDsum())+2048.0-lowDsumChange+DSUM_MARGIN_OF_ERROR;
            double predictHigh = ((double)ow.getDsum())+2048.0-highDsumChange-DSUM_MARGIN_OF_ERROR;

            //if(inferDsumDebug(predictHigh, predictLow)) {
            //if(inferDsum(predictHigh, predictLow)) {
            //    continue;
            //}

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
                        if (mem.getHRA() >= 0 && mem.getHRA() <= 9) {
                            String rngAtEnc = mem.getRNGStateWithDsum();
                            wrap.advanceFrame();
                            wrap.advanceFrame();
                            Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                    mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                            int owFrames = ow.getOverworldFrames() + edge.getFrames();
                            //  String pruneDsum = dsumPrune ? " [*]" : "";
                            String defaultYbf = "";
                            String redbarYbf = "";
                            if(enc.species == 97) {
                                // non-redbar
//                                ByteBuffer saveState2 = gb.saveState();
                                wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                wrap.injectRBInput(A);
                                wrap.advanceFrame();
                                wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                wrap.injectRBInput(DOWN | A);
                                wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                wrap.injectRBInput(DOWN | A | RIGHT);
                                int res2 = wrap.advanceWithJoypadToAddress(DOWN | A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                if(res2 == RedBlueAddr.catchSuccessAddr) {
                                    defaultYbf = ", default ybf: [*]";
                                } else {
                                    defaultYbf = ", default ybf: [ ]";
                                }
/*
                                // redbar
                                gb.loadState(saveState2);
                                wrap.writeMemory(0xD16D, 1);
                                wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                wrap.injectRBInput(A);
                                wrap.advanceFrame();
                                wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                wrap.injectRBInput(DOWN | A);
                                wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                wrap.injectRBInput(A | RIGHT);
                                int res3 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                if(res3 == RedBlueAddr.catchSuccessAddr) {
                                    redbarYbf = ", redbar ybf: [*]";
                                } else {
                                    redbarYbf = ", redbar ybf: [ ]";
                                }
                                */
                            }
                            writer.println(
                                    ow.toString() + " " + edgeAction.logStr() + ", " +
                                            String.format(
                                                    "species %d lv%d DVs %04X rng %s encrng %s",
                                                    enc.species, enc.level, enc.dvs, enc.battleRNG, rngAtEnc
                                            ) + ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames) + defaultYbf + redbarYbf
                                    //                              + pruneDsum
                            );
                            writer.flush();
                        } else {
                            res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                        }
                    } if (res == RedBlueAddr.joypadOverworldAddr) {
                        int igt = readIGT();
                        int extraWastedFrames = igt - initIGT - edge.getFrames();
                        //String pruneDsum = prune ? "[*]" : "";
                        newState = new OverworldState(ow.toString() + " " + edgeAction.logStr()
                                // + pruneDsum
                                , edge.getNextPos(), Math.max(0, ow.aPressCounter() - 1), true, gb.getDivState(), mem.getHRA(), mem.getHRS(),
                                false, mem.getTurnFrameStatus(), mem.getNPCTimers(),
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
    public static void main(String[] args) throws IOException {
        saveTiles.add(new SaveTile(pw24_16, 9, false));
        saveTiles.add(new SaveTile(pw25_16, 18, false));
        saveTiles.add(new SaveTile(pw24_17, 0, false));
        saveTiles.add(new SaveTile(pw25_17, 9, false));

        pw21_12.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw21_13));
        pw21_12.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw22_12));
        pw21_12.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw21_12));
        pw21_12.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw21_12));
        pw21_12.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw21_12));
        //pw21_12.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw21_12));
        pw21_12.setMinStepsToGrass(1);

        pw22_12.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw21_12));
        pw22_12.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw22_13));
        pw22_12.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw22_12));
        pw22_12.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw22_12));
        pw22_12.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw22_12));
        //pw22_12.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw22_12));
        pw22_12.setMinStepsToGrass(1);

        pw21_13.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw21_12));
        pw21_13.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw21_14));
        pw21_13.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw22_13));
        pw21_13.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw21_13));
        pw21_13.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw21_13));
        pw21_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw21_13));
        //pw21_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw21_13));
        pw21_13.setMinStepsToGrass(1);

        pw22_13.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw22_12));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw21_13));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw22_14));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw23_13));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw22_13));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw22_13));
        pw22_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw22_13));
        //pw22_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw22_13));
        pw22_13.setMinStepsToGrass(1);

        pw23_13.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw22_13));
        pw23_13.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw23_14));
        pw23_13.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw23_13));
        pw23_13.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw23_13));
        pw23_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw23_13));
        //pw23_13.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw23_13));
        pw23_13.setMinStepsToGrass(1);

        pw20_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw20_15));
        pw20_14.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw21_14));
        pw20_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw20_14));
        pw20_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw20_14));
        pw20_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw20_14));
        //pw20_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw20_14));
        pw20_14.setMinStepsToGrass(1);

        pw21_14.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw21_13));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw20_14));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw21_15));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw22_14));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw21_14));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw21_14));
        pw21_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw21_14));
        //pw21_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw21_14));
        pw21_14.setMinStepsToGrass(1);

        pw22_14.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw22_13));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw21_14));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw22_15));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw23_14));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw22_14));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw22_14));
        pw22_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw22_14));
        //pw22_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw22_14));
        pw22_14.setMinStepsToGrass(1);

        pw23_14.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw23_13));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw22_14));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw23_15));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw24_14));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw23_14));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw23_14));
        pw23_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw23_14));
        //pw23_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw23_14));
        pw23_14.setMinStepsToGrass(1);

        pw24_14.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw23_14));
        pw24_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw24_15));
        pw24_14.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw25_14));
        pw24_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw24_14));
        pw24_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw24_14));
        pw24_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw24_14));
        //pw24_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw24_14));
        pw24_14.setMinStepsToGrass(1);

        pw25_14.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw24_14));
        pw25_14.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw25_15));
        pw25_14.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw25_14));
        pw25_14.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw25_14));
        pw25_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw25_14));
        //pw25_14.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw25_14));
        pw25_14.setMinStepsToGrass(1);

        pw20_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw20_14));
        pw20_15.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw21_15));
        pw20_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw20_15));
        pw20_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw20_15));
        pw20_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw20_15));
        //pw20_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw20_15));
        pw20_15.setMinStepsToGrass(1);

        pw21_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw21_14));
        pw21_15.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw20_15));
        pw21_15.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw22_15));
        pw21_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw21_15));
        pw21_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw21_15));
        pw21_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw21_15));
        //pw21_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw21_15));
        pw21_15.setMinStepsToGrass(1);

        pw22_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw22_14));
        pw22_15.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw21_15));
        pw22_15.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw23_15));
        pw22_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw22_15));
        pw22_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw22_15));
        pw22_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw22_15));
        //pw22_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw22_15));
        pw22_15.setMinStepsToGrass(1);

        pw23_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw23_14));
        pw23_15.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw22_15));
        pw23_15.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw24_15));
        pw23_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw23_15));
        pw23_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw23_15));
        pw23_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw23_15));
        //pw23_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw23_15));
        pw23_15.setMinStepsToGrass(1);

        pw24_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw24_14));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw23_15));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw24_16));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw25_15));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw24_15));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw24_15));
        pw24_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw24_15));
        //pw24_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw24_15));
        pw24_15.setMinStepsToGrass(1);

        pw25_15.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw25_14));
        pw25_15.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw24_15));
        pw25_15.addEdge(new OverworldEdge(OverworldAction.DOWN, 9, 9, pw25_16));
        pw25_15.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw25_15));
        pw25_15.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw25_15));
        pw25_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw25_15));
        //pw24_15.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw24_15));
        pw25_15.setMinStepsToGrass(1);

        pw24_16.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw24_15));
        pw24_16.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw25_16));
        pw24_16.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw24_16));
        pw24_16.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw24_16));
        pw24_16.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw24_16));
        //pw24_16.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw24_16));
        pw24_16.setMinStepsToGrass(1);

        pw25_16.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw25_15));
        pw25_16.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw24_16));
        pw25_16.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw25_16));
        pw25_16.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw25_16));
        pw25_16.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw25_16));
        //pw25_16.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw25_16));
        pw25_16.setMinStepsToGrass(1);

        pw24_17.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw24_16));
        pw24_17.addEdge(new OverworldEdge(OverworldAction.RIGHT, 9, 9, pw25_17));
        pw24_17.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw24_17));
        pw24_17.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw24_17));
        pw24_17.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw24_17));
        //pw24_17.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw24_17));
        pw24_17.setMinStepsToGrass(2);

        pw25_17.addEdge(new OverworldEdge(OverworldAction.UP, 9, 9, pw25_16));
        pw25_17.addEdge(new OverworldEdge(OverworldAction.LEFT, 9, 9, pw24_17));
        pw25_17.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw25_17));
        pw25_17.addEdge(new OverworldEdge(OverworldAction.START_B, 35, 35, pw25_17));
        pw25_17.addEdge(new OverworldEdge(OverworldAction.S_A_B_S, 64, 64, pw25_17));
        //pw25_17.addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, 91, 91, pw25_17));
        pw25_17.setMinStepsToGrass(2);
        List<IntroSequence> introSequences = new ArrayList<>();

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, fsback, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido1, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido1, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido2, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido2, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido2, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido2, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido1, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido1, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido1, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido1, title0, cont, fsback, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfWait, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfWait, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfWait, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfWait, nido0, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, mmback, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, mmback, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, mmback, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, mmback, title0, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title1, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title1, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title1, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title1, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido2, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido2, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido2, title0, cont, fsback, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido2, title0, cont, fsback, cont, cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, fsback, cont,cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, fsback, cont,cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, fsback, cont,cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, fsback, cont, fsback, cont, fsback, cont,cont));

        introSequences.add(new IntroSequence(nopal, gfSkip, nido3, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido3, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido3, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido3, title0, cont, cont));

        Collections.sort(introSequences);
        Collections.sort(saveTiles, new SaveTileComparator());

        // Make folder if necessary
        if (!new File("logs").exists()) {
            new File("logs").mkdir();
        }

        if (!new File("testroms").exists()) {
            new File("testroms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("testroms/pokeblue-pcjack.gbc").exists()) {
            System.err.println("Could not find pokeblue-pcjack.gbc in testroms directory!");
            System.exit(0);
        }

        String ts = Long.toString(System.currentTimeMillis());
        File file = new File("sandslash_encounters_" + ts + ".txt");
        writer = new PrintWriter(file);
        for(SaveTile saveTile : saveTiles) {
            OverworldTile savePos = saveTile.getOwPos();
            makeSave(savePos.getX(), savePos.getY());
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("testroms/pokeblue-pcjack.gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            for (int i=0; i<introSequences.size(); i++) {
                IntroSequence intro = introSequences.get(i);
                int baseCost = saveTile.getStartCost() + intro.cost();
                if (baseCost <= MAX_COST) {
                    intro.execute(wrap);
                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    //wrap.writeMemory(0xD2B5, 0x80);
                    //wrap.writeMemory(0xD2B6, 0x50);
                    //wrap.writeMemory(0xD158, 0x80);
                    //wrap.writeMemory(0xD159, 0x50);
                    //wrap.writeMemory(0xD31D, 0x04);
                    //wrap.writeMemory(0xD320, 0x0D);
                    //wrap.writeMemory(0xD322, 0x12);
                    //wrap.writeMemory(0xD324, 0x0D);
                    //wrap.writeMemory(0xD700, 0x01);
                    //wrap.writeMemory(0xD325, 0x01);
                    //wrap.writeMemory(0xD326, 0xFF);
                    int hra = mem.getHRA();
                    int hrs = mem.getHRS();
                    int dsum = (hra + hrs) % 256;
                    String header = "------  [" + savePos.getMap() + "#" + savePos.getX() + "," + savePos.getY() + "] " + intro.toString() + " | baseDsum: " + dsum + "; baseCost: " + baseCost + "  ------";
                    System.out.println(header);
                    OverworldState owState = new OverworldState(savePos.toString() + " - " + intro.toString() + ":",
                            saveTile.getOwPos(), 1, true, gb.getDivState(), hra, hrs,
                            saveTile.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(), baseCost, 0);
                    overworldSearch(owState);
                    gb.step(HARD_RESET);
                }
            }
        }
    }

    private static int readIGT() {
        return 3600*gb.readMemory(0xDA43) + 60*gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
    }

    private static void makeSave(int x, int y) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/pokeblue-pcjack.sav");
        int mapWidth = 15;
        int baseX = x;
        int baseY = y;
        int tlPointer = 0xC6E8 + (baseY / 2 + 1) * (mapWidth + 6) + (baseX / 2 + 1);
        //baseSave[0x2419] = (byte) 1;
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
        FileFunctions.writeBytesToFile("testroms/pokeblue-pcjack.sav", baseSave);
    }

    static class SaveTileComparator implements Comparator<SaveTile> {
        @Override public int compare(SaveTile o1, SaveTile o2) {
            if(o1.getStartCost() != o2.getStartCost()) {
                return o1.getStartCost() - o2.getStartCost();
            } else {
                return o2.getOwPos().getMinStepsToGrass() - o1.getOwPos().getMinStepsToGrass();
            }
        }
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
                    wrap.advanceFrame();
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
            String ret = "bluejack";
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

    private static PalStrat pal = new PalStrat("_pal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {UP}, new Integer[] {1});
    private static PalStrat nopal = new PalStrat("_nopal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {NO_INPUT}, new Integer[] {1});
    private static PalStrat abss = new PalStrat("_nopal(ab)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {A, A}, new Integer[] {0, 0});
    private static PalStrat holdpal = new PalStrat("_pal(hold)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {UP, UP}, new Integer[] {0, 0});

    private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    private static Strat gfWait = new Strat("_gfwait", 253, new Integer[] {RedBlueAddr.delayAtEndOfShootingStarAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});

    private static Strat nido0 = new Strat("_hop0", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    private static Strat nido1 = new Strat("_hop1", 131, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 1});
    private static Strat nido2 = new Strat("_hop2", 190, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 0, 0, 1});
    private static Strat nido3 = new Strat("_hop3", 298, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 0, 0, 0, 0, 1});

    private static Strat title0 = new Strat("_title0", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {START}, new Integer[] {1});
    private static Strat title1 = new Strat("_title1", 270, new Integer[] {RedBlueAddr.titleScreenPickNewMonAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, START}, new Integer[] { 1, 1});
    private static Strat mmback = new Strat("_mmback", 162 + 88, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {B}, new Integer[] {1});
    private static Strat cont = new Strat("", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {A},
            new Integer[] {1});

    private static Strat fsback = new Strat("_fsback", 97,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {B},
            new Integer[] {1});

    /* dsum unused */
    private static final double DSUM_HIGH_COEF = 0.686;
    private static final double DSUM_LOW_COEF = 0.623;
    private static final double DSUM_MARGIN_OF_ERROR = 5.0;

    // returns true if should prune
    private static boolean inferDsum(double predictHigh, double predictLow) {
        if(Math.abs(predictHigh-predictLow)>=256.0) {
            return false;
        } else {
            while(predictHigh>=256.0) {
                predictHigh-=256.0;
                predictLow-=256.0;
            }
            if (predictLow < 253.0 && predictHigh > 8.0) {
                return true;
            }
            return false;
        }
    }

    private static boolean inferDsum(OverworldState ow) {
        int minSteps = ow.getPos().getMinStepsToGrass();
        double dsum = (double)(2048+ow.getHra()+ow.getHrs());
        double lowFrames = ((double)(minSteps))*9.0;
        //double highFrames = 9.0*((double)(minSteps))+MAX_COST-ow.getWastedFrames();
        double highFrames = MAX_COST - ow.getWastedFrames();
        double predictLow = dsum-lowFrames*DSUM_LOW_COEF+DSUM_MARGIN_OF_ERROR;
        double predictHigh = dsum-highFrames*DSUM_HIGH_COEF-DSUM_MARGIN_OF_ERROR;
        return inferDsum(predictHigh, predictLow);
    }

    private static OverworldTile pw21_12 = new OverworldTile(228, 21, 12, true);
    private static OverworldTile pw22_12 = new OverworldTile(228, 22, 12, true);
    private static OverworldTile pw21_13 = new OverworldTile(228, 21, 13, true);
    private static OverworldTile pw22_13 = new OverworldTile(228, 22, 13, true);
    private static OverworldTile pw23_13 = new OverworldTile(228, 23, 13, true);
    private static OverworldTile pw20_14 = new OverworldTile(228, 20, 14, true);
    private static OverworldTile pw21_14 = new OverworldTile(228, 21, 14, true);
    private static OverworldTile pw22_14 = new OverworldTile(228, 22, 14, true);
    private static OverworldTile pw23_14 = new OverworldTile(228, 23, 14, true);
    private static OverworldTile pw24_14 = new OverworldTile(228, 24, 14, true);
    private static OverworldTile pw25_14 = new OverworldTile(228, 25, 14, true);
    private static OverworldTile pw20_15 = new OverworldTile(228, 20, 15, true);
    private static OverworldTile pw21_15 = new OverworldTile(228, 21, 15, true);
    private static OverworldTile pw22_15 = new OverworldTile(228, 22, 15, true);
    private static OverworldTile pw23_15 = new OverworldTile(228, 23, 15, true);
    private static OverworldTile pw24_15 = new OverworldTile(228, 24, 15, true);
    private static OverworldTile pw25_15 = new OverworldTile(228, 25, 15, true);
    private static OverworldTile pw24_16 = new OverworldTile(228, 24, 16, true);
    private static OverworldTile pw25_16 = new OverworldTile(228, 25, 16, true);
    private static OverworldTile pw24_17 = new OverworldTile(228, 24, 17);
    private static OverworldTile pw25_17 = new OverworldTile(228, 25, 17);

}
