package dabomstew.rta.tid;

import mrwint.gbtasgen.Gb;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dabomstew.rta.RedBlueAddr;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;

public class RedBlueTIDManip {
    private static final int NO_INPUT = 0x00;
    private static final int A = 0x01;
    private static final int B = 0x02;
    private static final int SELECT = 0x04;
    private static final int START = 0x08;
    private static final int UP = 0x40;
    private static final int HARD_RESET = 0x800;

    /* Change this to "blue" or "red" before running */
    private static final String gameName = "red";
    /* Change this to increase/decrease number of intro sequence combinations processed */
    private static final int MAX_COST = 2750;

    //static int BASE_COST = 492, HOP_BASE_COST = 172, HOP1_COST = 131, HOP2_COST = 190, HOP3_COST = 298, HOP4_COST = 447, HOP5_COST = 536, SCROLL_ROUGHCOST = 270, SOFT_RESET_COST = 363, NG_WINDOW_COST = 20, START_NG_COST = 20, BACKOUT_COST = 142, OAK_SPEECH_COST = 114;
    private static final int TITLE_BASE_COST = (gameName.equals("blue") ? 0 : 1);
    private static final int CRY_BASE_COST = (gameName.equals("blue") ? 96 : 88);

    private static PalStrat pal = new PalStrat("_pal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {UP}, new Integer[] {1});
    private static PalStrat nopal = new PalStrat("_nopal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {NO_INPUT}, new Integer[] {1});
    private static PalStrat abss = new PalStrat("_nopal(ab)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {A, A}, new Integer[] {0, 0});
    private static PalStrat holdpal = new PalStrat("_pal(hold)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {UP, UP}, new Integer[] {0, 0});

    private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    private static Strat gfWait = new Strat("_gfwait", 253, new Integer[] {RedBlueAddr.delayAtEndOfShootingStarAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
    private static List<Strat> gf = Arrays.asList(gfSkip, gfWait);

    private static Strat nido0 = new Strat("_hop0", 172 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    private static Strat nido1 = new Strat("_hop1", 172 + 131 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 1});
    private static Strat nido2 = new Strat("_hop2", 172 + 190 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 0, 0, 1});
    private static Strat nido3 = new Strat("_hop3", 172 + 298 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 0, 0, 0, 0, 1});
    private static Strat nido4 = new Strat("_hop4", 172 + 447 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 1});
    private static Strat nido5 = new Strat("_hop5", 172 + 536 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.displayTitleScreenAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
    private static List<Strat> nido = Arrays.asList(nido0, nido1, nido2, nido3, nido4, nido5);

    private static Strat newGame = new Strat("_newgame", 20 + 20, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A}, new Integer[] {32});
    private static Strat backout = new Strat("_backout", 142 + 20 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {B}, new Integer[] {1});

    private static ResetStrat gfReset = new ResetStrat("_gfreset", 363, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat hop0Reset = new ResetStrat("_hop0(reset)", 363 + 4, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat ngReset = new ResetStrat("_ngreset", 363, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    private static ResetStrat oakReset = new ResetStrat("_oakreset", 363 + 114, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});

    private static ResetStrat hop1Reset = new ResetStrat("_hop1(reset)", 363 + 131 + 3, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0});
    private static ResetStrat hop2Reset = new ResetStrat("_hop2(reset)", 363 + 190 + 4, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0});
    private static ResetStrat hop3Reset = new ResetStrat("_hop3(reset)", 363 + 298 + 5, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0, 0, 0});
    private static ResetStrat hop4Reset = new ResetStrat("_hop4(reset)", 363 + 447 + 4, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0});
    private static List<ResetStrat> hopResets = Arrays.asList(hop1Reset, hop2Reset, hop3Reset, hop4Reset);

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
                wrap.injectRBInput(input[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame();
                }
            }
        }
    }

    private static class PalStrat extends Strat {
        PalStrat(String name, int cost, Integer[] addr, Integer[] input, Integer[] advanceFrames) {
            super(name, cost, addr, input, advanceFrames);
        }
        @Override public void execute(GBWrapper wrap) {
            for (int i = 0; i < addr.length; i++) {
                wrap.advanceWithJoypadToAddress(input[i], addr[i]);
                wrap.advanceFrame(input[i]);
                for (int j = 0; j < advanceFrames[i]; j++) {
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
                wrap.injectRBInput(input[i]);
                for(int j=0; j<advanceFrames[i]; j++) {
                    wrap.advanceFrame();
                }
            }
            wrap.advanceWithJoypadToAddress(A | B | START | SELECT, RedBlueAddr.softResetAddr);
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

        File file = new File(gameName + "_tids.txt");
        PrintWriter writer = new PrintWriter(file);

        ArrayList<Strat> title = new ArrayList<>();
        ArrayList<Strat> titleResets = new ArrayList<>();
        ArrayList<Strat> titleusb = new ArrayList<>();
        int maxTitle = (MAX_COST - (492 + 172 + CRY_BASE_COST + TITLE_BASE_COST + 20 + 20));
        for(int i=0; maxTitle>=0 && i<=maxTitle/270; i++) {
            Integer[] addr = new Integer[i+1], input = new Integer[i+1], advFrames = new Integer[i+1];
            Integer[] rsAddr = new Integer[i+1], rsInput = new Integer[i+1], rsAdvFrames = new Integer[i+1];
            Integer[] usbAddr = new Integer[i+1], usbInput = new Integer[i+1], usbAdvFrames = new Integer[i+1];

            for(int j=0; j<i; j++) {
                addr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
                input[j] = NO_INPUT;
                advFrames[j] = 1;

                rsAddr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
                rsInput[j] = NO_INPUT;
                rsAdvFrames[j] = 1;

                usbAddr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
                usbInput[j] = NO_INPUT;
                usbAdvFrames[j] = 1;
            }

            addr[i] = RedBlueAddr.joypadAddr;
            input[i] = START;
            advFrames[i] = 1;
            title.add(new Strat("_title" + i, CRY_BASE_COST + 270*i, addr, input, advFrames));

            rsAddr[i] = RedBlueAddr.joypadAddr;
            rsInput[i] = A | B | START | SELECT;
            rsAdvFrames[i] = 1;
            titleResets.add(new ResetStrat("_title" + i + "(reset)", 363 + 270*i, rsAddr, rsInput, rsAdvFrames));

            usbAddr[i] = RedBlueAddr.joypadAddr;
            usbInput[i] = UP | SELECT | B;
            usbAdvFrames[i] = 1;
            titleusb.add(new ResetStrat("_title" + i + "(usb)_csreset", CRY_BASE_COST + 363 + 270*i, usbAddr, usbInput, usbAdvFrames));
        }

        ArrayList<IntroSequence> newGameSequences = new ArrayList<>();

        ArrayList<IntroSequence> resetSequences = new ArrayList<>();
        resetSequences.add(new IntroSequence(gfReset));
        resetSequences.add(new IntroSequence(gfWait, hop0Reset));
        resetSequences.addAll(permute(gf, hopResets));

        ArrayList<IntroSequence> s3seqs = new ArrayList<>();
        s3seqs.addAll(permute(gf, nido));

        while(!s3seqs.isEmpty()) {
            ArrayList<IntroSequence> s4seqs = new ArrayList<>();
            for(IntroSequence s3 : s3seqs) {
                int ngcost = s3.cost() + CRY_BASE_COST + 20 + 20;
                int ngmax = (MAX_COST - ngcost - 492);
                for(int i=0; ngmax>=0 && i<=ngmax/270; i++) {
                    s4seqs.add(append(s3, title.get(i)));
                }

                int rscost = ngcost + 363 + 172 + TITLE_BASE_COST;
                int rsmax = (MAX_COST - rscost - 492);
                for(int j=0; rsmax>=0 && j<=rsmax/270; j++) {
                    resetSequences.add(append(s3, titleResets.get(j)));
                    if(270*j <= MAX_COST - 492 - CRY_BASE_COST) {
                        resetSequences.add(append(s3, titleusb.get(j)));
                    }
                    if(270*j <= MAX_COST - 492 - CRY_BASE_COST - 20) {
                        resetSequences.add(append(s3, title.get(j), ngReset));
                    }
                }
            }
            s3seqs.clear();

            for(IntroSequence s4 : s4seqs) {
                IntroSequence seq = append(s4, newGame);
                newGameSequences.add(seq);

                int ngcost = s4.cost() + 20 + 20;
                if((MAX_COST - 492 - ngcost) >= 142 + TITLE_BASE_COST + CRY_BASE_COST) {
                    s3seqs.add(append(s4, backout));
                }

                int rscost = s4.cost() + 20 + 20 + 114;
                if((MAX_COST - 492 - rscost) >= 363 + 172 + TITLE_BASE_COST + CRY_BASE_COST + 20 + 20) {
                    resetSequences.add(append(s4, newGame, oakReset));
                }
            }
        }
        Collections.sort(resetSequences);

        ArrayList<IntroSequence> introSequencesTmp = new ArrayList<>(newGameSequences);
        while(!newGameSequences.isEmpty()) {
            Collections.sort(newGameSequences);
            ListIterator<IntroSequence> ngIter = newGameSequences.listIterator();
            while(ngIter.hasNext()) {
                IntroSequence ng = ngIter.next();
                ngIter.remove();
                for (IntroSequence rs : resetSequences) {
                    if (rs.cost() + ng.cost() <= MAX_COST - 492) {
                        IntroSequence newSeq = append(rs, ng);
                        introSequencesTmp.add(newSeq);
                        ngIter.add(newSeq);
                    } else {
                       break;
                    }
                }
            }
        }

        ArrayList<IntroSequence> introSequences = new ArrayList<>();
        for(IntroSequence seq : introSequencesTmp) {
            introSequences.add(append(nopal, seq));
            introSequences.add(append(pal, seq));
            introSequences.add(append(abss, seq));
            introSequences.add(append(holdpal, seq));
        }
        introSequencesTmp.clear();
        introSequencesTmp = null;

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
            writer.println(
                    seq.toString() + ": "
                            + String.format("0x%4s", Integer.toHexString(tid).toUpperCase()).replace(' ', '0')
                            + " (" + String.format("%5s)", tid).replace(' ', '0')
                            + ", Cost: " + (seq.cost() + 492));
            gb.step(HARD_RESET);
        }
        writer.flush();
        writer.close();
    }

    private static int readTID(Gb gb) {
        return (gb.readMemory(0xD359) << 8) | gb.readMemory(0xD35A);
    }
}