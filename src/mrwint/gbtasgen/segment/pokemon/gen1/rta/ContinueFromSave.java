package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import mrwint.gbtasgen.move.Move;
import mrwint.gbtasgen.segment.util.SeqSegment;

public class ContinueFromSave extends SeqSegment {
    @Override
    protected void execute() {
        seqButtonNoDelay(Move.UP | Move.SELECT | Move.B);
        seqButtonNoDelay(Move.UP | Move.SELECT | Move.B);
        seqButtonNoDelay(Move.START);
        seqButtonNoDelay(Move.A);
        seqButtonNoDelay(Move.A);
    }
}
