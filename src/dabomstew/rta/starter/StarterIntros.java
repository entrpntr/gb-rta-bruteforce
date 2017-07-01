package dabomstew.rta.starter;

import dabomstew.rta.GBWrapper;
import dabomstew.rta.RedBlueAddr;

import java.util.*;

public class StarterIntros {
    private static final int NO_INPUT = 0x00;

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static String gameName;
    private static String BASE_SAVE_NAME;
    private static int TITLE_BASE_COST;
    private static int CRY_BASE_COST;

    static Strat titleStrat(int idx, int btn) {
        Integer[] addr = new Integer[idx+1], input = new Integer[idx+1], advFrames = new Integer[idx+1];

        for(int j=0; j<idx; j++) {
            addr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
            input[j] = NO_INPUT;
            advFrames[j] = 1;
        }

        addr[idx] = RedBlueAddr.joypadAddr;
        input[idx] = btn;
        advFrames[idx] = 1;
        String suffix = btn == A ? "(A)" : "(S)";
        return new Strat("_title" + idx + suffix, CRY_BASE_COST + 270*idx + 58, addr, input, advFrames);
    }

    static void initializeStrats(String game, String baseSaveName) {
        gameName = game;
        BASE_SAVE_NAME = baseSaveName;
        TITLE_BASE_COST = (gameName.equals("blue") ? 0 : 1);
        CRY_BASE_COST = (gameName.equals("blue") ? 96 : 88);
        hop0 = new Strat("_hop0", 172 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
        hop1 = new Strat("_hop1", 172 + 131 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, UP | SELECT | B}, new Integer[] {0, 0, 1});
        hop2 = new Strat("_hop2", 172 + 190 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, UP | SELECT | B}, new Integer[] {0, 0, 0, 0, 1});
        hop3 = new Strat("_hop3", 172 + 298 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, UP | SELECT | B}, new Integer[] {0, 0, 0, 0, 0, 0, 1});
        hop4 = new Strat("_hop4", 172 + 447 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, UP | SELECT | B}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 1});
        hop5 = new Strat("_hop5", 172 + 487 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, UP | SELECT| B}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
        hop6 = new Strat("_hop6", 172 + 536 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.displayTitleScreenAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
        hops = Arrays.asList(hop0, hop1, hop2, hop3, hop4, hop5, hop6);
        mmback = new Strat("_mmback", 142 + TITLE_BASE_COST, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {B}, new Integer[] {1});
    }

    static List<IntroSequence> initializeIntroSequences(int maxCost, String game, String baseSaveName) {
        initializeStrats(game, baseSaveName);

        int maxTitle = (maxCost -(492 + 172 + CRY_BASE_COST + TITLE_BASE_COST + 58 + 50));
        for(int i=0; maxTitle>=0 && i<=maxTitle/270; i++) {
            Integer[] rsAddr = new Integer[i+1], rsInput = new Integer[i+1], rsAdvFrames = new Integer[i+1];
            Integer[] usbAddr = new Integer[i+1], usbInput = new Integer[i+1], usbAdvFrames = new Integer[i+1];

            for(int j=0; j<i; j++) {
                rsAddr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
                rsInput[j] = NO_INPUT;
                rsAdvFrames[j] = 1;

                usbAddr[j] = RedBlueAddr.titleScreenPickNewMonAddr;
                usbInput[j] = NO_INPUT;
                usbAdvFrames[j] = 1;
            }

            title.add(titleStrat(i, A));
            title.add(titleStrat(i, START));

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
        resetSequences.add(new IntroSequence(gfreset));
        resetSequences.add(new IntroSequence(gfwait, hop0reset));
        resetSequences.addAll(permute(gf, hopResets));

        ArrayList<IntroSequence> s3seqs = new ArrayList<>();
        s3seqs.addAll(permute(gf, hops));

        while(!s3seqs.isEmpty()) {
            ArrayList<IntroSequence> s4seqs = new ArrayList<>();
            for(IntroSequence s3 : s3seqs) {
                int ngcost = s3.cost() + CRY_BASE_COST + 58 + 50;
                int ngmax = (maxCost - ngcost - 492);
                for(int i=0; ngmax>=0 && i<=ngmax/270; i++) {
                    s4seqs.add(append(s3, titleStrat(i, START)));
                    s4seqs.add(append(s3, titleStrat(i, A)));
                }

                int rscost = ngcost + 363 + 172 + TITLE_BASE_COST;
                int rsmax = (maxCost - rscost - 492);
                for(int j=0; rsmax>=0 && j<=rsmax/270; j++) {
                    resetSequences.add(append(s3, titleResets.get(j)));
                    if(270*j <= maxCost - 492 - CRY_BASE_COST) {
                        resetSequences.add(append(s3, titleusb.get(j)));
                    }
                    // TODO: move this case to next loop now that there are options strats
                    if(270*j <= maxCost - 492 - CRY_BASE_COST - 58) {
                        resetSequences.add(append(s3, title.get(j), ngreset));
                    }
                }
            }
            s3seqs.clear();

            while(!s4seqs.isEmpty()) {
                ArrayList<IntroSequence> s4tmp = new ArrayList<>();
                for (IntroSequence s4 : s4seqs) {
                    String lastStrat = s4.get(s4.size()-1).name;
                    IntroSequence seqA = append(s4, contA);
                    IntroSequence seqS = append(s4, contS);
                    if(!lastStrat.startsWith("_title") || lastStrat.endsWith("(S)")) {
                        newGameSequences.add(seqA);
                    }
                    if(!lastStrat.startsWith("_title") || lastStrat.endsWith("(A)")) {
                        newGameSequences.add(seqS);
                    }

                    int ngcost = s4.cost() + 50;
                    if ((maxCost - 492 - ngcost) >= 142 + 58 + TITLE_BASE_COST + CRY_BASE_COST) {
                        if(!lastStrat.startsWith("_title") || lastStrat.endsWith("(S)")) {
                            s3seqs.add(append(s4, mmback));
                        }
                    }

                    if ((maxCost - 492 - ngcost) >= 98) {
                        if(!lastStrat.startsWith("_title") || lastStrat.endsWith("(S)")) {
                            s4tmp.add(append(s4, fsbackA));
                        }
                        if(!lastStrat.startsWith("_title") || lastStrat.endsWith("(A)")) {
                            s4tmp.add(append(s4, fsbackS));
                        }
                    }

                    // TODO: More framecounting for intros with save files present
                }

                s4seqs = new ArrayList<>(s4tmp);
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
                    if (rs.cost() + ng.cost() <= maxCost - 492) {
                        IntroSequence newSeq = append(rs, ng);
                        introSequencesTmp.add(newSeq);
                        ngIter.add(newSeq);
                    } else {
                        break;
                    }
                }
            }
        }

        List<IntroSequence> introSequences = new ArrayList<>();
        for(IntroSequence seq : introSequencesTmp) {
            introSequences.add(append(nopal, seq));
            introSequences.add(append(pal, seq));
            introSequences.add(append(nopalab, seq));
            introSequences.add(append(palhold, seq));
            introSequences.add(append(palab, seq));
        }
        introSequencesTmp.clear();

        Collections.sort(introSequences);
        //for(IntroSequence seq : introSequences) {
        //    System.out.println(seq.toString() + ", cost: " + (seq.cost() + 492));
        //}
        return introSequences;
    }


    static PalStrat pal = new PalStrat("_pal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {UP}, new Integer[] {1});
    static PalStrat nopal = new PalStrat("_nopal", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr}, new Integer[] {NO_INPUT}, new Integer[] {1});
    static PalStrat nopalab = new PalStrat("_nopal(ab)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {A, A}, new Integer[] {0, 0});
    static PalStrat palhold = new PalStrat("_pal(hold)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {UP, UP}, new Integer[] {0, 0});
    static PalStrat palab = new PalStrat("_pal(ab)", 0, new Integer[] {RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr}, new Integer[] {UP, UP | A, UP | A}, new Integer[] {70, 0, 0});
    static Strat gfskip = new Strat("_gfskip", 0, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {UP | SELECT | B}, new Integer[] {1});
    static Strat gfwait = new Strat("_gfwait", 253, new Integer[] {RedBlueAddr.delayAtEndOfShootingStarAddr}, new Integer[] {NO_INPUT}, new Integer[] {0});
    private static List<Strat> gf = Arrays.asList(gfskip, gfwait);

    static Strat hop0, hop1, hop2, hop3, hop4, hop5, hop6, mmback;
    static List<Strat> hops;

    static Strat contA = new Strat("_cont(A+A)", 50,
            new Integer[] {RedBlueAddr.joypadAddr, RedBlueAddr.joypadAddr},
            new Integer[] {A, A},
            new Integer[] {1, 1});

    static Strat contS = new Strat("_cont(S+A)", 50,
            new Integer[] {RedBlueAddr.joypadAddr, RedBlueAddr.joypadAddr},
            new Integer[] {START, A},
            new Integer[] {1, 1});

    static Strat fsbackA = new Strat("_fsback(A+B)", 98,
            new Integer[] {RedBlueAddr.joypadAddr, RedBlueAddr.joypadAddr},
            new Integer[] {A, B},
            new Integer[] {1, 1});

    static Strat fsbackS = new Strat("_fsback(S+B)", 98,
            new Integer[] {RedBlueAddr.joypadAddr, RedBlueAddr.joypadAddr},
            new Integer[] {START, B},
            new Integer[] {1, 1});

    static ResetStrat gfreset = new ResetStrat("_gfreset", 363, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    static ResetStrat hop0reset = new ResetStrat("_hop0(reset)", 363 + 4, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    static ResetStrat ngreset = new ResetStrat("_ngreset", 363, new Integer[] {RedBlueAddr.joypadAddr}, new Integer[] {A | B | START | SELECT}, new Integer[] {0});
    //TODO: contreset, oakreset, overworldreset if doing super long manips

    static ResetStrat hop1reset = new ResetStrat("_hop1(reset)", 363 + 131 + 3, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0});
    static ResetStrat hop2reset = new ResetStrat("_hop2(reset)", 363 + 190 + 4, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0});
    static ResetStrat hop3reset = new ResetStrat("_hop3(reset)", 363 + 298 + 5, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0, 0, 0});
    static ResetStrat hop4reset = new ResetStrat("_hop4(reset)", 363 + 447 + 4, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0});
    static ResetStrat hop5reset = new ResetStrat("_hop5(reset)", 363 + 487 + 3, new Integer[] {RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr}, new Integer[] {NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, NO_INPUT, A | B | START | SELECT}, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    private static List<ResetStrat> hopResets = Arrays.asList(hop1reset, hop2reset, hop3reset, hop4reset, hop5reset);

    private static ArrayList<Strat> title = new ArrayList<>();
    private static ArrayList<Strat> titleResets = new ArrayList<>();
    private static ArrayList<Strat> titleusb = new ArrayList<>();

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
                    wrap.advanceFrame(input[i]);
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
            if(BASE_SAVE_NAME.equals("charmander")) {
                ret += "_leftside";
            } else if(BASE_SAVE_NAME.equals("charmander2") || BASE_SAVE_NAME.equals("bulba2")) {
                ret += "_below";
            } else if(BASE_SAVE_NAME.equals("bulba")) {
                ret += "_rightside";
            }
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
}
