package dabomstew.rta.starter;

import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.RedBlueAddr;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static dabomstew.rta.starter.StarterIntros.*;

public class StarterSearch {
    private static String gameName;
    private static int minPlayerLen;
    private static int maxPlayerLen;

    private static final int HARD_RESET = 0x800;

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static final int MAX_WASTED_FRAMES;
    private static final int MAX_COST;
    private static final int GAME_COST;
    private static final int SAVE_COST;
    private static final String BASE_SAVE_NAME;

    static {
        /* Modify below parameters before running. */

        gameName = "blue";
        BASE_SAVE_NAME = "charmander2";
        MAX_WASTED_FRAMES = 420;
        minPlayerLen = 5;
        maxPlayerLen = 5;

        /* Modify above parameters before running. */


        if(gameName.equals("red")) {
            GAME_COST = 861;
        } else {
            GAME_COST = 868;
        }
        MAX_COST = MAX_WASTED_FRAMES + GAME_COST;

        if(BASE_SAVE_NAME.equals("charmander2")) {
            SAVE_COST = 102;
        } else {
            SAVE_COST = 0;
        }
    }

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

        List<IntroSequence> introSequences = initializeIntroSequences(MAX_COST-SAVE_COST, gameName, BASE_SAVE_NAME);
        System.out.println("Number of intro sequences: " + introSequences.size());

        Gb.loadGambatte(1);
        for (int playerLen = minPlayerLen; playerLen <= maxPlayerLen; playerLen++) {
            for (int igtFrmCoef = 0; igtFrmCoef < 12; igtFrmCoef++) {
                int igtF = igtFrmCoef * 5;

                System.out.println();
                System.out.println("Player Len: " + playerLen + ", IGT: " + igtF );
                System.out.println("----------------------");

                makeSave(igtF);
                Gb gb = new Gb(0, false);
                gb.startEmulator("testroms/poke" + gameName + ".gbc");
                GBMemory mem = new GBMemory(gb);
                GBWrapper wrap = new GBWrapper(gb, mem);

                for (IntroSequence seq : introSequences) {
                    boolean printed = false;
                    gb.step(HARD_RESET);
                    seq.execute(wrap);

                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

                    for (int n = 0xD158; n < 0xD158 + playerLen; n++) {
                        wrap.writeMemory(n, 0xF4);
                    }
                    wrap.writeMemory(0xD158 + playerLen, 0x50);

                    wrap.injectRBInput(START);
                    wrap.advanceFrame(START);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
/*
                    wrap.injectRBInput(START);
                    wrap.advanceFrame(START);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
*/
                    wrap.injectRBInput(A);
                    wrap.advanceFrame(A);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                    wrap.injectRBInput(A);
                    wrap.advanceFrame(A);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.manualTextScrollAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.displayTextBoxIdAddr);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
                    wrap.advanceFrame(A);

                    int atkdef;
                    int spdspc;
                    int atk;
                    int def;
                    int spd;
                    int spc;

                    ByteBuffer nonickState = gb.saveState();
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.manualTextScrollAddr);
                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.manualTextScrollAddr);
                    wrap.injectRBInput(A);
                    wrap.advanceFrame(A);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.displayTextBoxIdAddr);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
                    wrap.advanceFrame(B);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.calcStatAddr);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.updateNpcSpriteAddr);

                    atkdef = gb.readMemory(0xD186);
                    spdspc = gb.readMemory(0xD187);
                    atk = atkdef / 16;
                    def = atkdef % 16;
                    spd = spdspc / 16;
                    spc = spdspc % 16;
                    if ((atk == 1 && def == 0 && spd == 2 && spc == 4) || (atk == 15 && def == 15 && spd == 15 && spc == 15 && playerLen == 1)) {
                    //if (atk >= 14 && def >= 11 && spd >= 13 && spc >= 14) {
                        System.out.println(seq.toString() + "  (base cost: " + (seq.cost() + 492 - GAME_COST + SAVE_COST) + ")");
                        printed = true;
                        System.out.print("[NN] "); // NN = say no to nicknaming
                        System.out.println(String.format("DVs %04X", gb.readMemory(0xD186) * 256 + gb.readMemory(0xD187)));
                    }
                    gb.loadState(nonickState);

                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.manualTextScrollAddr);
                    wrap.injectRBInput(A);
                    wrap.advanceFrame(A);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.manualTextScrollAddr);

                    wrap.injectRBInput(B);
                    wrap.advanceFrame(B);
                    wrap.advanceWithJoypadToAddress(B, RedBlueAddr.displayTextBoxIdAddr);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);

                    wrap.advanceFrame(A);
                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.displayNamingScreenAddr);
                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);

                    ByteBuffer saveState = gb.saveState();
                    for (int i = -2; i <= MAX_COST - SAVE_COST - seq.cost() - 492; i++) {
                        gb.loadState(saveState);
                        if(i == -2) { // -2 = buffer start on naming screen
                            wrap.injectRBInput(START);
                        }
                        if(i == 0) {
                            wrap.advanceFrame();
                            wrap.advanceFrame(A);
                            saveState = gb.saveState();
                        }
                        if(i > 0) {
                            wrap.advanceFrame();
                            saveState = gb.saveState();
                        }

                        wrap.advanceWithJoypadToAddress(START, RedBlueAddr.updateNpcSpriteAddr);

                        String f = (i < 10 && i >= 0) ? " " + i : "" + i;

                        atkdef = gb.readMemory(0xD186);
                        spdspc = gb.readMemory(0xD187);
                        atk = atkdef / 16;
                        def = atkdef % 16;
                        spd = spdspc / 16;
                        spc = spdspc % 16;
                        if ((atk == 1 && def == 0 && spd == 2 && spc == 4) || (atk == 15 && def == 15 && spd == 15 && spc == 15 && playerLen == 1)) {
                        //if (atk >= 14 && def >= 11 && spd >= 13 && spc >= 14) {
                            if(!printed) {
                                System.out.println(seq.toString() + "  (base cost: " + (seq.cost() + 492 - GAME_COST + SAVE_COST) + ")");
                                printed = true;
                            }
                            System.out.print("[" + f + "] ");
                            System.out.println(String.format("DVs %04X", gb.readMemory(0xD186) * 256 + gb.readMemory(0xD187)));
                        }
                        if(i == -2) {
                            i++;
                        }
                    }
                }
            }
        }
    }

    private static void makeSave(int igtFrames) throws IOException {
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/" + BASE_SAVE_NAME + "_" + gameName + ".sav");

        baseSave[0x2CEF] = (byte) 0;
        baseSave[0x2CF0] = (byte) 47;
        baseSave[0x2CF1] = (byte) igtFrames;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/poke" + gameName + ".sav", baseSave);
    }
}