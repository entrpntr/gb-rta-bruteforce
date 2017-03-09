package dabomstew.rta.ditto;

import dabomstew.rta.*;
import dabomstew.rta.ffef.OverworldAction;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GodDittoChecker {
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

    private static String gameName = "red";

    private static String paths[] = {"L", "U"};
    private static int hps[] = {12,1,0};
    private static int nameLens[] = {1,2,3,4,5,6,7};
    private static int bulbaLens[] = {1,2,3,4,5,6,7,8,9,10};
    private static int numABs[] = {0,1,2,3,4};
    private static int igtFramesToCheck = 1; // = 60

/*
    private static String paths[] = {"D"};
    private static int hps[] = {1};
    private static int nameLens[] = {5};
    private static int bulbaLens[] = {1};
    private static int numABs[] = {1};
    private static int igtFramesToCheck = 60; // = 60
*/
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


        for(int j=0; j<igtFramesToCheck; j++) {
            int map = 228;
            int x = 25;
            int y = 17;
            makeSave(x, y, j);
            Gb gb = new Gb(0, false);
            gb.startEmulator("testroms/poke" + gameName + ".gbc");
            GBMemory mem = new GBMemory(gb);
            GBWrapper wrap = new GBWrapper(gb, mem);

            // pal(hold)
            wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.biosReadKeypadAddr);
            wrap.advanceFrame(UP);
            wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.initAddr);
            wrap.advanceFrame(UP);

            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(UP | SELECT | B);
            wrap.advanceFrame();
            wrap.advanceToAddress(RedBlueAddr.animateNidorinoAddr);
            wrap.advanceToAddress(RedBlueAddr.checkInterruptAddr);
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            wrap.injectRBInput(A);
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
            ByteBuffer saveState = gb.saveState();

            for (int nameLen : nameLens) {
                for(int bulbaLen : bulbaLens) {
                    for (int hp : hps) {
                        for(int numAB : numABs) {
                            for(String path : paths) {
                                gb.loadState(saveState);

                                wrap.writeMemory(0xD31D, 0x04);
                                wrap.writeMemory(0xD320, 0x0D);
                                wrap.writeMemory(0xD322, 0x12);
                                wrap.writeMemory(0xD324, 0x09);
                                wrap.writeMemory(0xD325, 0x01);
                                wrap.writeMemory(0xD326, 0xFF);

                                for (int k = 0; k < bulbaLen; k++) {
                                    wrap.writeMemory(0xD2B5 + k, 0x80);
                                }
                                for (int i = 0; i < nameLen; i++) {
                                    wrap.writeMemory(0xD158 + i, 0x80);
                                }
                                wrap.writeMemory(0xD158 + nameLen, 0x50);
                                wrap.writeMemory(0xD2B5 + bulbaLen, 0x50);
                                wrap.writeMemory(0xD16D, hp);
                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                wrap.injectRBInput(UP);
                                wrap.advanceFrame(UP);
                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                wrap.injectRBInput(A);
                                wrap.advanceFrame(A);
                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                int input = 16 * (int) (Math.pow(2.0, (OverworldAction.fromString(path).ordinal())));
                                wrap.injectRBInput(input);
                                wrap.advanceFrame(input);
                                wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr);
                                String igtStr = (j < 10) ? "[ " + j + "]: " : "[" + j + "]: ";
                                if (igtFramesToCheck == 1) {
                                    igtStr = "";
                                }
                                String output = igtStr + "playerLen: " + nameLen + ", bulbaLen: " + bulbaLen + ", bulbaHP: " + hp + ", numABs: " + numAB + ", input: U A " + path;
                                if (mem.getHRA() > 9) {
                                    output += ", NO ENCOUNTER";
                                } else {
                                    wrap.advanceFrame();
                                    wrap.advanceFrame();
                                    Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                            mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                    if (enc.species != 76) {
                                        output += ", BAD SPECIES = " + enc.species;
                                    } else {
                                        wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                        wrap.injectRBInput(A);
                                        wrap.advanceFrame();
                                        wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                        wrap.injectRBInput(DOWN | A);
                                        wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                        if(numAB > 0) {
                                            wrap.advanceWithJoypadToAddress(DOWN | A | RIGHT, RedBlueAddr.joypadAddr);
                                            //System.out.println("IGT-i: " + readIGT(gb));
                                            wrap.injectRBInput(DOWN | A | RIGHT);
                                            wrap.advanceFrame(DOWN | A | RIGHT);
                                            wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
                                            wrap.injectRBInput(B);
                                            wrap.advanceFrame(B);
                                        }
                                        for(int l=1; l<numAB; l++) {
                                            wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
                                            wrap.injectRBInput(A);
                                            wrap.advanceFrame(A);
                                            wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
                                            wrap.injectRBInput(B);
                                            wrap.advanceFrame(B);
                                        }

                                        //System.out.println("TEST");
                                        wrap.advanceWithJoypadToAddress(A | UP, RedBlueAddr.joypadAddr);
                                        //System.out.println("IGT-f: " + readIGT(gb));
                                        wrap.injectRBInput(A | UP);
                                        //wrap.advanceFrame();
                                        //System.out.println("TEST2");
                                        wrap.advanceWithJoypadToAddress(A | UP, RedBlueAddr.joypadAddr);

                                        int res2 = wrap.advanceWithJoypadToAddress(A | UP, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                        //System.out.println("TEST3");
                                        if (res2 == RedBlueAddr.catchSuccessAddr) {
                                            output += ", ybf: [*]";
                                        } else {
                                            output += ", ybf: [ ]";
                                        }
                                    }
                                }
                                System.out.println(output);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void makeSave(int x, int y, int igtFrames) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/ditto_" + gameName + ".sav");
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
        FileFunctions.writeBytesToFile("testroms/poke" + gameName + ".sav", baseSave);
    }

    private static int readIGT(Gb gb) {
        return 3600*gb.readMemory(0xDA43) + 60*gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
    }
}
