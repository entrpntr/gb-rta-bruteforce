package dabomstew.rta.pidgey;

import dabomstew.rta.*;
import dabomstew.rta.ffef.*;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.*;

public class Pidgey {
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

    /* Change this to "blue" or "red" before running */
    private static final String gameName = "red";
    /* Change this to increase/decrease number of intro sequence combinations processed */
    private static final int MAX_COST = 21;

    private static PrintWriter writer;
    private static HashSet<String> seenStates = new HashSet<>();
    private static List<SaveTile> saveTiles = new ArrayList<>();

    private static Gb gb;
    private static GBWrapper wrap;
    private static GBMemory mem;

    private static void overworldSearch(OverworldState ow) {
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
                    ((OverworldAction.isDpad(edgeAction) && edge.getNextPos().isEncounterTile()) ? 0 : edge.getNextPos().getMinStepsToGrass() * 17);
            //int highFrames = edge.getFrames() + 17 * edge.getNextPos().getMinStepsToGrass() +
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
            int res = 0;
            OverworldState newState;
            switch (edgeAction) {
                case LEFT:
                case UP:
                case RIGHT:
                case DOWN:
                    int input = 16 * (int) (Math.pow(2.0, (edgeAction.ordinal())));
                    wrap.injectRBInput(input);
                    Position dest = getDestination(mem, input);
                    if (travellingToWarp(dest.map, dest.x, dest.y)) {
                        wrap.advanceWithJoypadToAddress(input, RedBlueAddr.enterMapAddr);
                        res = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    } else {
                        res = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr,
                                RedBlueAddr.newBattleAddr);
                        while (mem.getX() != dest.x || mem.getY() != dest.y) {
                            if (res == RedBlueAddr.newBattleAddr) {
                                // Check for garbage
                                res = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                        RedBlueAddr.joypadOverworldAddr);
                                if (res == RedBlueAddr.encounterTestAddr) {
                                    if ((mem.getHRA() <= 7 && mem.getMap() == 51) || (mem.getHRA() <= 24 && mem.getMap() == 13)) {
                                        String rngAtEnc = mem.getRNGStateWithDsum();
                                        wrap.advanceFrame();
                                        wrap.advanceFrame();
                                        Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                                mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                        int owFrames = ow.getOverworldFrames() + edge.getFrames();
                                        //  String pruneDsum = dsumPrune ? " [*]" : "";
                                        String defaultYbf = "";
                                        String redbarYbf = "";
                                        if (enc.species == 36) {
                                            // non-redbar
                                            ByteBuffer saveState2 = gb.saveState();
                                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                            wrap.injectRBInput(A);
                                            wrap.advanceFrame();
                                            wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                            wrap.injectRBInput(DOWN | A);
                                            wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                            wrap.injectRBInput(A | RIGHT);
                                            int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                            if (res2 == RedBlueAddr.catchSuccessAddr) {
                                                defaultYbf = ", default ybf: [*]";
                                            } else {
                                                defaultYbf = ", default ybf: [ ]";
                                            }

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
                                            if (res3 == RedBlueAddr.catchSuccessAddr) {
                                                redbarYbf = ", redbar ybf: [*]";
                                            } else {
                                                redbarYbf = ", redbar ybf: [ ]";
                                            }
                                            redbarYbf += ", HP = " + gb.readMemory(0xCFE7);
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
                                        break;
                                    }
                                }
                            }
                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                            wrap.injectRBInput(input);
                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                            res = wrap.advanceToAddress(RedBlueAddr.newBattleAddr,
                                    RedBlueAddr.joypadOverworldAddr);

                        }
                        if(res == RedBlueAddr.newBattleAddr) {
                            res = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                    RedBlueAddr.joypadOverworldAddr);
                            if (res == RedBlueAddr.encounterTestAddr) {
                                if ((mem.getHRA() <= 7 && mem.getMap() == 51) || (mem.getHRA() <= 24 && mem.getMap() == 13)) {
                                    String rngAtEnc = mem.getRNGStateWithDsum();
                                    wrap.advanceFrame();
                                    wrap.advanceFrame();
                                    Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                            mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                    int owFrames = ow.getOverworldFrames() + edge.getFrames();
                                    //  String pruneDsum = dsumPrune ? " [*]" : "";
                                    String defaultYbf = "";
                                    String redbarYbf = "";
                                    if (enc.species == 36) {
                                        // non-redbar
                                        ByteBuffer saveState2 = gb.saveState();
                                        wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                        wrap.injectRBInput(A);
                                        wrap.advanceFrame();
                                        wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                        wrap.injectRBInput(DOWN | A);
                                        wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                        wrap.injectRBInput(A | RIGHT);
                                        int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                        if (res2 == RedBlueAddr.catchSuccessAddr) {
                                            defaultYbf = ", default ybf: [*]";
                                        } else {
                                            defaultYbf = ", default ybf: [ ]";
                                        }

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
                                        if (res3 == RedBlueAddr.catchSuccessAddr) {
                                            redbarYbf = ", redbar ybf: [*]";
                                        } else {
                                            redbarYbf = ", redbar ybf: [ ]";
                                        }
                                        redbarYbf += ", HP = " + gb.readMemory(0xCFE7);
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
                                }
                                else {
                                    res = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                }
                            }
                        }
                    }
                    if (res == RedBlueAddr.joypadOverworldAddr) {
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
                                mem.getNPCTimers(), ow.getWastedFrames() + 4, ow.getOverworldFrames() + wastedFrames);
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
        // LOL HACKS GALORE; NO TIME FOR RATIONAL SOLUTIONS
        pw51_1_13.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_12));
        pw51_1_12.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_11));
        pw51_1_12.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_12));
        pw51_1_11.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_10));
        pw51_1_11.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_11));
        pw51_1_10.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_9));
        pw51_1_10.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_10));
        pw51_1_9.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_8));
        pw51_1_9.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_9));
        pw51_1_8.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_7));
        pw51_1_8.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_8));
        pw51_1_7.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_6));
        pw51_1_7.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_7));
        pw51_1_6.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_5));
        pw51_1_6.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_6));
        pw51_1_5.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_4));
        pw51_1_5.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_5));
        pw51_1_4.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_3));
        pw51_1_4.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_4));
        pw51_1_3.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_2));
        pw51_1_3.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_3));
        pw51_1_2.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw51_1_1));
        pw51_1_2.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_2));
        pw51_1_1.addEdge(new OverworldEdge(OverworldAction.UP, 0, 78, pw47_4_7));
        pw51_1_1.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw51_1_1));

        pw47_4_7.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_6));
        pw47_4_7.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_7));
        pw47_4_6.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_5));
        pw47_4_6.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_6));
        pw47_4_6.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_6));
        pw47_4_5.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_4));
        pw47_4_5.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_5));
        pw47_4_5.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_5));
        pw47_4_4.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_3));
        pw47_4_4.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_4));
        pw47_4_4.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_4));
        pw47_4_3.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_2));
        pw47_4_3.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_3));
        pw47_4_3.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_3));
        pw47_4_2.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_4_1));
        pw47_4_2.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_2));
        pw47_4_2.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_2));
        pw47_4_1.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw47_5_1));
        pw47_4_1.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_4_1));
        pw47_5_7.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_6));
        pw47_5_7.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_7));
        pw47_5_6.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_5));
        pw47_5_6.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_6));
        pw47_5_5.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_4));
        pw47_5_5.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_5));
        pw47_5_4.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_3));
        pw47_5_4.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_4));
        pw47_5_3.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_2));
        pw47_5_3.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_3));
        pw47_5_2.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw47_5_1));
        pw47_5_2.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_2));
        pw47_5_1.addEdge(new OverworldEdge(OverworldAction.UP, 0, 78, pw1_53_101));
        pw47_5_1.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw47_5_1));

        pw1_53_101.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw1_53_100));

        pw1_53_100.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw1_53_99));
        pw1_53_100.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_53_100));

        pw1_53_99.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_53_98));
        pw1_53_99.addEdge(new OverworldEdge(OverworldAction.RIGHT, 1, 17, pw1_54_99));
        pw1_53_99.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_53_99));

        pw1_53_98.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_53_97));
        pw1_53_98.addEdge(new OverworldEdge(OverworldAction.RIGHT, 1, 17, pw1_54_98));
        pw1_53_98.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_53_98));

        pw1_53_97.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_53_96));
        pw1_53_97.addEdge(new OverworldEdge(OverworldAction.RIGHT, 1, 17, pw1_54_97));
        pw1_53_97.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_53_97));

        pw1_53_96.addEdge(new OverworldEdge(OverworldAction.DOWN, 1, 17, pw1_53_97));
        pw1_53_96.addEdge(new OverworldEdge(OverworldAction.RIGHT, 1, 17, pw1_54_96));
        pw1_53_96.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_53_96));

        pw1_54_99.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_54_98));
        pw1_54_99.addEdge(new OverworldEdge(OverworldAction.LEFT, 1, 17, pw1_53_99));
        pw1_54_99.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_54_99));

        pw1_54_98.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_54_97));
        pw1_54_98.addEdge(new OverworldEdge(OverworldAction.LEFT, 1, 17, pw1_53_98));
        pw1_54_98.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_54_98));

        pw1_54_97.addEdge(new OverworldEdge(OverworldAction.UP, 1, 17, pw1_54_96));
        pw1_54_97.addEdge(new OverworldEdge(OverworldAction.LEFT, 1, 17, pw1_53_97));
        pw1_54_97.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_54_97));

        pw1_54_96.addEdge(new OverworldEdge(OverworldAction.DOWN, 1, 17, pw1_54_97));
        pw1_54_96.addEdge(new OverworldEdge(OverworldAction.LEFT, 1, 17, pw1_53_96));
        pw1_54_96.addEdge(new OverworldEdge(OverworldAction.A, 4, 2, pw1_54_96));

        saveTiles.add(new SaveTile(pw51_1_13, 0, true));
        //saveTiles.add(new SaveTile(pw51_1_12, 0, true));
        //saveTiles.add(new SaveTile(pw51_1_11, 0, true));
        //saveTiles.add(new SaveTile(pw1_53_101, 0, true));
        //saveTiles.add(new SaveTile(pw1_53_100, 0, true));
        //saveTiles.add(new SaveTile(pw1_53_99, 0, true));
        //saveTiles.add(new SaveTile(pw1_53_98, 0, true));
        //saveTiles.add(new SaveTile(pw1_54_99, 0, true));
        //saveTiles.add(new SaveTile(pw1_54_98, 0, true));
        List<IntroSequence> introSequences = new ArrayList<>();

        introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, cont));
        introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, cont));

        Collections.sort(introSequences);
        Collections.sort(saveTiles, new SaveTileComparator());


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
        File file = new File(gameName + "_pidgey_encounters_" + ts + ".txt");
        writer = new PrintWriter(file);
        for(SaveTile saveTile : saveTiles) {
            OverworldTile savePos = saveTile.getOwPos();
            makeSave(savePos.getMap(), savePos.getX(), savePos.getY());
            Gb.loadGambatte(1);
            gb = new Gb(0, false);
            gb.startEmulator("roms/poke" + gameName + ".gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            for (int i=0; i<introSequences.size(); i++) {
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
                    OverworldState owState = new OverworldState(savePos.toString() + " - " + intro.toString() + ":",
                            saveTile.getOwPos(), 1, true, gb.getDivState(), hra, hrs,
                            saveTile.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(), baseCost, 0);
                    overworldSearch(owState);
                    gb.step(HARD_RESET);
                }
            }
        }
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
        if (map == 51) {
            if (x == 1 && y == 0) {
                return true;
            }
        } else if (map == 47) {
            if (x == 5 && y == 0) {
                return true;
            }
        }
        return false;
    }

    private static int readIGT() {
        return 3600*gb.readMemory(0xDA43) + 60*gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
    }

    private static void makeSave(int map, int x, int y) throws IOException {
        String prefix = (map == 51) ? "forest" : "r2";
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/pidgey_" + gameName + "_" + prefix + ".sav");
        int mapWidth = (map == 51) ? 17 : 10;
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
        baseSave[0x2CF1] = (byte) 0;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("roms/poke" + gameName + ".sav", baseSave);
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

    private static PalStrat pal = new PalStrat("_pal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {UP}, new Integer[] {1});
    private static PalStrat nopal = new PalStrat("_nopal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {NO_INPUT}, new Integer[] {1});
    private static PalStrat abss = new PalStrat("_nopal(ab)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {A, A}, new Integer[] {0, 0});
    private static PalStrat holdpal = new PalStrat("_pal(hold)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {UP, UP}, new Integer[] {0, 0});

    private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    private static Strat gfWait = new Strat("_gfwait", 253, new Integer[] {RedBlueAddr.delayAtEndOfShootingStarAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});

    private static Strat nido0 = new Strat("_hop0", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});

    private static Strat title0 = new Strat("_title0", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {START}, new Integer[] {1});
    private static Strat cont = new Strat("", 0,
            new Integer[] {RedBlueAddr.joypadAddr},
            new Integer[] {A},
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
        double lowFrames = ((double)(minSteps))*17.0;
        //double highFrames = 17.0*((double)(minSteps))+MAX_COST-ow.getWastedFrames();
        double highFrames = MAX_COST - ow.getWastedFrames();
        double predictLow = dsum-lowFrames*DSUM_LOW_COEF+DSUM_MARGIN_OF_ERROR;
        double predictHigh = dsum-highFrames*DSUM_HIGH_COEF-DSUM_MARGIN_OF_ERROR;
        return inferDsum(predictHigh, predictLow);
    }

    private static OverworldTile pw51_1_13 = new OverworldTile(51, 1, 13, true);
    private static OverworldTile pw51_1_12 = new OverworldTile(51, 1, 12, true);
    private static OverworldTile pw51_1_11 = new OverworldTile(51, 1, 11, true);
    private static OverworldTile pw51_1_10 = new OverworldTile(51, 1, 10, true);
    private static OverworldTile pw51_1_9 = new OverworldTile(51, 1, 9, true);
    private static OverworldTile pw51_1_8 = new OverworldTile(51, 1, 8, true);
    private static OverworldTile pw51_1_7 = new OverworldTile(51, 1, 7, true);
    private static OverworldTile pw51_1_6 = new OverworldTile(51, 1, 6, true);
    private static OverworldTile pw51_1_5 = new OverworldTile(51, 1, 5);
    private static OverworldTile pw51_1_4 = new OverworldTile(51, 1, 4);
    private static OverworldTile pw51_1_3 = new OverworldTile(51, 1, 3);
    private static OverworldTile pw51_1_2 = new OverworldTile(51, 1, 2);
    private static OverworldTile pw51_1_1 = new OverworldTile(51, 1, 1);

    private static OverworldTile pw47_4_7 = new OverworldTile(47, 4, 7);
    private static OverworldTile pw47_4_6 = new OverworldTile(47, 4, 6);
    private static OverworldTile pw47_4_5 = new OverworldTile(47, 4, 5);
    private static OverworldTile pw47_4_4 = new OverworldTile(47, 4, 4);
    private static OverworldTile pw47_4_3 = new OverworldTile(47, 4, 3);
    private static OverworldTile pw47_4_2 = new OverworldTile(47, 4, 2);
    private static OverworldTile pw47_4_1 = new OverworldTile(47, 4, 1);
    private static OverworldTile pw47_5_7 = new OverworldTile(47, 5, 7);
    private static OverworldTile pw47_5_6 = new OverworldTile(47, 5, 6);
    private static OverworldTile pw47_5_5 = new OverworldTile(47, 5, 5);
    private static OverworldTile pw47_5_4 = new OverworldTile(47, 5, 4);
    private static OverworldTile pw47_5_3 = new OverworldTile(47, 5, 3);
    private static OverworldTile pw47_5_2 = new OverworldTile(47, 5, 2);
    private static OverworldTile pw47_5_1 = new OverworldTile(47, 5, 1);

    private static OverworldTile pw1_53_101 = new OverworldTile(13, 3, 11);
    private static OverworldTile pw1_53_100 = new OverworldTile(13, 3, 10);
    private static OverworldTile pw1_53_99 = new OverworldTile(13, 3, 9);
    private static OverworldTile pw1_53_98 = new OverworldTile(13, 3, 8);
    private static OverworldTile pw1_53_97 = new OverworldTile(13, 3, 7, true);
    private static OverworldTile pw1_53_96 = new OverworldTile(13, 3, 6, true);
    private static OverworldTile pw1_54_99 = new OverworldTile(13, 4, 9);
    private static OverworldTile pw1_54_98 = new OverworldTile(13, 4, 8);
    private static OverworldTile pw1_54_97 = new OverworldTile(13, 4, 7, true);
    private static OverworldTile pw1_54_96 = new OverworldTile(13, 4, 6, true);
}
