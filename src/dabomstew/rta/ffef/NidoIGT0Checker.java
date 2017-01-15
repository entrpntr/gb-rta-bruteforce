package dabomstew.rta.ffef;

import dabomstew.rta.*;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;

public class NidoIGT0Checker {
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

    // TODO: READ THIS STUFF FROM LOGS
    static String gameName = "red";
    static int map = 1;
    static int x = 7;
    static int y = 18;
    //static String path = "L L L L L D S_A_B_S D S_B A D L L L U";
    //static String path = "L D U A L L U L L L L A U L L L L L A D D A D D L A D L A U U A U";
    //static String path = "L L A L R A U L A D R A L L A L L A D L D D A L L A L U";
    static String path = "L L L U L L U A D L A L U L L L D D A D D A D L A L L A L U U A U";
    //static String path = "L L L L L L L L R R R L L L L L L L D D D L L U U U U D D";
    private static Gb gb;
    private static GBWrapper wrap;
    private static GBMemory mem;

    public static void main(String[] args) throws IOException {
        if (!new File("testroms").exists()) {
            new File("testroms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("testroms/poke" + gameName + ".gbc").exists()) {
            System.err.println("Could not find poke" + gameName + ".gbc in testroms directory!");
            System.exit(0);
        }

        Gb.loadGambatte(1);

        // TODO: There are currently hacks for specific Nido paths
        String[] actions = path.split(" ");
        int successes = 60;
        for(int i=0; i<=59; i++) {
            makeSave(map, x, y, i);
            gb = new Gb(0, false);
            gb.startEmulator("testroms/poke" + gameName + ".gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            //wrap.advanceWithJoypadToAddress(DOWN, RedBlueAddr.biosReadKeypadAddr);
            //wrap.advanceFrame(DOWN);
            //wrap.advanceFrame();
            // TODO: DON'T HARDCODE INTRO; ABSTRACT INTRO STRATS TO BE USABLE BY SEARCHER AND CHECKER
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
            String f = (i < 10) ? " " + i : "" + i;
            System.out.print("[" + f + "] ");
            for(int j=0; j<actions.length; j++) {
                OverworldAction owAction = OverworldAction.fromString(actions[j]);
                boolean lastAction = j==actions.length-1;
                boolean ledgejump = j==path.lastIndexOf("D")/2;
                //boolean ledgejump = false;
                if(!execute(owAction, lastAction, ledgejump)) {
                    System.out.println(" - FAILURE");
                    successes--;
                    break;
                } else if(lastAction) {
                    System.out.println();
                }
            }
          //  gb.step(HARD_RESET);
        }
        System.out.println();
        System.out.println("Successes: " + successes + "/60");
    }

    // TODO: ABSTRACT THIS OUT TO BE USABLE BY SEARCHER AND CHECKER
    private static boolean execute(OverworldAction owAction, boolean lastAction, boolean ledgeJump) {
        int res;
        //System.out.print(owAction.logStr());
        switch(owAction) {
            case LEFT:
            case UP:
            case RIGHT:
            case DOWN:
                int input = 16 * (int) (Math.pow(2.0, (owAction.ordinal())));
                wrap.injectRBInput(input);
                res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.newBattleAddr);
                //System.out.println("Test");
                if(res == RedBlueAddr.newBattleAddr) {
                    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr, RedBlueAddr.joypadOverworldAddr);
                } else {
                    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                }
                //System.out.print("Test 2");
                if (res == RedBlueAddr.encounterTestAddr) {
                    if (mem.getHRA() >= 0 && mem.getHRA() <= 24) {
                        System.out.print("Encounter at [" + mem.getMap() + "#" + mem.getX() + "," + mem.getY() + "]: ");
                        String rngAtEnc = mem.getRNGStateWithDsum();
                        wrap.advanceFrame();
                        wrap.advanceFrame();
                        Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                mem.getEncounterDVs(), mem.getRNGStateHRAOnly());

                        System.out.print(String.format(
                                "species %d lv%d DVs %04X rng %s encrng %s",
                                enc.species, enc.level, enc.dvs, enc.battleRNG, rngAtEnc
                        ));
                        if (enc.species == 3 && enc.level == 4 && (enc.dvs == 0xFFEF || enc.dvs == 0xFBEE )) {
                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                            wrap.injectRBInput(A);
                            wrap.advanceFrame();
                            wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                            wrap.injectRBInput(DOWN | A);
                            wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                            wrap.injectRBInput(A | RIGHT);
                            int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                            if (res2 == RedBlueAddr.catchSuccessAddr) {
                                System.out.print(", defaultYbf: [*]");
                                return true;
                            } else {
                                System.out.print(", defaultYbf: [ ]");
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else if(lastAction) {
                        System.out.print("NO ENCOUNTER");
                        return false;
                    } else {
                        wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                        return true;
                    }
                } else {
                    // TODO: implement NPC collision and ledgejumping
                    if(ledgeJump) {
                        wrap.injectRBInput(input);
                        wrap.advanceFrame(input);
                        wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                    }
                    if(lastAction) {
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
                    System.out.println("REACHED PRINTLETTERDELAY");
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

    private static void makeSave(int map, int x, int y, int igtFrames) throws IOException {
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
        baseSave[0x2CF1] = (byte) igtFrames;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/poke" + gameName + ".sav", baseSave);
    }
}
