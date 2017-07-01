package dabomstew.rta.starter;

import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.RedBlueAddr;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static dabomstew.rta.starter.StarterIntros.*;

public class StarterFrameChecker {
    private static String gameName;
    private static int playerLen;
    private static int igtF;
    private static int maxFrameNum;
    private static IntroSequence seq;

    private static final int HARD_RESET = 0x800;

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static final String BASE_SAVE_NAME;

    /* Modify these parameters before running. */
    static {
        gameName = "blue";
        BASE_SAVE_NAME = "charmander2";
        playerLen = 5;
        igtF = 11;
        maxFrameNum = 59;
        initializeStrats(gameName, BASE_SAVE_NAME);
        seq = new IntroSequence(palhold, gfskip, hop0, titleStrat(0, START), fsbackA, contA);
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

        System.out.println(seq.toString());

        makeSave(igtF);
        Gb.loadGambatte(1);
        Gb gb = new Gb(0, false);
        gb.startEmulator("testroms/poke" + gameName + ".gbc");
        GBMemory mem = new GBMemory(gb);
        GBWrapper wrap = new GBWrapper(gb, mem);

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

        ByteBuffer noNickState = gb.saveState();
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

        System.out.print("[NN] "); // NN = say no to nickname
        printStats(gb);
        gb.loadState(noNickState);

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
        for(int i=-2; i<=maxFrameNum; i++) {
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

            System.out.print("[" + f + "] ");
            printStats(gb);

            if(i == -2) {
                i++;
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

    private static void printStats(Gb gb) {
        int dvs = gb.readMemory(0xD186) * 256 + gb.readMemory(0xD187);
        System.out.println(
                String.format(
                        "DVs %04X  ::  %2d/%2d/%2d/%2d/%2d",
                        dvs,
                        gb.readMemory(0xD18E),
                        gb.readMemory(0xD190),
                        gb.readMemory(0xD192),
                        gb.readMemory(0xD194),
                        gb.readMemory(0xD196))
        );
    }
}