package mrwint.gbtasgen.segment.section.trainer;

import java.util.ArrayList;
import java.util.List;

import mrwint.gbtasgen.segment.Segment;
import mrwint.gbtasgen.segment.fight.EndFightSegment;
import mrwint.gbtasgen.segment.fight.InitFightSegment;
import mrwint.gbtasgen.segment.fight.KillEnemyMonSegment;
import mrwint.gbtasgen.segment.fight.NewEnemyMonSegment;
import mrwint.gbtasgen.segment.fight.KillEnemyMonSegment.EnemyMoveDesc;
import mrwint.gbtasgen.segment.util.SequenceSegment;
import mrwint.gbtasgen.state.StateBuffer;


public class AzaleaTwinsAmyMay extends Segment {

	SequenceSegment sequence;
	
	public AzaleaTwinsAmyMay() {
		List<Segment> segments = new ArrayList<Segment>();

		segments.add(new InitFightSegment(2));
		{
			KillEnemyMonSegment kems = new KillEnemyMonSegment(); // spinarak
			kems.enemyMoveDesc = new EnemyMoveDesc[] {EnemyMoveDesc.missWith()}; // any
			kems.maxOwnDamage = 0;
			kems.attackCount[0][1] = 1; // 1x scratch crit
			kems.numExpGainers = 1; // no level up
			kems.onlyPrintInfo = false;
			segments.add(kems);
		}
		segments.add(NewEnemyMonSegment.any());
		{
			KillEnemyMonSegment kems = new KillEnemyMonSegment(); // ledyba
			kems.enemyMoveDesc = new EnemyMoveDesc[] {EnemyMoveDesc.missWith()}; // any
			kems.maxOwnDamage = 0;
			kems.attackCount[0][1] = 1; // 1x scratch crit
			kems.numExpGainers = 1; // no level up
			kems.onlyPrintInfo = false;
			segments.add(kems);
		}
		segments.add(new EndFightSegment(1));

		sequence = new SequenceSegment(segments.toArray(new Segment[0]));
	}
	
	@Override
	public StateBuffer execute(StateBuffer in) throws Throwable {
		return sequence.execute(in);
	}
}