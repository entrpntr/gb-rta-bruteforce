package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import mrwint.gbtasgen.move.Move;
import mrwint.gbtasgen.move.pokemon.gen1.OverworldInteract;
import mrwint.gbtasgen.segment.Segment;
import mrwint.gbtasgen.segment.pokemon.TextSegment;
import mrwint.gbtasgen.segment.pokemon.WalkToSegment;
import mrwint.gbtasgen.segment.util.MoveSegment;
import mrwint.gbtasgen.segment.util.SeqSegment;
import mrwint.gbtasgen.segment.util.SkipTextsSegment;

public class ViridianShopping extends SeqSegment {
    @Override
    protected void execute() {
        seq(new WalkToSegment(29, 19)); // enter viridian mart

        seq(new WalkToSegment(3, 5));
        seq(new WalkToSegment(2, 5));
        seq(new MoveSegment(new OverworldInteract(1)));

        {
            seq(new SkipTextsSegment(1, true)); // buy
            seq(new TextSegment());
            {
                seq(Segment.press(Move.A)); // pokeball
                seq(Segment.scroll(-2)); // x3
                seq(Segment.press(Move.A)); // buy
                seq(new SkipTextsSegment(1)); // confirmation text
                seq(new SkipTextsSegment(1, true)); // "yes"
                seq(new SkipTextsSegment(1)); // thank you text
            }
            seq(Segment.repress(Move.B)); // cancel
            seq(new SkipTextsSegment(2)); // cancel + bye
        }

        seq(new WalkToSegment(3, 8, false)); // leave mart
        seq(new WalkToSegment(7, 17)); // save spot
    }
}
