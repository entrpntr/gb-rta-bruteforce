package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import mrwint.gbtasgen.metric.Metric;
import mrwint.gbtasgen.metric.pokemon.CheckEncounterMetric;
import mrwint.gbtasgen.move.HardResetMove;
import mrwint.gbtasgen.move.Move;
import mrwint.gbtasgen.move.PressButton;
import mrwint.gbtasgen.move.Wait;
import mrwint.gbtasgen.move.pokemon.gen1.WalkStep;
import mrwint.gbtasgen.segment.Segment;
import mrwint.gbtasgen.segment.pokemon.CatchMonSegment;
import mrwint.gbtasgen.segment.pokemon.ResetAndContinueSegment;
import mrwint.gbtasgen.segment.pokemon.WalkToSegment;
import mrwint.gbtasgen.segment.util.SeqSegment;
import mrwint.gbtasgen.segment.util.SkipTextsSegment;
import mrwint.gbtasgen.state.StateBuffer;

public class NidoFBEE extends SeqSegment {
    @Override
    public void execute() {
/*        seq(Segment.press(Move.START)); // open menu
        for(int i=0;i<3;i++) {
            seqMove(new PressButton(Move.UP, Metric.PRESSED_JOY)); // move to "save"
        }
        seq(Segment.press(Move.A)); // save
        seq(new SkipTextsSegment(1, true)); // say "yes"
        seqMove(new Wait(32));
        seqMove(new HardResetMove());
        seqButtonNoDelay(Move.UP | Move.SELECT | Move.B);
        seqButtonNoDelay(Move.UP | Move.SELECT | Move.B);
        seqButtonNoDelay(Move.A);
        seqButtonNoDelay(Move.START);
        seqButtonNoDelay(Move.A); */
/*
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
*/
        seq(new WalkToSegment( -1, 17 ));
        seq(new WalkToSegment( 40,  9 ));
        seq(new WalkToSegment(  2, 17 ));
        seq(new WalkToSegment( -1, 17 ));
/*
        seqMove(new WalkStep(Move.RIGHT, false));
        seqMove(new WalkStep(Move.RIGHT, false));
        seqMove(new WalkStep(Move.RIGHT, false));

        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.DOWN, false));
        seqMove(new WalkStep(Move.DOWN, false));
        seqMove(new WalkStep(Move.DOWN, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.LEFT, false));
        seqMove(new WalkStep(Move.UP, false));
        seqMove(new WalkStep(Move.UP, false));
        seqMove(new WalkStep(Move.UP, false));
        seqMove(new WalkStep(Move.UP, false));
        seqMove(new WalkStep(Move.DOWN, false));
*/
        seq(new WalkToSegment(35, 9));
        seq(new WalkToSegment(35, 12));
        seq(new WalkToSegment(33, 12));
        seq(new WalkToSegment(33, 8));
        seq(new WalkToSegment(33, 9));
        seqMove(new WalkStep(Move.DOWN, false));
        seqMetric(new CheckEncounterMetric(3, 4).withAtkDV(15).withSpcDV(14));
        seq(new CatchMonSegment(0, "A"));
    }
}
