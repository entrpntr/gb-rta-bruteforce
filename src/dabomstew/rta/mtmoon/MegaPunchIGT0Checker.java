package dabomstew.rta.mtmoon;

import dabomstew.rta.*;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MegaPunchIGT0Checker {
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    public static final int RESET = 0x800;
    public static final int numThreads = 1;

    public static final int maxSecond = 1;
    static ArrayList<ArrayList<Integer>> segmentPaths = new ArrayList<>();
    //static String htasStr = "[[Input 16,Input 16,Input 16,Input 16,Input 128,Input 17,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 33,Input 32,Input 128,Input 33,Input 32,Input 33,Input 32,Input 33,Input 128],[Input 16,Input 64,Input 16,Input 16,Input 64,Input 64],[Input 128,Input 128,Input 128,Input 128,Input 33,Input 32,Input 32],[Input 16,Input 17,Input 16,Input 64,Input 17,Input 16,Input 65,Input 16,Input 16,Input 17],[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129,Input 32,Input 32,Input 32,Input 32,Input 33],[Input 32,Input 64,Input 64,Input 32,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64],[Input 128,Input 16,Input 16,Input 129,Input 128],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 128,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 16,Input 16,Input 64,Input 16,Input 16,Input 16],[Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 16,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 128,Input 129,Input 128,Input 32,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 33,Input 32,Input 32,Input 32,Input 32,Input 32]]";

    /*
    // r3 lass
    static String htasStr =
        "[" +
            "[Input 16,Input 16,Input 16,Input 128,Input 16,Input 17,Input 16]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 64]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 33,Input 32,Input 128,Input 32,Input 33,Input 32,Input 33,Input 32,Input 129]," +
            "[Input 64,Input 16,Input 16,Input 16,Input 64,Input 64]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 32,Input 33,Input 32]," +
//            "[Input 16,Input 17,Input 16,Input 64,Input 17,Input 16,Input 17,Input 16,Input 65,Input 16]," +
            "[Input 16,Input 17,Input 16,Input 64,Input 17,Input 16,Input 65,Input 16,Input 16,Input 17]," +
            "[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129,Input 32,Input 32,Input 32,Input 33,Input 32]," +
            "[Input 32,Input 32,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 32,Input 32]," +
            "[Input 128,Input 16,Input 16,Input 129,Input 128]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 128]," +
            "[Input 64,Input 64,Input 64,Input 17,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 17,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32]" +
        "]";
    */
/*
    static String htasStr =
        "[" +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 33,Input 32,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 128]," +
            "[Input 16,Input 16,Input 16,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 16,Input 16,Input 16,Input 64,Input 16]," +
            "[Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 64,Input 64,Input 64,Input 65,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 17,Input 16,Input 16,Input 16]," +
            "[Input 16,Input 64,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 65,Input 64]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 65,Input 64,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 129,Input 32,Input 32,Input 32,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 128,Input 128,Input 32,Input 33,Input 32,Input 33,Input 32,Input 33,Input 32,Input 33]," +
            "[Input 16,Input 16,Input 64,Input 16,Input 64,Input 64]," +
            "[Input 128,Input 128,Input 128,Input 32,Input 32,Input 129,Input 32]," +
            "[Input 16,Input 17,Input 16,Input 64,Input 65,Input 16,Input 17,Input 16,Input 16,Input 17]," +
            "[Input 128,Input 129,Input 128,Input 32,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 33,Input 32]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 33,Input 32,Input 64,Input 65,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 33,Input 32]," +
            "[Input 16,Input 17,Input 128,Input 129,Input 128]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 128]," +
            "[Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 17,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 129,Input 128,Input 128,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32]," +
            "[Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16]" +
        "]";
//*/
    //baton pass
    //static String htasStr = "[[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 33,Input 32,Input 32,Input 32],[Input 32,Input 32,Input 32,Input 32,Input 128],[Input 16,Input 64,Input 16,Input 64,Input 16,Input 16,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 16,Input 16,Input 16,Input 64,Input 16],[Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 64,Input 64,Input 64,Input 65,Input 64,Input 16,Input 64,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 17,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 16,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 65,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 65,Input 64,Input 64,Input 32],[Input 32,Input 32,Input 32,Input 32,Input 128,Input 129,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128],[Input 128,Input 32,Input 129,Input 32,Input 33,Input 32,Input 33,Input 32,Input 33,Input 32],[Input 16,Input 16,Input 64,Input 16,Input 64,Input 64],[Input 128,Input 128,Input 128,Input 32,Input 32,Input 129,Input 32],[Input 16,Input 17,Input 16,Input 64,Input 65,Input 16,Input 17,Input 16,Input 17,Input 16],[Input 128,Input 128,Input 129,Input 32,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 33,Input 32],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 33,Input 32,Input 64,Input 65,Input 64,Input 32,Input 64,Input 64,Input 32,Input 64,Input 64,Input 33,Input 32],[Input 16,Input 17,Input 128,Input 129,Input 128],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 128],[Input 64,Input 16,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 129,Input 128,Input 128,Input 128,Input 32,Input 128,Input 32,Input 129,Input 128,Input 128,Input 128,Input 32,Input 128,Input 128,Input 32,Input 128],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32],[Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64]]";

    //static String htasStr = "[[Input 16,Input 16,Input 128,Input 16,Input 16,Input 16,Input 17],[Input 64,Input 64,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64],[Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 128,Input 128,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 33,Input 128,Input 32,Input 33,Input 32,Input 33,Input 128,Input 32,Input 33],[Input 16,Input 17,Input 64,Input 16,Input 64,Input 64],[Input 128,Input 128,Input 128,Input 128,Input 32,Input 33,Input 32],[Input 16,Input 16,Input 17,Input 64,Input 17,Input 16,Input 17,Input 16,Input 16,Input 65],[Input 128,Input 128,Input 128,Input 129,Input 128,Input 32,Input 128,Input 32,Input 32,Input 33,Input 32,Input 32],[Input 32,Input 64,Input 64,Input 64,Input 65,Input 64,Input 64,Input 33,Input 32,Input 32,Input 64,Input 64,Input 65,Input 32,Input 64,Input 64,Input 64,Input 65,Input 32,Input 32,Input 64,Input 64],[Input 16,Input 16,Input 128,Input 128,Input 128],[Input 128,Input 16,Input 128,Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 128,Input 16,Input 16],[Input 64,Input 16,Input 64,Input 16,Input 64,Input 16,Input 16,Input 16],[Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 64,Input 16,Input 16,Input 17],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 128,Input 128,Input 32],[Input 32,Input 32,Input 33,Input 32,Input 32,Input 33,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 33,Input 32,Input 32,Input 33]]";

    ///*
    // bug catcher
    static String htasStr = "[" +
            "[Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 33,Input 32,Input 128,Input 32,Input 33,Input 32,Input 33,Input 32,Input 129]," +
            "[Input 64,Input 16,Input 16,Input 16,Input 64,Input 64]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 32,Input 33,Input 32]," +
            "[Input 16,Input 17,Input 16,Input 64,Input 65,Input 16,Input 17,Input 16,Input 16,Input 17]," +
            "[Input 128,Input 129,Input 128,Input 128,Input 32,Input 128,Input 128,Input 32,Input 32,Input 32,Input 33,Input 32]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 33,Input 32,Input 64,Input 64,Input 64,Input 65,Input 64,Input 32,Input 64,Input 64,Input 65,Input 64,Input 64,Input 32,Input 32]," +
            "[Input 16,Input 128,Input 129,Input 128,Input 16]," +
            "[Input 128,Input 129,Input 128,Input 16,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 65,Input 64,Input 16,Input 17,Input 16,Input 16,Input 16]," +
            "[Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 65,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 33,Input 32,Input 32,Input 33,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32]," +
            "]";
    //*/

    //static String htasStr = "[[Input 64,Input 64,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 129,Input 128,Input 128],[Input 128,Input 128,Input 128,Input 129,Input 128]]";

    //static String htasStr = "[[Input 64,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 64,Input 64,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 17,Input 16],[Input 128,Input 16,Input 128,Input 128,Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 128,Input 128]]";

    //static String htasStr = "[[Input 64,Input 64,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 64,Input 64,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 128,Input 128,Input 128,Input 128,Input 128]]";

    //static String htasStr = "[[Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 17],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 129,Input 32,Input 32,Input 33,Input 128,Input 33,Input 32,Input 32,Input 33],[Input 64,Input 16,Input 16,Input 16,Input 64,Input 65],[Input 128,Input 129,Input 128,Input 32,Input 32,Input 32,Input 128],[Input 64,Input 17,Input 16,Input 16,Input 17,Input 16,Input 17,Input 16,Input 64,Input 17],[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 33,Input 32,Input 32,Input 32,Input 32],[Input 64,Input 65,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 32,Input 64,Input 64,Input 65,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 65,Input 32,Input 32,Input 32],[Input 128,Input 128,Input 128,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 129,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 16,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 16,Input 64,Input 64,Input 16,Input 17,Input 16],[Input 128,Input 128,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 128,Input 32,Input 128,Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 33,Input 32,Input 32]]";
    //static String htasStr = "[[Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 128,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 128,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 32,Input 33,Input 32,Input 129,Input 128,Input 33,Input 32,Input 32,Input 33],[Input 16,Input 16,Input 64,Input 16,Input 64,Input 64],[Input 128,Input 128,Input 129,Input 128,Input 32,Input 32,Input 32],[Input 64,Input 16,Input 17,Input 16,Input 17,Input 16,Input 65,Input 16,Input 16,Input 17],[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 33,Input 32],[Input 64,Input 32,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 65,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 65,Input 32,Input 32],[Input 16,Input 129,Input 128,Input 16,Input 128],[Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 16,Input 65,Input 16,Input 64,Input 16,Input 17,Input 16],[Input 128,Input 17,Input 128,Input 16,Input 16,Input 16,Input 17,Input 16],[Input 64,Input 64,Input 17,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 128,Input 33,Input 128,Input 128,Input 128,Input 32,Input 128,Input 128,Input 128,Input 32,Input 128,Input 128],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32]]";
    //public static PrintStream ps;

    //static String htasStr = "[[Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 64,Input 16,Input 64,Input 16,Input 16,Input 16,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 65,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 129,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 128,Input 129,Input 128,Input 128,Input 128]]";

    //prenerd non-redbar
    //static String htasStr="[[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 17,Input 16,Input 16],[Input 64,Input 65,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 17,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16],[Input 128,Input 129,Input 128,Input 128,Input 128]]";

    //prenerd2 non-redbar
    //static String htasStr="[[Input 64,Input 65,Input 64,Input 64,Input 64,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128],[Input 128,Input 129]]";

    //static String htasStr="[[Input 64,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 16,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 17,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128],[Input 128,Input 128,Input 129,Input 128,Input 128]]";

    static void processHtas() {
        htasStr = htasStr.replaceAll("Input ", "");
        htasStr = htasStr.replace("[", "");
        String[] segments = htasStr.split("]");
        for(String s : segments) {
            String[] sPath = s.split(",");
            ArrayList<Integer> inputs = new ArrayList<>();
            for(String s2 : sPath) {
                if(!s2.equals("")) {
                    inputs.add(Integer.parseInt(s2));
                }
            }
            segmentPaths.add(inputs);
        }
    }

    public static void main(String[] args) throws IOException {
        // Mt.Moonius
        Gb.loadGambatte(numThreads);

        processHtas();
        String path = "";

        for(int i=0;i<segmentPaths.size();i++) {
            for(int input : segmentPaths.get(i)) {
                if((input & 1) != 0) {
                    path += "A"+ Func.inputName(input & 0xFE);
                }
                else {
                    path += Func.inputName(input);
                }
            }
            ///*
            if(i == 0) { path += "U"; }
            if(i == 1) { path += "U"; }
            if(i == 5) { path += "LUR"; }
            if(i == 9) { path += "L"; }
            //*/
            /*
            if(i == 1) { path += "D"; }
            if(i == 5) { path += "R"; }
            if(i == 6) { path += "R"; }
            if(i == 10) { path += "LUR"; }
            if(i == 14) { path += "L"; }
            */
        }
        // comment next 2 lines out if using a complete htas path
        path += "L";
        //path += "U U U U A L U L U U U U U R U A U U U";
        //path += "U U U U A U U U A U U L L U U R U U U"; // r3 lass
        //path += "U U U U U U U U U U U L U U U";
        //path += "L U U A U U U A U U U U U A U U U U";

        //path += "U U U U R R R R R A R R L L L L L L L D D D D D D";

        path += "U U L U U U U U U U U A U U U U"; // non-redbar, 0 tiles wasted
        //path += "U U L U U U U U U U L U U U A U U"; // non-redbar
        //path += "U U U U U U U L L U U R U U U A U"; //redbar
        //path = "U U U U U R U U U U U U R R R R R R U U U U U U U U U U U U U U R R R R R R R U U U U U U U R R R D A D D D D D D D D D D R D D D D";
        path = path.replace(" ", "");
        System.out.println(path);
        System.out.println("# A presses: " + path.chars().filter(num -> num == 'A').count());

        final String finalPath = path;

        //byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/prenerd_paras2.sav");

        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/megapunch2.sav");
        //byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/red_moon_2932_r3lass.sav");
        //byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/baton_pass.sav");
        baseSave[0x2CEF] = (byte) 20;
        baseSave[0x2CF0] = (byte) 22;
        baseSave[0x2CF1] = (byte) 0;
        //baseSave[0x2D3A] = (byte) 0;
        //baseSave[0x2D3B] = (byte) 0;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("testroms/pokered.sav", baseSave);

        Gb[] gbs = new Gb[numThreads];
        GBMemory[] mems = new GBMemory[numThreads];
        GBWrapper[] wraps = new GBWrapper[numThreads];

        for (int i = 0; i < numThreads; i++) {
            gbs[i] = new Gb(i, false);
            gbs[i].startEmulator("testroms/pokered.gbc");
            gbs[i].step(0); // let gambatte initialize itself
            mems[i] = new GBMemory(gbs[i]);
            wraps[i] = new GBWrapper(gbs[i], mems[i]);
        }

        // Advance through first part of intro
        // Always use fastest buffering method because there's no way
        // anything else is worth it
        //wraps[0].advanceWithJoypadToAddress(A, RedBlueAddr.biosReadKeypadAddr);
        //wraps[0].advanceFrame(A);
        int[] introInputs = { B | SELECT | UP, B | SELECT | UP, START };
        int introInputCtr = 0;
        while (introInputCtr < 3) {
            wraps[0].advanceToAddress(RedBlueAddr.joypadAddr);
            // inject intro inputs
            wraps[0].injectRBInput(introInputs[introInputCtr++]);
            wraps[0].advanceFrame();
        }

        // Advance to IGT inject point
        wraps[0].advanceToAddress(RedBlueAddr.igtInjectAddr);
        ByteBuffer state = gbs[0].saveState();

        // Setup for threading
        boolean[][] successes = new boolean[60][60];

        // Deal with save
        long startAll = System.currentTimeMillis();
        for (int igtsec = 0; igtsec < maxSecond; igtsec++) {
            System.out.println("Second " + igtsec);
            for (int igt0 = 0; igt0 < 60; igt0++) {
                final int num = 0;
                final int sec = igtsec;
                final int frm = igt0;

                Gb gb = gbs[num];
                GBMemory mem = mems[num];
                GBWrapper wrap = wraps[num];
                System.out.print("S" + sec + " F" + frm);
                long startFrame = System.currentTimeMillis();
                gb.loadState(state);
                gb.writeMemory(0xda44, sec);
                gb.writeMemory(0xda45, frm);

                // skip the rest of the intro
                wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                wrap.injectRBInput(A);
                wrap.advanceFrame();
                wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                wrap.injectRBInput(A);
                wrap.advanceFrame();


                //if(frm == 6) {
                //    wrap.advanceToAddress(RedBlueAddr.enterMapAddr);
                //    System.out.println("IGT0: " + gb.readMemory(0xDA45) + ", Map: " + gb.readMemory(0xD35E));
                //    wrap.advanceToAddress(0xC335);
                 //   System.out.println("IGT0: " + gb.readMemory(0xDA45) + ", Map: " + gb.readMemory(0xD35E));
                //}

                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                //wrap.writeMemory(0xD31E,0x04);

            wrap.writeMemory(0xD16D, 0x14);

                //wrap.writeMemory(0xD31D, 0x10);
                //wrap.writeMemory(0xD322, 0xFF);

                //wrap.writeMemory(0xD31D, 0x04);
                //wrap.writeMemory(0xD324, 0x0B);
                //wrap.writeMemory(0xD325, 0x01);
                //wrap.writeMemory(0xD326, 0xFF);

                int pathIdx = 0;

                // Process shit
                boolean garbage = false;
                boolean success = false;
                boolean[] itemsPickedUp = new boolean[4];
                //boolean[] itemsPickedUp = new boolean[5];
                int itemIdx = 0;
                while (pathIdx < finalPath.length() && !garbage) {
                    int input = inputFromChar(finalPath.charAt(pathIdx++));
                    //System.out.println("Input: " + input);
                    // Execute the action
                    Position dest = getDestination(mem, input);
                    wrap.injectRBInput(input);
                    if(input == B) {
                        wrap.advanceFrame();
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    }
                    else if(input == START) {
                        wrap.advanceFrame();
                        wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                    } else {
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                        //System.out.println("TEST x");

                    }

                    if (travellingToWarp(dest.map, dest.x, dest.y)) {
                        //int restest = 0;
                        //while(restest != RedBlueAddr.enterMapAddr) {
                            //restest = wrap.advanceToAddress(RedBlueAddr.enterMapAddr, 0x2024);
                            //if (frm == 1) {
                                //System.out.println("IGT0: " + gb.readMemory(0xDA45) + ", Counter: " + gb.readMemory(0xD736));
                                //wrap.advanceToAddress(0xC335);
                                //System.out.println("IGT0: " + gb.readMemory(0xDA45) + ", Map: " + gb.readMemory(0xD35E));
                            //}
                            //if(restest == 0x2024) {
                            //    wrap.advanceToAddress(0x2025);
                            //}
                        //}
                        //if (frm == 1) {
                            //System.out.println("TRUE IGT0: " + gb.readMemory(0xDA45) + ", Counter: " + gb.readMemory(0xD736));
                            //wrap.advanceToAddress(0xC335);
                            //System.out.println("IGT0: " + gb.readMemory(0xDA45) + ", Map: " + gb.readMemory(0xD35E));
                        //}
                        wrap.advanceToAddress(RedBlueAddr.enterMapAddr);
                        //if(frm == 6) {
                        //    System.out.println("IGT0: " + gb.readMemory(0xDA45));
                        //}
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                    } else if(input != START && input != B){
                        int result = wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr,
                                RedBlueAddr.newBattleAddr);

                        // Did we turnframe or hit an
                        // ignored input frame after
                        // a
                        // warp?
                        while (mem.getX() != dest.x || mem.getY() != dest.y) {
                            if (result == RedBlueAddr.newBattleAddr) {
                                // Check for garbage
                                int result2 = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                        RedBlueAddr.joypadOverworldAddr);

                                if (result2 == RedBlueAddr.encounterTestAddr) {
                                    // Yes we can. What's up
                                    // on this tile?
                                    int hra = mem.getHRA();
                                    // logLN("hrandom add was "+hra);
                                    if (hra < 10) {
                                        int hrs = mem.getHRS();
                                        int divState = gb.getDivState();
                                        wrap.advanceFrame();
                                        wrap.advanceFrame();
                                        System.out.print(
                                                String.format("[%s] Encounter at map %d x %d y %d Species %d Level %d DVs %04X rdiv %04X hra %02X hrs %02X [turnframe]",
                                                        success ? "S" : "F", mem.getMap(), mem.getX(),
                                                        mem.getY(), mem.getEncounterSpecies(),
                                                        mem.getEncounterLevel(), mem.getEncounterDVs(), divState, hra, hrs));
                                        garbage = true;
                                        break;
                                    }
                                }
                            }
                            // Do that input again
                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                            wrap.injectRBInput(input);
                            wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr + 1);
                            result = wrap.advanceToAddress(RedBlueAddr.newBattleAddr,
                                    RedBlueAddr.joypadOverworldAddr);
                        }
                        // Can we get an encounter now?
                        if (!garbage) {
                            int result2 = wrap.advanceToAddress(RedBlueAddr.encounterTestAddr,
                                    RedBlueAddr.joypadOverworldAddr);

                            if (result2 == RedBlueAddr.encounterTestAddr) {

                                // Yes we can. What's up on
                                // this tile?
                                int hra = mem.getHRA();
                                // logLN("hrandom add was "+hra);
                                if (hra < 10) {
                                    // is this actually
                                    // good?
                                    int hrs = mem.getHRS();
                                    int divState = gb.getDivState();
                                    wrap.advanceFrame();
                                    wrap.advanceFrame();
                                    //wrap.advanceFrame();
                                    //wrap.advanceFrame();
                                    boolean ybfFailed = false;
                                    String battleRNG = "";
                                    if(mem.getMap() == 61 && mem.getX() <= 18 && mem.getY() <= 31) {

                                    //if(mem.getMap() == 59 && mem.getX() == 25 && mem.getY() > 26) {
                                        if(mem.getEncounterSpecies() == 109) {
                                            success = true;
/*
                                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                            //wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                            //System.out.println();
                                            //ByteBuffer savestate = gb.saveState();
                                            //int j = 0;
                                            //for(int i=2; i<3; i++) {
                                               // for(j=0; j<12; j++) {
                                                    //gb.loadState(savestate);
                                                    //for (int k = 0; k < j; k++) {
                                                        //wrap.advanceFrame();
                                                        //wrap.advanceToAddress(RedBlueAddr.joypadAddr + 2);
                                                        //wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    //}
                                                    wrap.injectRBInput(A);
                                                    wrap.advanceFrame();
                                                    wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
                                                    wrap.injectRBInput(A);
                                                    wrap.advanceFrame();
                                                    wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.joypadAddr);
                                                    wrap.injectRBInput(UP);
                                                    wrap.advanceFrame();
                                                    //wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.joypadAddr + 2);
                                                    //wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    wrap.advanceFrame();
                                                    wrap.advanceFrame();
                                                    wrap.advanceFrame();

                                            //wrap.advanceFrame();wrap.advanceFrame();
                                                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    wrap.injectRBInput(A);
                                                    wrap.advanceFrame();
                                                    //System.out.println ("TEST");
                                                    wrap.advanceWithJoypadToAddress(A, RedBlueAddr.manualTextScrollAddr);
                                                    //battleRNG = " " + gb.readMemory(0xD05F) + " -- " + gb.readMemory(0xCFE7);
                                                    //System.out.println(battleRNG);
                                             //   }
                                           // }
*/
                                            //System.out.println();
                                            //ByteBuffer savestate = gb.saveState();
/*
                                            for(int i=0; i<1; i++) {
                                                for(int j=0; j<16; j++) {
                                                    gb.loadState(savestate);
                                                    for(int k=0; k<i; k++) {
                                                        wrap.advanceToAddress(RedBlueAddr.joypadAddr + 2);
                                                        wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    }
                                                    wrap.injectRBInput(UP);
                                                    wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.joypadAddr + 2);
                                                    wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    for(int k=0; k<j; k++) {
                                                        wrap.advanceToAddress(RedBlueAddr.joypadAddr + 2);
                                                        wrap.advanceToAddress(RedBlueAddr.joypadAddr);
                                                    }
                                                    wrap.injectRBInput(A);
                                                    wrap.advanceFrame(A);
                                                    //while(true) {
                                                        int res1 = wrap.advanceWithJoypadToAddress(A, RedBlueAddr.manualTextScrollAddr);
                                                        //wrap.injectRBInput(B);
                                                        //wrap.advanceFrame(B);
                                                        //if (res1 == 0x30E8 && gb.readMemory(0xD125) != 0x01) {
                                                        //    break;
                                                        //}
                                                    //}
                                                    battleRNG = " " + i + ", " + j + ": " + gb.readMemory(0xD05F) + " -- " + gb.readMemory(0xCFE7);
                                                    System.out.println(battleRNG);
                                                }
                                            }*/
//*/
                                            ///*
                                            wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
                                            wrap.injectRBInput(A);
                                            wrap.advanceFrame();
                                            wrap.advanceToAddress(RedBlueAddr.playCryAddr);
                                            wrap.injectRBInput(DOWN | A);
                                            wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
                                            wrap.injectRBInput(A | RIGHT);
                                            int result3 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr, RedBlueAddr.catchFailureAddr);
                                            if(result3 == RedBlueAddr.catchFailureAddr) {
                                                ybfFailed = true;
                                                success = false;
                                            }
                                            //*/
                                        }
                                    }
                                    System.out.print(
                                            String.format("[%s] Encounter at map %d x %d y %d Species %d Level %d DVs %04X rdiv %04X hra %02X hrs %02X %s",
                                                    success ? "S" : "F", mem.getMap(), mem.getX(),
                                                    mem.getY(), mem.getEncounterSpecies(),
                                                    mem.getEncounterLevel(), mem.getEncounterDVs(), divState, hra, hrs, ybfFailed ? "**YOLOBALL FAIL**" : ""));
                                    // trash state, throw it
                                    // in the bin
                                    // (encounter)
                                    garbage = true;
                                }
                            }
                            if (!garbage) {
                                wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

                                // Pick up item?
                                if (timeToPickUpItem(mem.getMap(), mem.getX(), mem.getY(),
                                        itemsPickedUp) && (gb.readMemory(0xC109) != 0xC || mem.getMap() != 59)) {
                                //if (timeToPickUpItem2(mem.getMap(), mem.getX(), mem.getY(), itemsPickedUp)) {                                    // Pick it up
                                    wrap.injectRBInput(A);
                                    wrap.advanceWithJoypadToAddress(A,
                                            RedBlueAddr.textJingleCommandAddr);
                                    wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
                                    itemsPickedUp[itemIdx++] = true;
                                }
                            }
                        }

                    }
                }

                if (!garbage) {
                    //success = true;
                    System.out.print(
                            String.format("[F] No encounter at map %d x %d y %d rdiv %04X hra %02X hrs %02X", mem.getMap(),
                                mem.getX(), mem.getY(), gb.getDivState(), mem.getHRA(), mem.getHRS()));
                }

                //System.out.println(String.format(" (time=%dms)", System.currentTimeMillis() - startFrame));
                System.out.println();
                successes[sec][frm] = success;
            }
        };

        // Print results
        int numSuccesses = 0;
        for (int sec = 0; sec < maxSecond; sec++) {
            for (int frm = 0; frm < 60; frm++) {
                numSuccesses += successes[sec][frm] ? 1 : 0;
            }
        }

        System.out.println(String.format("%d worked found (time=%dms).", numSuccesses, System.currentTimeMillis() - startAll));

        //ps.close();
    }

    private static int inputFromChar(char charVal) {
        switch (charVal) {
            case 'U':
                return UP;
            case 'D':
                return DOWN;
            case 'L':
                return LEFT;
            case 'R':
                return RIGHT;
            case 'A':
                return A;
            case 'B':
                return B;
            case 'S':
                return START;
            default:
                return 0;
        }
    }

    private static boolean travellingToWarp(int map, int x, int y) {
        if (map == 59) {
            if (x == 5 && y == 5) {
                return true;
            } else if (x == 17 && y == 11) {
                return true;
            }
        } else if (map == 60) {
            if (x == 25 && y == 9) {
                return true;
            } else if (x == 17 && y == 11) {
                return true;
            } else if (x == 21 && y == 17) {
                return true;
            }
        } else {
            if (x == 25 && y == 9) {
                return true;
            }
        }
        return false;
    }

    public static boolean timeToPickUpItem(int map, int x, int y, boolean[] pickedUpItems) {
        if (map == 61 && !pickedUpItems[2] && x == 28 && y == 5) {
            return true;
        } else if (map == 59) {
            if (!pickedUpItems[0] && x == 35 && y == 32) {
                return true;
            } else if (!pickedUpItems[1] && x == 36 && y == 24) {
                return true;
            } else if (!pickedUpItems[3] && x == 3 && y == 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean timeToPickUpItem2(int map, int x, int y, boolean[] pickedUpItems) {
        if (map == 61 && !pickedUpItems[3] && x == 28 && y == 5) {
            return true;
        } else if (map == 59) {
            if (!pickedUpItems[0] && x == 5 && y == 31) {
                return true;
            } else if (!pickedUpItems[1] && x == 34 && y == 31) {
                return true;
            } else if (!pickedUpItems[2] && x == 35 && y == 23) {
                return true;
            } else if (!pickedUpItems[4] && x == 3 && y == 2) {
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
}
