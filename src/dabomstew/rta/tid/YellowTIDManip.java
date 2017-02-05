package dabomstew.rta.tid;

import dabomstew.rta.YellowAddr;
import mrwint.gbtasgen.Gb;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;

public class YellowTIDManip {
    private static final int NO_INPUT = 0x00;
    private static final int A = 0x01;
    private static final int B = 0x02;
    private static final int SELECT = 0x04;
    private static final int START = 0x08;
    private static final int UP = 0x40;
    private static final int HARD_RESET = 0x800;

    /* Change this to increase/decrease number of intro sequence combinations processed */
    private static final int MAX_COST = 3000;
    /* Change this to include/disclude the intro buffers with smaller windows for success */
    private static final boolean includeTightWindows = true;

    private static Strat gfSkip = new Strat("_gfskip", 61, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {START}, new Integer[] {1});
    private static Strat gfWait = new Strat("_gfwait", 61+253, new Integer[] {YellowAddr.delayAtEndOfShootingStarAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
    private static List<Strat> gf = Arrays.asList(gfSkip, gfWait);

    private static Strat intro0 = new Strat("_intro0", 147, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A}, new Integer[] {1});
    private static Strat intro1 = new Strat("_intro1", 147 + 140, new Integer[] {YellowAddr.intro2Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat intro2 = new Strat("_intro2", 147 + 275, new Integer[] {YellowAddr.intro4Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat intro3 = new Strat("_intro3", 147 + 411, new Integer[] {YellowAddr.intro6Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat intro4 = new Strat("_intro4", 147 + 594, new Integer[] {YellowAddr.intro8Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat intro5 = new Strat("_intro5", 147 + 729, new Integer[] {YellowAddr.intro10Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat intro6 = new Strat("_intro6", 147 + 864, new Integer[] {YellowAddr.intro12Addr, YellowAddr.joypadAddr}, new Integer[] {NO_INPUT, A}, new Integer[] {0, 1});
    private static Strat introwait = new Strat("_introwait", 147 + 1199, new Integer[] {YellowAddr.titleScreenAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
    private static List<Strat> intro = Arrays.asList(intro0, intro1, intro2, intro3, intro4, intro5, intro6, introwait);

    private static Strat newGame = new Strat("_newgame", 20 + 20, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A}, new Integer[] {40});
    private static Strat backout = new Strat("_backout", 140 + 20, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {B}, new Integer[] {1});
    private static Strat title = new Strat("_title", 90, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {START}, new Integer[] {1});

    private static ResetStrat gfReset = new ResetStrat("_gfreset", 371, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro0Reset = new ResetStrat("_intro0(reset)", 371, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro1Reset = new ResetStrat("_intro1(reset)", 371 + 140, new Integer[] {YellowAddr.intro2Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro2Reset = new ResetStrat("_intro2(reset)", 371 + 275, new Integer[] {YellowAddr.intro4Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro3Reset = new ResetStrat("_intro3(reset)", 371 + 411, new Integer[] {YellowAddr.intro6Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro4Reset = new ResetStrat("_intro4(reset)", 371 + 594, new Integer[] {YellowAddr.intro8Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro5Reset = new ResetStrat("_intro5(reset)", 371 + 729, new Integer[] {YellowAddr.intro10Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat intro6Reset = new ResetStrat("_intro6(reset)", 371 + 864, new Integer[] {YellowAddr.intro12Addr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static List<Strat> introReset = Arrays.asList(intro0Reset, intro1Reset, intro2Reset, intro3Reset, intro4Reset, intro5Reset, intro6Reset);

    private static ResetStrat ngReset = new ResetStrat("_ngreset", 371, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat oakReset = new ResetStrat("_oakreset", 371 + 119, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat titleReset = new ResetStrat("_title(reset)", 371, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat titleUsb = new ResetStrat("_title(usb)_csreset", 371 + 90, new Integer[] {YellowAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});

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
                wrap.advanceToAddress(addr[i]);
                wrap.injectYellowInput(input[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame();
                }
            }
        }
    }

    private static class ResetStrat extends Strat {
        ResetStrat(String name, int cost, Integer[] addr, Integer[] input, Integer[] advanceFrames) {
            super(name, cost, addr, input, advanceFrames);
        }
        @Override public void execute(GBWrapper wrap) {
            for(int i=0; i<addr.length; i++) {
                wrap.advanceToAddress(addr[i]);
                wrap.injectYellowInput(input[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame();
                }
            }
            wrap.advanceWithJoypadToAddress(A | B | START | SELECT, YellowAddr.softResetAddr);
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
            String ret = "yellow";
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

    private static ArrayList<IntroSequence> permute(List<? extends Strat> sl1, List<? extends Strat> sl2) {
        ArrayList<IntroSequence> seqs = new ArrayList<>();
        for(Strat s1 : sl1) {
            for(Strat s2 : sl2) {
                IntroSequence seq = new IntroSequence();
                seq.add(s1);
                seq.add(s2);
                seqs.add(seq);
            }
        }
        return seqs;
    }

    private static IntroSequence append(IntroSequence seq, Strat... strats) {
        IntroSequence newSeq = new IntroSequence(seq);
        newSeq.addAll(Arrays.asList(strats));
        return newSeq;
    }

    private static IntroSequence append(IntroSequence seq1, IntroSequence seq2) {
        IntroSequence newSeq = new IntroSequence(seq1);
        newSeq.addAll(seq2);
        return newSeq;
    }

    private static IntroSequence append(Strat strat, IntroSequence seq) {
        IntroSequence newSeq = new IntroSequence(strat);
        newSeq.addAll(seq);
        return newSeq;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!new File("roms").exists()) {
            new File("roms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }

        File file = new File("yellow_tids.txt");
        PrintWriter writer = new PrintWriter(file);

        ArrayList<IntroSequence> newGameSequences = new ArrayList<>();

        ArrayList<IntroSequence> resetSequences = new ArrayList<>();
        resetSequences.add(new IntroSequence(gfReset));

        if(includeTightWindows) {
            resetSequences.addAll(permute(gf, introReset));
        } else {
            resetSequences.add(new IntroSequence(gfSkip, intro0Reset));
            resetSequences.add(new IntroSequence(gfWait, intro0Reset));
        }

        ArrayList<IntroSequence> s3seqs = new ArrayList<>();
        if(includeTightWindows) {
            s3seqs.addAll(permute(gf, intro));
        } else {
            s3seqs.add(new IntroSequence(gfSkip, intro0));
            s3seqs.add(new IntroSequence(gfWait, intro0));
            s3seqs.add(new IntroSequence(gfSkip, introwait));
            s3seqs.add(new IntroSequence(gfWait, introwait));
        }

        while(!s3seqs.isEmpty()) {
            ArrayList<IntroSequence> s4seqs = new ArrayList<>();
            for(IntroSequence s3 : s3seqs) {
                int ngcost = s3.cost() + 90 + 20 + 20;
                int ngmax = (MAX_COST - ngcost - 498);
                if(ngmax>=0) {
                    s4seqs.add(append(s3, title));
                }

                int rscost = ngcost + 371 + 61 + 147;
                int rsmax = (MAX_COST - rscost - 498);
                if(rsmax >= 0) {
                    resetSequences.add(append(s3, titleReset));
                    resetSequences.add(append(s3, titleUsb));
                    resetSequences.add(append(s3, title, ngReset));
                }
            }
            s3seqs.clear();

            for(IntroSequence s4 : s4seqs) {
                IntroSequence seq = append(s4, newGame);
                newGameSequences.add(seq);

                int ngcost = s4.cost() + 20 + 20;
                if((MAX_COST - 498 - ngcost) >= 140 + 90) {
                    s3seqs.add(append(s4, backout));
                }

                int rscost = s4.cost() + 20 + 20 + 119;
                if((MAX_COST - 498 - rscost) >= 371 + 61 + 147 + 90 + 20 + 20) {
                    resetSequences.add(append(s4, newGame, oakReset));
                }
            }
        }
        Collections.sort(resetSequences);

        ArrayList<IntroSequence> introSequences = new ArrayList<>(newGameSequences);
        while(!newGameSequences.isEmpty()) {
            Collections.sort(newGameSequences);
            ListIterator<IntroSequence> ngIter = newGameSequences.listIterator();
            while(ngIter.hasNext()) {
                IntroSequence ng = ngIter.next();
                ngIter.remove();
                for (IntroSequence rs : resetSequences) {
                    if (rs.cost() + ng.cost() <= MAX_COST - 498) {
                        IntroSequence newSeq = append(rs, ng);
                        introSequences.add(newSeq);
                        ngIter.add(newSeq);
                    } else {
                        break;
                    }
                }
            }
        }
        
        System.out.println("Number of intro sequences: " + introSequences.size());
        Collections.sort(introSequences);

        // Init gambatte with 1 screen
        Gb.loadGambatte(1);
        Gb gb = new Gb(0, false);
        gb.startEmulator("roms/pokeyellow.gbc");
        GBMemory mem = new GBMemory(gb);
        GBWrapper wrap = new GBWrapper(gb, mem);
        for(IntroSequence seq : introSequences) {
            seq.execute(wrap);
            int tid = readTID(gb);
            writer.println(
                    seq.toString() + ": "
                            + String.format("0x%4s", Integer.toHexString(tid).toUpperCase()).replace(' ', '0')
                            + " (" + String.format("%5s)", tid).replace(' ', '0')
                            + ", Cost: " + (seq.cost() + 498));
            writer.flush();
            gb.step(HARD_RESET);
        }
        writer.close();
    }

    private static int readTID(Gb gb) {
        return (gb.readMemory(0xD358) << 8) | gb.readMemory(0xD359);
    }
}