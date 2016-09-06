package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import static mrwint.gbtasgen.move.Move.A;
import static mrwint.gbtasgen.move.Move.START;

import mrwint.gbtasgen.move.Move;
import mrwint.gbtasgen.segment.pokemon.TextSegment;
import mrwint.gbtasgen.segment.pokemon.gen1.common.NamingSegment;
import mrwint.gbtasgen.segment.util.SeqSegment;
import mrwint.gbtasgen.segment.util.SkipTextsSegment;

public class OakSpeech extends SeqSegment {

    @Override
    public void execute() {
        seqButton(Move.START);
        seqButton(Move.A);
        seqButton(Move.START);
        seqButton(Move.A);

        seq(new SkipTextsSegment(13));

        seqButton(A);
        seq(new NamingSegment("A"));
        seqButton(START);

        seq(new SkipTextsSegment(5));

        seqButton(A);
        seq(new NamingSegment("A"));
        seqButton(START);

        seq(new SkipTextsSegment(7));
        seq(new TextSegment());
    }
}
