package dabomstew.rta.mtmoon;

import dabomstew.rta.*;
import dabomstew.rta.ffef.OverworldAction;
import dabomstew.rta.ffef.OverworldEdge;
import dabomstew.rta.ffef.OverworldState;
import dabomstew.rta.ffef.OverworldTile;
import mrwint.gbtasgen.Gb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class MegaPunchExtension {
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

    static Gb gb;
    static GBMemory mem;
    static GBWrapper wrap;
    static PrintWriter writer;
    private static HashSet<String> seenStates = new HashSet<>();

    static int MAX_COST = 40;

    static ArrayList<ArrayList<Integer>> segmentPaths = new ArrayList<>();
    //static String htasStr = "[[Input 16,Input 16,Input 16,Input 16,Input 128,Input 17,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 64],[Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 33,Input 32,Input 128,Input 33,Input 32,Input 33,Input 32,Input 33,Input 128],[Input 16,Input 64,Input 16,Input 16,Input 64,Input 64],[Input 128,Input 128,Input 128,Input 128,Input 33,Input 32,Input 32],[Input 16,Input 17,Input 16,Input 64,Input 17,Input 16,Input 65,Input 16,Input 16,Input 17],[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129,Input 32,Input 32,Input 32,Input 32,Input 33],[Input 32,Input 64,Input 64,Input 32,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64],[Input 128,Input 16,Input 16,Input 129,Input 128],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 128,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 64,Input 16,Input 16,Input 64,Input 16,Input 16,Input 16],[Input 16,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 64,Input 16,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 128,Input 129,Input 128,Input 32,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 33,Input 32,Input 32,Input 32,Input 32,Input 32]]";
    /*static String htasStr = "[" +
            "[Input 16,Input 16,Input 16,Input 16,Input 128,Input 17,Input 16]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 16,Input 64,Input 64,Input 64]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 33,Input 32,Input 128,Input 32,Input 33,Input 32,Input 33,Input 32,Input 129]," +
            "[Input 64,Input 16,Input 16,Input 16,Input 64,Input 64]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 33,Input 32,Input 32]," +
            "[Input 16,Input 17,Input 16,Input 64,Input 17,Input 16,Input 65,Input 16,Input 16,Input 17]," +
            "[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129,Input 32,Input 32,Input 32,Input 33,Input 32]," +
            "[Input 32,Input 32,Input 65,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 32,Input 32]," +
            "[Input 128,Input 16,Input 16,Input 129,Input 128]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 128]," +
            "[Input 64,Input 64,Input 64,Input 17,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 17,Input 16,Input 16,Input 16]," +
            "[Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128]," +
            "[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32]]";
*/
    //static String htasStr = "[[Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 17],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16],[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 32,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 64,Input 64],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 32,Input 32,Input 32,Input 32,Input 32,Input 128,Input 128,Input 128,Input 128],[Input 32,Input 129,Input 32,Input 32,Input 33,Input 128,Input 33,Input 32,Input 32,Input 33],[Input 64,Input 16,Input 16,Input 16,Input 64,Input 65],[Input 128,Input 129,Input 128,Input 32,Input 32,Input 32,Input 128],[Input 64,Input 17,Input 16,Input 16,Input 17,Input 16,Input 17,Input 16,Input 64,Input 17],[Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 128,Input 33,Input 32,Input 32,Input 32,Input 32],[Input 64,Input 65,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 32,Input 64,Input 64,Input 65,Input 64,Input 64,Input 32,Input 64,Input 64,Input 64,Input 65,Input 32,Input 32,Input 32],[Input 128,Input 128,Input 128,Input 16,Input 16],[Input 128,Input 128,Input 128,Input 129,Input 128,Input 128,Input 16,Input 128,Input 128,Input 128,Input 128,Input 16,Input 128,Input 16,Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 16,Input 64,Input 64,Input 16,Input 17,Input 16],[Input 128,Input 128,Input 16,Input 17,Input 16,Input 16,Input 16,Input 16],[Input 16,Input 64,Input 64,Input 16,Input 16,Input 16],[Input 128,Input 129,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 128,Input 32,Input 32,Input 128,Input 32,Input 128,Input 128,Input 32,Input 128,Input 128,Input 128,Input 128,Input 129],[Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 32,Input 33,Input 32,Input 32]]";
    static String htasStr = "[" +
            "[Input 128,Input 16,Input 16,Input 16,Input 16,Input 16,Input 16]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 16,Input 64]," +
            "[Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 64,Input 32,Input 32,Input 32]," +
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


    private static OverworldTile pw11_31 = new OverworldTile(61, 11, 31, true);
    private static OverworldTile pw11_30 = new OverworldTile(61, 11, 30, true);
    private static OverworldTile pw11_29 = new OverworldTile(61, 11, 29, true);
    private static OverworldTile pw11_28 = new OverworldTile(61, 11, 28, true);
    private static OverworldTile pw11_27 = new OverworldTile(61, 11, 27, true);
    private static OverworldTile pw11_26 = new OverworldTile(61, 11, 26, true);
    private static OverworldTile pw11_25 = new OverworldTile(61, 11, 25, true);
    private static OverworldTile pw11_24 = new OverworldTile(61, 11, 24, true);
    private static OverworldTile pw11_23 = new OverworldTile(61, 11, 23, true);
    private static OverworldTile pw11_22 = new OverworldTile(61, 11, 22, true);
    private static OverworldTile pw11_21 = new OverworldTile(61, 11, 21, true);
    private static OverworldTile pw11_20 = new OverworldTile(61, 11, 20, true);
    private static OverworldTile pw10_31 = new OverworldTile(61, 10, 31, true);
    private static OverworldTile pw10_30 = new OverworldTile(61, 10, 30, true);
    private static OverworldTile pw10_29 = new OverworldTile(61, 10, 29, true);
    private static OverworldTile pw10_28 = new OverworldTile(61, 10, 28, true);
    private static OverworldTile pw10_27 = new OverworldTile(61, 10, 27, true);
    private static OverworldTile pw10_26 = new OverworldTile(61, 10, 26, true);
    private static OverworldTile pw10_25 = new OverworldTile(61, 10, 25, true);
    private static OverworldTile pw10_24 = new OverworldTile(61, 10, 24, true);
    private static OverworldTile pw10_23 = new OverworldTile(61, 10, 23, true);
    private static OverworldTile pw10_22 = new OverworldTile(61, 10, 22, true);
    private static OverworldTile pw10_21 = new OverworldTile(61, 10, 21, true);
    private static OverworldTile pw10_20 = new OverworldTile(61, 10, 20, true);
    private static OverworldTile pw10_19 = new OverworldTile(61, 10, 19, true);
    private static OverworldTile pw10_18 = new OverworldTile(61, 10, 18, true);
    private static OverworldTile pw10_17 = new OverworldTile(61, 10, 17, true);
    private static OverworldTile pw10_16 = new OverworldTile(61, 10, 16, true);
    private static OverworldTile pw9_31 = new OverworldTile(61, 9, 31, true);
    private static OverworldTile pw9_30 = new OverworldTile(61, 9, 30, true);
    private static OverworldTile pw9_29 = new OverworldTile(61, 9, 29, true);
    private static OverworldTile pw9_28 = new OverworldTile(61, 9, 28, true);
    private static OverworldTile pw9_27 = new OverworldTile(61, 9, 27, true);
    private static OverworldTile pw9_26 = new OverworldTile(61, 9, 26, true);
    private static OverworldTile pw9_25 = new OverworldTile(61, 9, 25, true);
    private static OverworldTile pw9_24 = new OverworldTile(61, 9, 24, true);
    private static OverworldTile pw9_23 = new OverworldTile(61, 9, 23, true);
    private static OverworldTile pw9_22 = new OverworldTile(61, 9, 22, true);
    private static OverworldTile pw9_21 = new OverworldTile(61, 9, 21, true);
    private static OverworldTile pw9_20 = new OverworldTile(61, 9, 20, true);
    private static OverworldTile pw9_19 = new OverworldTile(61, 9, 19, true);
    private static OverworldTile pw9_18 = new OverworldTile(61, 9, 18, true);
    private static OverworldTile pw9_17 = new OverworldTile(61, 9, 17, true);
    static OverworldTile[] owTiles = {pw11_31, pw11_30, pw11_29, pw11_28, pw11_27, pw11_26, pw11_25, pw11_24, pw11_23, pw11_22, pw11_21, pw11_20,
        pw10_31, pw10_30, pw10_29, pw10_28, pw10_27, pw10_26, pw10_25, pw10_24, pw10_23, pw10_22, pw10_21, pw10_20, pw10_19, pw10_18, pw10_17, pw10_16,
        pw9_31, pw9_30, pw9_29, pw9_28, pw9_27, pw9_26, pw9_25, pw9_24, pw9_23, pw9_22, pw9_21, pw9_20, pw9_19, pw9_18, pw9_17};

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
        String ts = Long.toString(System.currentTimeMillis());
        File file = new File("bc_encs_" + ts + ".txt");
        //File file = new File("r3lass_encs_" + ts + ".txt");
        writer = new PrintWriter(file);
        pw11_31.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_30));
        pw11_31.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_31));
        pw11_30.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_29));
        pw11_30.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_29));
        pw11_30.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_31));
        pw11_29.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_28));
        pw11_29.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_29));
        pw11_29.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_30));
        pw11_28.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_27));
        pw11_28.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_28));
        pw11_28.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_29));
        pw11_27.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_26));
        pw11_27.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_27));
        pw11_27.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_28));
        pw11_26.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_25));
        pw11_26.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_26));
        pw11_26.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_27));
        pw11_25.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_24));
        pw11_25.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_25));
        pw11_25.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_26));
        pw11_24.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_23));
        pw11_24.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_24));
        pw11_24.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_25));
        pw11_23.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_22));
        pw11_23.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_23));
        pw11_23.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_24));
        pw11_22.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_21));
        pw11_22.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_22));
        pw11_22.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_23));
        pw11_21.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw11_20));
        pw11_21.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_21));
        pw11_21.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_22));
        pw11_20.addEdge(new OverworldEdge(OverworldAction.LEFT, 0, 17, pw10_20));
        pw11_20.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw11_21));
        pw10_31.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_30));
        pw10_31.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_31));
        pw10_31.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_31));
        pw10_30.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_29));
        pw10_30.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_30));
        pw10_30.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_30));
        pw10_30.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_31));
        pw10_29.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_28));
        pw10_29.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_29));
        pw10_29.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_29));
        pw10_29.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_30));
        pw10_28.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_27));
        pw10_28.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_28));
        pw10_28.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_28));
        pw10_28.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_29));
        pw10_27.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_26));
        pw10_27.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_27));
        pw10_27.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_27));
        pw10_27.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_28));
        pw10_26.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_25));
        pw10_26.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_26));
        pw10_26.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_26));
        pw10_26.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_27));
        pw10_25.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_24));
        pw10_25.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_25));
        pw10_25.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_25));
        pw10_25.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_26));
        pw10_24.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_23));
        pw10_24.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_24));
        pw10_24.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_24));
        pw10_24.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_25));
        pw10_23.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_22));
        pw10_23.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_23));
        pw10_23.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_23));
        pw10_23.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_24));
        pw10_22.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_21));
        pw10_22.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_22));
        pw10_22.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_22));
        pw10_22.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_23));
        pw10_21.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_20));
        pw10_21.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_21));
        pw10_21.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_21));
        pw10_21.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_22));
        pw10_20.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_19));
        pw10_20.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_20));
        pw10_20.addEdge(new OverworldEdge(OverworldAction.RIGHT, 34, 17, pw11_20));
        pw10_20.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_21));
        pw10_19.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_18));
        pw10_19.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_19));
        pw10_19.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_20));
        pw10_18.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw10_17));
        pw10_18.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_18));
        pw10_18.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_19));
        pw10_17.addEdge(new OverworldEdge(OverworldAction.UP, 34, 17, pw10_16));
        pw10_17.addEdge(new OverworldEdge(OverworldAction.LEFT, 34, 17, pw9_17));
        pw10_17.addEdge(new OverworldEdge(OverworldAction.DOWN, 34, 17, pw10_18));
        pw10_16.addEdge(new OverworldEdge(OverworldAction.DOWN, 0, 17, pw10_17));
        pw9_31.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_30));
        pw9_31.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_31));
        pw9_30.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_29));
        pw9_30.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_30));
        pw9_29.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_28));
        pw9_29.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_29));
        pw9_28.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_27));
        pw9_28.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_28));
        pw9_27.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_26));
        pw9_27.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_27));
        pw9_26.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_25));
        pw9_26.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_26));
        pw9_25.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_24));
        pw9_25.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_25));
        pw9_24.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_23));
        pw9_24.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_24));
        pw9_23.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_22));
        pw9_23.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_23));
        pw9_23.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_22));
        pw9_23.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_22));
        pw9_22.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_21));
        pw9_22.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_22));
        pw9_21.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_20));
        pw9_21.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_21));
        pw9_20.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_19));
        pw9_20.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_20));
        pw9_19.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_18));
        pw9_19.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_19));
        pw9_18.addEdge(new OverworldEdge(OverworldAction.UP, 0, 17, pw9_17));
        pw9_18.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_18));
        pw9_17.addEdge(new OverworldEdge(OverworldAction.RIGHT, 0, 17, pw10_17));
        for(OverworldTile owTile : owTiles) {
            owTile.addEdge(new OverworldEdge(OverworldAction.A, 2, 2, owTile));
            owTile.addEdge(new OverworldEdge(OverworldAction.START_B, 45, 45, owTile));
            Collections.sort(owTile.getEdgeList());
        }
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
            if(i == 0) { path += "U"; }
            if(i == 1) { path += "U"; }
            if(i == 5) { path += "LUR"; }
            if(i == 9) { path += "L"; }
            if(i == 16) { path += "L"; }
        }
        path = path.replace(" ", "");
        writer.println(path);

        final String finalPath = path;

        //byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/red_moon_2932_r3lass.sav");
        byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/megapunch2.sav");
        baseSave[0x2CEF] = (byte) 20;
        baseSave[0x2CF0] = (byte) 0;
        baseSave[0x2CF1] = (byte) 6;
        int csum = 0;
        for (int i = 0x2598; i < 0x3523; i++) {
            csum += baseSave[i] & 0xFF;
        }
        baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
        FileFunctions.writeBytesToFile("roms/pokered.sav", baseSave);

        gb = new Gb(0, false);
        gb.startEmulator("roms/pokered.gbc");

        mem = new GBMemory(gb);
        wrap = new GBWrapper(gb, mem);

        // Advance through first part of intro
        // Always use fastest buffering method because there's no way
        // anything else is worth it
        //wrap.advanceWithJoypadToAddress(UP, RedBlueAddr.biosReadKeypadAddr);
        //wrap.advanceFrame(UP);
        int[] introInputs = { B | SELECT | UP, B | SELECT | UP, START };
        int introInputCtr = 0;
        while (introInputCtr < 3) {
            wrap.advanceToAddress(RedBlueAddr.joypadAddr);
            // inject intro inputs
            wrap.injectRBInput(introInputs[introInputCtr++]);
            wrap.advanceFrame();
        }

        // skip the rest of the intro
        wrap.advanceToAddress(RedBlueAddr.joypadAddr);
        wrap.injectRBInput(A);
        wrap.advanceFrame();
        wrap.advanceToAddress(RedBlueAddr.joypadAddr);
        wrap.injectRBInput(A);
        wrap.advanceFrame();
        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

        wrap.writeMemory(0xD31E, 0x04);

        int pathIdx = 0;

        // Process shit
        boolean garbage = false;
        boolean success = false;
        boolean[] itemsPickedUp = new boolean[4];
        int itemIdx = 0;

        while (pathIdx < finalPath.length() && !garbage) {
            int input = inputFromChar(finalPath.charAt(pathIdx++));
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
            }

            if (travellingToWarp(dest.map, dest.x, dest.y)) {
                wrap.advanceToAddress(RedBlueAddr.enterMapAddr);
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
                        if (hra < 10) {
                            garbage = true;
                        }
                    }
                    if (!garbage) {
                        wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);

                        // Pick up item?
                        if (timeToPickUpItem(mem.getMap(), mem.getX(), mem.getY(),
                                itemsPickedUp) && (gb.readMemory(0xC109) != 0xC || mem.getMap() != 59)) {
                            // Pick it up
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

        int hra = mem.getHRA();
        int hrs = mem.getHRS();
        int dsum = (hra + hrs) % 256;
        OverworldState owState = new OverworldState("",
                pw11_31, 0, true, gb.getDivState(), hra, hrs,
                false, mem.getTurnFrameStatus(), mem.getNPCTimers(), 0, 0);
        overworldSearch(owState);
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

            if (ow.aPressCounter() == 2 && (edgeAction == OverworldAction.START_B
                    || edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
                continue;
            }
            if (ow.aPressCounter() > 0 && edgeAction == OverworldAction.A) {
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
                                    if (mem.getHRA() <= 9) {
                                        String rngAtEnc = mem.getRNGStateWithDsum();
                                        wrap.advanceFrame();
                                        wrap.advanceFrame();
                                        Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                                mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                        int owFrames = ow.getOverworldFrames() + edge.getFrames();
                                        //  String pruneDsum = dsumPrune ? " [*]" : "";

                                        String defaultYbf = "";
                                        String redbarYbf = "";
                                        if (enc.species == 109) {
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
                                            //redbarYbf += ", HP = " + gb.readMemory(0xCFE7);
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
                                if (mem.getHRA() <= 9) {
                                    String rngAtEnc = mem.getRNGStateWithDsum();
                                    wrap.advanceFrame();
                                    wrap.advanceFrame();
                                    Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
                                            mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
                                    int owFrames = ow.getOverworldFrames() + edge.getFrames();
                                    //  String pruneDsum = dsumPrune ? " [*]" : "";

                                    String defaultYbf = "";
                                    String redbarYbf = "";
                                    if (enc.species == 109) {
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
                                        //redbarYbf += ", HP = " + gb.readMemory(0xCFE7);

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
                default:
                    break;
            }
            gb.loadState(curSave);
        }
    }

    private static int readIGT() {
        return 3600*gb.readMemory(0xDA43) + 60*gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
    }
}
