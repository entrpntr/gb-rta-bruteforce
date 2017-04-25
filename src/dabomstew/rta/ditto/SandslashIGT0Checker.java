package dabomstew.rta.ditto;

import dabomstew.rta.*;
import dabomstew.rta.ffef.OverworldAction;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;

public class SandslashIGT0Checker {
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

    private static int x = 24;
    private static int y = 16;
    private static String path   = "R U L D A D";

    private static GBWrapper wrap;
    private static GBMemory mem;

    public static void main(String[] args) throws IOException {
        if (!new File("testroms").exists()) {
            new File("testroms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("testroms/pokeblue-pcjack.gbc").exists()) {
            System.err.println("Could not find pokeblue-pcjack.gbc in testroms directory!");
            System.exit(0);
        }

        Gb.loadGambatte(1);

        String[] actions = path.split(" ");
        int successes = 60;
        for(int i=0; i<=59; i++) {
            makeSave(x, y, i);
            Gb gb = new Gb(0, false);
            gb.startEmulator("testroms/pokeblue-pcjack.gbc");
            mem = new GBMemory(gb);
            wrap = new GBWrapper(gb, mem);

            // TODO: DON'T HARDCODE INTRO; ABSTRACT INTRO STRATS TO BE USABLE BY SEARCHER AND CHECKER
            wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.biosReadKeypadAddr);
            wrap.advanceFrame(UP);
            wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.initAddr);
/*
            for(int k=0; k<77; k++) {
                wrap.advanceFrame(UP);
            }
            wrap.advanceWithJoypadToAddress(UP | B, RedBlueAddr.initAddr);
*/
            //wrap.advanceToAddress(RedBlueAddr.delayAtEndOfShootingStarAddr);
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

            //wrap.writeMemory(0xD16D, 0x01);

            wrap.writeMemory(0xD31D, 0x06);
            wrap.writeMemory(0xD322, 0x0D);
            wrap.writeMemory(0xD323, 0x01);
            wrap.writeMemory(0xD324, 0x1E);
            wrap.writeMemory(0xD325, 0x02);
            wrap.writeMemory(0xD326, 0x44);
            wrap.writeMemory(0xD327, 0x01);
            wrap.writeMemory(0xD328, 0x06);
            wrap.writeMemory(0xD329, 0x01);
            wrap.writeMemory(0xD32A, 0xFF);

            int nLen = 1;
            wrap.writeMemory(0xD158 + nLen, 0x50);
            for(int n=0; n<nLen; n++) {
                wrap.writeMemory(0xD158 + n, 0x80);
            }

            int sLen = 3;
            wrap.writeMemory(0xD2B5 + sLen, 0x50);
            for(int s=0; s<sLen; s++) {
                wrap.writeMemory(0xD2B5 + s, 0x80);
            }

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
            //  gb.step(HARD_RESET);
        }
        System.out.println();
        System.out.println("Successes: " + successes + "/60");
    }

    // TODO: ABSTRACT THIS OUT TO BE USABLE BY SEARCHER AND CHECKER
    private static boolean execute(OverworldAction owAction, boolean lastAction) {
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
                if(res == RedBlueAddr.newBattleAddr) {
                    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr, RedBlueAddr.joypadOverworldAddr);
                } else {
                    res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
                }
                if (res == RedBlueAddr.encounterTestAddr) {
                    if (mem.getHRA() >= 0 && mem.getHRA() <= 9) {
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
                        if (enc.species == 97) {
/*                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                            wrap.injectRBInput(A);
                            wrap.advanceFrame();
                            wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                            wrap.injectRBInput(DOWN | A);
                            wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.joypadAddr);
                            wrap.advanceFrame(DOWN | A);
                            wrap.injectRBInput(A | RIGHT);
                            wrap.advanceFrame(A | RIGHT);
                            int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
*/
                            //wrap.writeMemory(0xD158 + 4, 0x50);
                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                            wrap.injectRBInput(A);
                            wrap.advanceFrame();
                            wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                            wrap.injectRBInput(DOWN | A);
                            wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                            //wrap.advanceWithJoypadToAddress(DOWN | A | RIGHT, RedBlueAddr.joypadAddr);
                            //wrap.advanceFrame(DOWN | A | RIGHT);
                            wrap.injectRBInput(DOWN | A | RIGHT);
                            int res2 = wrap.advanceWithJoypadToAddress(DOWN | A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
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

    private static void makeSave(int x, int y, int igtFrames) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/pcjack-dungeon.sav");
        int mapWidth = 15;
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
        FileFunctions.writeBytesToFile("testroms/pokeblue-pcjack.sav", baseSave);
    }
}
