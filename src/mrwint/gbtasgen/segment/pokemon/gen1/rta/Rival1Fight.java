package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import mrwint.gbtasgen.metric.pokemon.gen1.CheckLowerStatEffectMisses;
import mrwint.gbtasgen.segment.pokemon.fight.*;
import mrwint.gbtasgen.segment.util.SeqSegment;
import mrwint.gbtasgen.segment.util.SkipTextsSegment;

import static mrwint.gbtasgen.segment.pokemon.gen1.common.Constants.GROWL;

public class Rival1Fight extends SeqSegment {
    @Override
    public void execute() {

        seq(new InitFightSegment(0));
        {
            KillEnemyMonSegment kems = new KillEnemyMonSegment();
            kems.enemyMoveDesc = new KillEnemyMonSegment.EnemyMoveDesc[]{KillEnemyMonSegment.EnemyMoveDesc.missWith(new CheckLowerStatEffectMisses(), GROWL)};
            kems.attackCount[0][0] = 4; // tackle
            kems.numExpGainers = 2; // Squirtle, level up to 6
            seq(kems); // Bulbasaur
        }

        seq(new EndFightSegment(3)); // player defeated enemy
        seq(new SkipTextsSegment(4)); // rival after battle texts
    }
}
