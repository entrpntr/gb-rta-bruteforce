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

    private static String paths[] = {"U", "L"};
    private static int hps[] = {12, 1, 0};
    private static int nameLens[] = {4, 5, 6, 7};

    public static void main(String[] args) throws IOException {
        if (!new File("roms").exists()) {
            new File("roms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        if (!new File("roms/poke" + gameName + ".gbc").exists()) {
            System.err.println("Could not find poke" + gameName + ".gbc in roms directory!");
            System.exit(0);
        }

        Gb.loadGambatte(1);

        int igtFramesToCheck = 1; // = 60
        for(int j=0; j<igtFramesToCheck; j++) {
            int map = 228;
            int x = 25;
            int y = 17;
            makeSave(x, y, j);
            Gb gb = new Gb(0, false);
            gb.startEmulator("roms/poke" + gameName + ".gbc");
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

            for(String path : paths) {
                for (int hp : hps) {
                    for (int nameLen : nameLens) {
                        gb.loadState(saveState);
                        for (int i = 0; i < nameLen; i++) {
                            wrap.writeMemory(0xD158 + i, 0x80);
                        }
                        wrap.writeMemory(0xD158 + nameLen, 0x50);
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
                        if(igtFramesToCheck == 1) {
                            igtStr = "";
                        }
                        String output = igtStr + "input: " + path + ", hp: " + hp + ", nameLen: " + nameLen;
                        if(mem.getHRA() > 9) {
                            output += ", NO ENCOUNTER";
                        }
                        else {
                            wrap.advanceFrame();
                            wrap.advanceFrame();
                            Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                    mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                            if (enc.species != 76) {
                                output += ", BAD SPECIES = " + enc.species;
                            } else {
                                wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                wrap.injectRBInput(A);
                                wrap.advanceFrame();
                                wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.joypadAddr);
                                wrap.injectRBInput(DOWN | A);
                                wrap.advanceFrame(DOWN | A);

                                int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
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
        FileFunctions.writeBytesToFile("roms/poke" + gameName + ".sav", baseSave);
    }
}
