package dabomstew.rta.pidgey;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import dabomstew.rta.*;
import dabomstew.rta.ffef.OverworldAction;
import mrwint.gbtasgen.Gb;

public class PidgeyIGT0Checker {
    private static final String gameName = "red";
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    public static final int RESET = 0x800;

    private static GBWrapper wrap;
    private static GBMemory mem;
    static int map = 51;
    static int x = 1;
    static int y = 13;
    static String path = "U U A U U U U U U U U U U U U A R U U U U U U U U U U R R R R R U U U U U U D D D D D D U U U U U A L L";
    //static String path = "U R R R U U R U U D U";
    public static void main(String[] args) throws IOException {
        Gb.loadGambatte(1);

        String[] actions = path.split(" ");
        int successes = 60;
        for(int i=0; i<=59; i++) {
            makeSave(map, x, y, i);
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
            wrap.injectRBInput(START);
            wrap.advanceFrame();

            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

            // bulba health - redbar
            // wrap.writeMemory(0xD16D, 1);
            
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
        }
        System.out.println();
        System.out.println("Successes: " + successes + "/60");
    }

    private static boolean execute(OverworldAction owAction, boolean lastAction) {
        if(mem.getEnemyHP() > 0) {
            return true;
        }
        int res;
        switch(owAction) {
            case LEFT:
            case UP:
            case RIGHT:
            case DOWN:
                int input = 16 * (int) (Math.pow(2.0, (owAction.ordinal())));
                wrap.injectRBInput(input);
                Position dest = getDestination(mem, input);
                if (travellingToWarp(dest.map, dest.x, dest.y)) {
                    wrap.advanceWithJoypadToAddress(input, RedBlueAddr.enterMapAddr);
                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
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
                                    System.out.print("Encounter at [" + mem.getMap() + "#" + mem.getX() + "," + mem.getY() + "]: ");
                                    String rngAtEnc = mem.getRNGStateWithDsum();
                                    wrap.advanceFrame();
                                    wrap.advanceFrame();
                                    Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                            mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                    //  String pruneDsum = dsumPrune ? " [*]" : "";
                                    int enemyHP = mem.getEnemyHP();
                                    System.out.print(String.format(
                                            "species %d lv%d DVs %04X rng %s encrng %s, HP = %d",
                                            enc.species, enc.level, enc.dvs, enc.battleRNG, rngAtEnc, enemyHP
                                    ));
                                    if (enc.species == 36 && (enemyHP == 15 || enemyHP == 17)) {
                                        // non-redbar
                                        wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                        wrap.injectRBInput(A);
                                        wrap.advanceFrame();
                                        wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                        wrap.injectRBInput(DOWN | A);
                                        wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                        wrap.injectRBInput(A | RIGHT);
                                        int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                        if (res2 == RedBlueAddr.catchSuccessAddr) {
                                            System.out.print(", default ybf: [*]");
                                            return true;
                                        } else {
                                            System.out.print(", default ybf: [ ]");
                                            return false;
                                        }
                                    }
                                    return false;
                                }
                            }
                        }
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                        wrap.injectRBInput(input);
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                        res = wrap.advanceToAddress(RedBlueAddr.newBattleAddr,
                                RedBlueAddr.joypadOverworldAddr);
                    }
                    if (res == RedBlueAddr.newBattleAddr) {
                        res = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                RedBlueAddr.joypadOverworldAddr);
                        if (res == RedBlueAddr.encounterTestAddr) {
                            if ((mem.getHRA() <= 7 && mem.getMap() == 51) || (mem.getHRA() <= 24 && mem.getMap() == 13)) {
                                System.out.print("Encounter at [" + mem.getMap() + "#" + mem.getX() + "," + mem.getY() + "]: ");
                                String rngAtEnc = mem.getRNGStateWithDsum();
                                wrap.advanceFrame();
                                wrap.advanceFrame();
                                Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                        mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                //  String pruneDsum = dsumPrune ? " [*]" : "";
                                int enemyHP = mem.getEnemyHP();
                                System.out.print(String.format(
                                        "species %d lv%d DVs %04X rng %s encrng %s, HP = %d",
                                        enc.species, enc.level, enc.dvs, enc.battleRNG, rngAtEnc, enemyHP
                                ));
                                if (enc.species == 36 && (enemyHP == 15 || enemyHP == 17)) {
                                    wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                    wrap.injectRBInput(A);
                                    wrap.advanceFrame();
                                    wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                    wrap.injectRBInput(DOWN | A);
                                    wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                    wrap.injectRBInput(A | RIGHT);
                                    int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                    if (res2 == RedBlueAddr.catchSuccessAddr) {
                                        System.out.print(", default ybf: [*]");
                                        return true;
                                    } else {
                                        System.out.print(", default ybf: [ ]");
                                        return false;
                                    }
                                }
                                return false;
                            } else if (lastAction) {
                                System.out.print("NO ENCOUNTER");
                                return false;
                            } else {
                                wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                                return true;
                            }
                        } else {
                            if (lastAction) {
                                System.out.println();
                            }
                            return true;
                        }
                    }
                    if (lastAction) {
                        System.out.println();
                    }
                    return true;
                }
            case A:
                wrap.injectRBInput(A);
                wrap.advanceFrame(A);
                res = wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadOverworldAddr, RedBlueAddr.printLetterDelayAddr);
                if (res == RedBlueAddr.joypadOverworldAddr) {
                    return true;
                } else {
                    return false;
                }
            case START_B:
                wrap.injectRBInput(START);
                wrap.advanceFrame(START);
                wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
                wrap.injectRBInput(B);
                wrap.advanceFrame(B);
                wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadOverworldAddr);
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

    private static void makeSave(int map, int x, int y, int f) throws IOException {
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
        baseSave[0x2CF1] = (byte) f;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/poke" + gameName + ".sav", baseSave);
    }
}
