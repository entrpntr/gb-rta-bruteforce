package dabomstew.rta.tid;

import dabomstew.rta.GoldAddr;
import mrwint.gbtasgen.Gb;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;

public class GoldTIDManip {
    private static final int NO_INPUT = 0x00;
    private static final int A = 0x01;
    private static final int B = 0x02;
    private static final int START = 0x08;
    private static final int HARD_RESET = 0x800;

    private static final String gameName = "gold";

    // Change this to increase/decrease number of intro sequence combinations processed
    private static final int MAX_COST = 3000;

    private static final int BASE_COST = 332 + 32;

    private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] {GoldAddr.readJoypadAddr}, new Integer[] {START}, new Integer[] {1});
    private static Strat intro0 = new Strat("_intro0", 344, new Integer[] {GoldAddr.introScene1Addr, GoldAddr.readJoypadAddr}, new Integer[] {NO_INPUT, START}, new Integer[] {0, 1});
    private static Strat intro1 = new Strat("_intro1", 1830, new Integer[] {GoldAddr.introScene2Addr, GoldAddr.readJoypadAddr}, new Integer[] {NO_INPUT, START}, new Integer[] {0, 1});
    private static Strat intro2 = new Strat("_intro2", 2290, new Integer[] {GoldAddr.introScene3Addr, GoldAddr.readJoypadAddr}, new Integer[] {NO_INPUT, START}, new Integer[] {0, 1});

    private static Strat titleSkip = new Strat("_title", 55, new Integer[] {GoldAddr.titleScreenAddr}, new Integer[] {START}, new Integer[] {1});
    private static Strat newGame = new Strat("_newgame", 8, new Integer[] {GoldAddr.readJoypadAddr}, new Integer[] {A}, new Integer[] {16});
    private static Strat backout = new Strat("_backout", 16, new Integer[] {GoldAddr.readJoypadAddr}, new Integer[] {B}, new Integer[] {1});

    private static List<Strat> intro = Arrays.asList(gfSkip, intro0, intro1, intro2);

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
                wrap.advanceWithJoypadToAddress(input[i], addr[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame(input[i]);
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

    private static IntroSequence append(IntroSequence seq, Strat... strats) {
        IntroSequence newSeq = new IntroSequence(seq);
        newSeq.addAll(Arrays.asList(strats));
        return newSeq;
    }

    private static void addWaitPermutations(ArrayList<IntroSequence> introSequences, IntroSequence introSequence) {
        int ngmax = (MAX_COST - (introSequence.cost() + BASE_COST + 8));
        for(int i=0; ngmax>=0 && i<=ngmax/71; i++) {
            introSequences.add(append(introSequence, newGame));
            introSequence = append(introSequence, backout, titleSkip);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!new File("roms").exists()) {
            new File("roms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        File file = new File(gameName + "_tids.txt");
        PrintWriter writer = new PrintWriter(file);

        ArrayList<Strat> waitStrats = new ArrayList<>();
        int maxwaits = (MAX_COST - BASE_COST - 55 - 8)/4;
        for(int i=1; i<=maxwaits; i++) {
            Integer[] addr = new Integer[i];
            Integer[] input = new Integer[i];
            Integer[] advFrames = new Integer[i];
            for(int j=0; j<i; j++) {
                addr[j] = GoldAddr.mainMenuJoypadAddr;
                input[j] = NO_INPUT;
                advFrames[j] = 1;
            }
            waitStrats.add(new Strat("_wait" + i, i*4, addr, input, advFrames));
        }

        ArrayList<IntroSequence> introSequences = new ArrayList<>();
        for(Strat s : intro) {
            IntroSequence introSequence = new IntroSequence(s, titleSkip);
            int ngmax = (MAX_COST - (introSequence.cost() + BASE_COST + 8));
            for(int i=0; ngmax>=0 && i<=ngmax/71; i++) {
                introSequences.add(append(introSequence, newGame));
                for(Strat s2 : waitStrats) {
                    IntroSequence base = append(introSequence, s2);
                    addWaitPermutations(introSequences, base);
                }
                introSequence = append(introSequence, backout, titleSkip);
            }
        }

        System.out.println("Number of intro sequences: " + introSequences.size());
        Collections.sort(introSequences);

        // Init gambatte with 1 screen
        Gb.loadGambatte(1);
        Gb gb = new Gb(0, false);
        gb.startEmulator("roms/poke" + gameName + ".gbc");
        GBMemory mem = new GBMemory(gb);
        GBWrapper wrap = new GBWrapper(gb, mem);
        for(IntroSequence seq : introSequences) {
            seq.execute(wrap);
            int tid = readTID(gb);
            int lid = readLID(gb);
            writer.println(
                    seq.toString()
                            + ": TID = " + String.format("0x%4s", Integer.toHexString(tid).toUpperCase()).replace(' ', '0') + " (" + String.format("%5s)", tid).replace(' ', '0')
                            + ", LID = " + String.format("0x%4s", Integer.toHexString(lid).toUpperCase()).replace(' ', '0') + " (" + String.format("%5s)", lid).replace(' ', '0')
                            + ", Cost: " + (seq.cost() + BASE_COST));
            writer.flush();
            gb.step(HARD_RESET);
        }
        writer.close();
    }

    private static int readTID(Gb gb) {
        return (gb.readMemory(0xD1A1) << 8) | gb.readMemory(0xD1A2);
    }

    private static int readLID(Gb gb) {
        return (gb.readMemory(0xD9E9) << 8) | gb.readMemory(0xD9EA);
    }
}