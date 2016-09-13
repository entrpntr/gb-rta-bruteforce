package dabomstew.rta.nidobot;

public class OldGrassGrind {
	
	// for (int input : inputsStart) {
	// actionsToCheck.push(new
	// GrassGrindAction(peg.savedState, peg.path, "", input,
	// 0, -1));
	//
	// while (!actionsToCheck.isEmpty()) {
	// GrassGrindAction curAction = actionsToCheck.pop();
	// // logLN("Checking
	// //
	// "+curAction.preGrassAreaPath+"/"+curAction.inGrassAreaPath+"/"+inputName(curAction.input));
	// gb.loadState(curAction.savedState);
	// wrap.injectInput(curAction.input);
	//
	// String newPath = curAction.inGrassAreaPath +
	// inputName(curAction.input);
	//
	// // progress to encounter check
	// wrap.advanceToAddress(0x0683);
	// // logLN("got to encounter check");
	// boolean gotEncounter = false;
	//
	// if (mem.getY() != 12) {
	// if (mem.getHRA() >= 0 && mem.getHRA() <= 24) {
	// // Encounter, let's check what it is
	// gotEncounter = true;
	// // logLN("encounter with
	// // "+mem.getHRS()+" after "+newPath);
	// if (mem.getHRA() <= 24 && mem.getHRS() >= 141 &&
	// mem.getHRS() <= 165) {
	// wrap.advanceFrame();
	// wrap.advanceFrame();
	// if (mem.getBattleType() == 0) {
	// ps.println("wutface");
	// } else {
	// Encounter enc = new
	// Encounter(mem.getEncounterSpecies(),
	// mem.getEncounterLevel(), mem.getEncounterDVs());
	// if (!seenEncounters.contains(enc)) {
	// seenEncounters.add(enc);
	// if (enc.species == 3 && enc.level == 4) {
	// ps.printf(
	// "encountered %d Lv%d DVs %04X initPath %s grassPath
	// %s\n",
	// enc.species, enc.level, enc.dvs,
	// curAction.preGrassAreaPath, newPath);
	// if ((enc.dvs & 0xF000) == 0xF000) {
	// logF(
	// "encountered %d Lv%d DVs %04X initPath %s grassPath
	// %s\n",
	// enc.species, enc.level, enc.dvs,
	// curAction.preGrassAreaPath, newPath);
	// }
	// } else {
	// ps.println("Wutface2");
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// if (!gotEncounter) {
	// // wind forward to end of the step and save
	// wrap.advanceToAddress(0x0F4D);
	// String state = getUniqid();
	// if (!seenStatesGrass.contains(state)) {
	// seenStatesGrass.add(state);
	// int steps = (curAction.input == A) ?
	// curAction.numSteps
	// : curAction.numSteps + 1;
	// if (steps < maxStepsInGrass) {
	// // queue new actions
	// int lhInput = (curAction.input == LEFT ||
	// curAction.input == RIGHT)
	// ? curAction.input : curAction.lastHorizontalInput;
	// int[] inputs =
	// PermissibleActionsHandler.actionsInGrassArea(mem.getX(),
	// mem.getY(), lhInput);
	// if (inputs.length > 0) {
	// ByteBuffer save = gb.saveState();
	// for (int inputN : inputs) {
	// actionsToCheck
	// .push(new GrassGrindAction(save,
	// curAction.preGrassAreaPath,
	// newPath, inputN, steps, lhInput));
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// ps.flush();
	// }
	// positionsDone++;
	// if (positionsDone % onePercent == 0) {
	// long newTime = System.currentTimeMillis();
	// // logF("Done %d/%d , last%d=%dms,
	// // eta=%dms\n", positionsDone, numEndPositions,
	// // onePercent, newTime - lastOffset,
	// // (newTime - lastOffset) * (numEndPositions -
	// // positionsDone) / onePercent);
	// lastOffset = newTime;
	// }

}
