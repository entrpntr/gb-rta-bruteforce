package dabomstew.rta.ffef;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dabomstew.rta.Encounter;
import dabomstew.rta.FileFunctions;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.RedBlueAddr;
import dabomstew.rta.astar.Node;
import dabomstew.rta.generic.RBMap;
import dabomstew.rta.generic.RBMapDestination;
import dabomstew.rta.generic.RBMapTile;
import dabomstew.rta.generic.RBPokemon;
import mrwint.gbtasgen.Gb;

public class NidoBotFFEF {
	private static final int NO_INPUT = 0x00;

	public static final int A = 0x01;
	public static final int B = 0x02;
	public static final int SELECT = 0x04;
	public static final int START = 0x08;

	public static final int RIGHT = 0x10;
	public static final int LEFT = 0x20;
	public static final int UP = 0x40;
	public static final int DOWN = 0x80;

	private static final int HARD_RESET = 0x800;

	public static final int SLOT1 = 0x01; // 51/256 (~20%)
	public static final int SLOT2 = 0x02; // 51/256 (~20%)
	public static final int SLOT3 = 0x04; // 39/256 (~15%);
	public static final int SLOT4 = 0x08; // 25/256 (~10%)
	public static final int SLOT5 = 0x10; // 25/256 (~10%)
	public static final int SLOT6 = 0x20; // 25/256 (~10%)
	public static final int SLOT7 = 0x40; // 13/256 (~5%)
	public static final int SLOT8 = 0x80; // 13/256 (~5%)
	public static final int SLOT9 = 0x100; // 11/256 (~4%)
	public static final int SLOT10 = 0x200; // 3/256 (~1%)

	public static final int SLOTS;

	private static final String gameName;
	private static PrintWriter writer;

	// Graph is only built right now to waste 10 steps, so more would need to be
	// added to make use of >= 204 frames.
	// Also more intros would need to be spelled out.
	private static final int MAX_COST;
	static {
		int BLUE_COST = 138;
		gameName = "red";
		MAX_COST = (gameName.equals("blue")) ? BLUE_COST : BLUE_COST + 28;
		SLOTS = (gameName.equals("blue")) ? SLOT10 : SLOT4;
	}

	// TODO: LOOK AT TUNING THESE MORE
	private static final double DSUM_HIGH_COEF = 0.686;
	private static final double DSUM_LOW_COEF = 0.623;
	private static final double DSUM_MARGIN_OF_ERROR = 5.0;

	// Sort tiles by starting cost (lower starting cost takes priority),
	// then by starting distance from grass (longer distance takes priority).
	//
	// The idea being that if you get to a state that's already been reached, it
	// has been from a state that has wasted
	// fewer frames to get to that point.
	static class SaveTileComparator implements Comparator<SaveTile> {
		@Override
		public int compare(SaveTile o1, SaveTile o2) {
			if (o1.getStartCost() != o2.getStartCost()) {
				return o1.getTrueStartCost() - o2.getTrueStartCost();
			} else {
				return o2.getOwPos().getMinStepsToGrass() - o1.getOwPos().getMinStepsToGrass();
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// Make folder if necessary
		if (!new File("logs").exists()) {
			new File("logs").mkdir();
		}

		if (!new File("roms").exists()) {
			new File("roms").mkdir();
			System.err.println("I need ROMs to simulate!");
			System.exit(0);
		}

		if (!new File("roms/poke" + gameName + ".gbc").exists()) {
			System.err.println("Could not find poke" + gameName + ".gbc in roms directory!");
			System.exit(0);
		}

		File file = new File(gameName + "_ffef_encounters.txt");
		writer = new PrintWriter(file);

		// TODO: Programmatically add intros for manips with higher cost caps
		List<IntroSequence> introSequences = new ArrayList<>();
		introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, cont));
		introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, cont));
		introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, cont));
		introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, cont));
		introSequences.add(new IntroSequence(nopal, gfSkip, nido0, title0, cont, backout, cont, cont));
		introSequences.add(new IntroSequence(pal, gfSkip, nido0, title0, cont, backout, cont, cont));
		introSequences.add(new IntroSequence(abss, gfSkip, nido0, title0, cont, backout, cont, cont));
		introSequences.add(new IntroSequence(holdpal, gfSkip, nido0, title0, cont, backout, cont, cont));

		introSequences.add(new IntroSequence(nopal, gfSkip, nido1, title0, cont, cont));
		introSequences.add(new IntroSequence(pal, gfSkip, nido1, title0, cont, cont));
		introSequences.add(new IntroSequence(abss, gfSkip, nido1, title0, cont, cont));
		introSequences.add(new IntroSequence(holdpal, gfSkip, nido1, title0, cont, cont));

		initTiles(false, new RBMapDestination(RBMap.VIRIDIAN_CITY, RBMapDestination.WEST_CONNECTION));
		Collections.shuffle(saveTiles);

		// Collections.sort(saveTiles, new SaveTileComparator());
		for (SaveTile saveTile : saveTiles) {
			// Comment these lines ouif you want to search the whole space
			if (saveTile.getOwPos().getMinStepsToGrass() > 22) {
				continue;
			}

			OverworldTile savePos = saveTile.getOwPos();
			makeSave(RBMap.getMapByID(savePos.getMap()), savePos.getX(), savePos.getY());

			// Init gambatte with 1 screen
			Gb.loadGambatte(1);
			gb = new Gb(0, false);
			gb.startEmulator("roms/poke" + gameName + ".gbc");
			mem = new GBMemory(gb);
			wrap = new GBWrapper(gb, mem);

			for (IntroSequence intro : introSequences) {
				int baseCost = saveTile.getStartCost() + intro.cost();
				if (baseCost <= MAX_COST) {
					// System.out.println("--- [" + savePos.getMap() + "#" +
					// savePos.getX() + "," + savePos.getY() + "] " +
					// intro.toString() + " ---");
					intro.execute(wrap);
					wrap.advanceToAddress(RedBlueAddr.joypadOverworldAddr);
					OverworldState owState = new OverworldState(savePos.toString() + " - " + intro.toString() + ":",
							saveTile.getOwPos(), 1, true, gb.getDivState(), mem.getHRA(), mem.getHRS(),
							saveTile.isViridianNpc(), mem.getTurnFrameStatus(), mem.getNPCTimers(), baseCost, 0);
					overworldSearch(owState);
					gb.step(HARD_RESET);
				}
			}
		}
		writer.close();
	}

	// private static void overworldSearch(OverworldState ow, boolean dsumPrune)
	// {
	private static void overworldSearch(OverworldState ow) {
		if (ow.getWastedFrames() > MAX_COST) {
			return;
		}
		if (!seenStates.add(ow.getUniqId())) {
			return;
		}
		if (ow.getMap() == 33 && ow.getX() == 33 && ow.getY() == 11 && ow.getWastedFrames() + 34 > MAX_COST) {
			return;
		}

		if (inferDsum(ow)) {
			return;
		}

		ByteBuffer curSave = gb.saveState();

		for (OverworldEdge edge : ow.getPos().getEdgeList()) {
			OverworldAction edgeAction = edge.getAction();
			if (ow.getMap() == 1 && ow.getX() == 47 - 30 && edgeAction == OverworldAction.RIGHT && ow.isViridianNpc()) {
				continue;
			}
			if (ow.aPressCounter() > 0 && (edgeAction == OverworldAction.A || edgeAction == OverworldAction.START_B
					|| edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
				continue;
			}
			if (!ow.canPressStart() && (edgeAction == OverworldAction.START_B || edgeAction == OverworldAction.S_A_B_S
					|| edgeAction == OverworldAction.S_A_B_A_B_S)) {
				continue;
			}
			int edgeCost = edge.getCost();
			if (ow.getWastedFrames() + edgeCost > MAX_COST) {
				continue;
			}

			int lowFrames = edge.getFrames()
					+ ((OverworldAction.isDpad(edgeAction) && edge.getNextPos().isEncounterTile()) ? 0
							: edge.getNextPos().getMinStepsToGrass() * 17);
			int highFrames = edge.getFrames() + 17 * edge.getNextPos().getMinStepsToGrass()
					+ 17 * effectiveWastableSteps(edge.getNextPos()) + MAX_COST - ow.getWastedFrames() - edgeCost;
			double lowDsumChange = ((double) lowFrames) * DSUM_LOW_COEF;
			double highDsumChange = ((double) highFrames) * DSUM_HIGH_COEF;
			double predictLow = ((double) ow.getDsum()) + 2048.0 - lowDsumChange + DSUM_MARGIN_OF_ERROR;
			double predictHigh = ((double) ow.getDsum()) + 2048.0 - highDsumChange - DSUM_MARGIN_OF_ERROR;

			// if(inferDsumDebug(predictHigh, predictLow)) {
			if (inferDsum(RBMap.getMapByID(ow.getMap()), predictHigh, predictLow)) {
				continue;
			}

			int initIGT = readIGT();
			int wastedFrames;
			int res;
			OverworldState newState;
			switch (edgeAction) {
			case LEFT:
			case UP:
			case RIGHT:
			case DOWN:
				int input = 16 * (int) (Math.pow(2.0, (edgeAction.ordinal())));
				wrap.injectRBInput(input);
				wrap.advanceWithJoypadToAddress(input, RedBlueAddr.newBattleAddr);
				res = wrap.advanceWithJoypadToAddress(input, RedBlueAddr.encounterTestAddr,
						RedBlueAddr.joypadOverworldAddr);
				if (res == RedBlueAddr.encounterTestAddr) {
					if (mem.getHRA() >= 0 && mem.getHRA() < RBMap.getMapByID(mem.getMap()).getGlobalEncounterRate()) {
						String rngAtEnc = mem.getRNGStateWithDsum();
						wrap.advanceFrame();
						wrap.advanceFrame();
						Encounter enc = new Encounter(mem.getEncounterSpecies(), mem.getEncounterLevel(),
								mem.getEncounterDVs(), mem.getRNGStateHRAOnly());
						int owFrames = ow.getOverworldFrames() + edge.getFrames();
						// String pruneDsum = dsumPrune ? " [*]" : "";
						String defaultYbf = "";
						if ((enc.species == RBPokemon.NIDORAN_MALE.getIndexNumber())) {
							if ((enc.dvs == 0xFFEF || enc.dvs == 0xFFEE || enc.dvs == 0xFEEF)) {
								wrap.advanceToAddress(RedBlueAddr.manualTextScrollAddr);
								wrap.injectRBInput(A);
								wrap.advanceFrame();
								wrap.advanceToAddress(RedBlueAddr.playCryAddr);
								wrap.injectRBInput(DOWN | A);
								wrap.advanceWithJoypadToAddress(DOWN | A, RedBlueAddr.displayListMenuIdAddr);
								wrap.injectRBInput(A | RIGHT);
								int res2 = wrap.advanceWithJoypadToAddress(A | RIGHT, RedBlueAddr.catchSuccessAddr,
										RedBlueAddr.catchFailureAddr);
								if (res2 == RedBlueAddr.catchSuccessAddr) {
									defaultYbf = ", default ybf: [*]";
								} else {
									defaultYbf = ", default ybf: [ ]";
								}
								System.out.println(ow.toString() + " " + edgeAction.logStr() + ", "
										+ String.format("species %d lv%d DVs %04X rng %s encrng %s", enc.species, enc.level,
												enc.dvs, enc.battleRNG, rngAtEnc)
										+ ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames)
										+ defaultYbf
								// + pruneDsum
								);
							}
						}
						writer.println(ow.toString() + " " + edgeAction.logStr() + ", "
								+ String.format("species %d lv%d DVs %04X rng %s encrng %s", enc.species, enc.level,
										enc.dvs, enc.battleRNG, rngAtEnc)
								+ ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames)
								+ defaultYbf
						// + pruneDsum
						);
						writer.flush();
					}
				} else if (res == RedBlueAddr.joypadOverworldAddr) {
					while (mem.getMap() == ow.getMap() && mem.getX() == ow.getX() && mem.getY() == ow.getY()) {
						wrap.injectRBInput(input);

						wrap.advanceFrame(input);
						wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
					}
					// Ledgejumps still call joypad on ledge tile
					if (edge.getFrames() == 40) {
						wrap.injectRBInput(input);
						wrap.advanceFrame(input);
						wrap.advanceWithJoypadToAddress(input, RedBlueAddr.joypadOverworldAddr);
					}
					int igt = readIGT();
					int extraWastedFrames = igt - initIGT - edge.getFrames();
					boolean newViridianNPC = (ow.getMap() == 1 && ow.getX() == 0 && input == LEFT);
					// String pruneDsum = prune ? "[*]" : "";
					newState = new OverworldState(ow.toString() + " " + edgeAction.logStr()
					// + pruneDsum
							, edge.getNextPos(), Math.max(0, ow.aPressCounter() - 1), true, gb.getDivState(),
							mem.getHRA(), mem.getHRS(), ow.isViridianNpc() || newViridianNPC, mem.getTurnFrameStatus(),
							mem.getNPCTimers(), ow.getWastedFrames() + edgeCost + extraWastedFrames,
							ow.getOverworldFrames() + edge.getFrames());
					// overworldSearch(newState, prune || dsumPrune);
					overworldSearch(newState);
				}

				break;
			case A:
				wrap.injectRBInput(A);
				wrap.advanceFrame(A);
				res = wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadOverworldAddr,
						RedBlueAddr.printLetterDelayAddr);
				if (res == RedBlueAddr.joypadOverworldAddr) {
					newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 2, true,
							gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(),
							mem.getNPCTimers(), ow.getWastedFrames() + 2, ow.getOverworldFrames() + 2);
					// overworldSearch(newState, prune || dsumPrune);
					overworldSearch(newState);
				}
				break;
			case START_B:
				wrap.injectRBInput(START);
				wrap.advanceFrame(START);
				wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(B);
				wrap.advanceFrame(B);
				wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadOverworldAddr);
				wastedFrames = readIGT() - initIGT;

				newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0, true,
						gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(),
						mem.getNPCTimers(), ow.getWastedFrames() + wastedFrames,
						ow.getOverworldFrames() + wastedFrames);
				// overworldSearch(newState, prune || dsumPrune);
				overworldSearch(newState);
				break;
			case S_A_B_S:
				wrap.injectRBInput(START);
				wrap.advanceFrame(START);
				wrap.advanceToAddress(RedBlueAddr.joypadAddr);

				wrap.injectRBInput(A);
				wrap.advanceFrame(A);
				wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);

				wrap.injectRBInput(B);
				wrap.advanceFrame(B);
				wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);

				wrap.injectRBInput(START);
				wrap.advanceFrame(START);
				wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadOverworldAddr);

				wastedFrames = readIGT() - initIGT;

				newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0, false,
						gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(),
						mem.getNPCTimers(), ow.getWastedFrames() + wastedFrames,
						ow.getOverworldFrames() + wastedFrames);
				// overworldSearch(newState, prune || dsumPrune);
				overworldSearch(newState);
				break;
			case S_A_B_A_B_S:
				wrap.injectRBInput(START);
				wrap.advanceFrame(START);
				wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(A);
				wrap.advanceFrame(A);
				wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(B);
				wrap.advanceFrame(B);
				wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(A);
				wrap.advanceFrame(A);
				wrap.advanceWithJoypadToAddress(A, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(B);
				wrap.advanceFrame(B);
				wrap.advanceWithJoypadToAddress(B, RedBlueAddr.joypadAddr);
				wrap.injectRBInput(START);
				wrap.advanceFrame(START);
				wrap.advanceWithJoypadToAddress(START, RedBlueAddr.joypadOverworldAddr);
				wastedFrames = readIGT() - initIGT;

				newState = new OverworldState(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), 0, false,
						gb.getDivState(), mem.getHRA(), mem.getHRS(), ow.isViridianNpc(), mem.getTurnFrameStatus(),
						mem.getNPCTimers(), ow.getWastedFrames() + wastedFrames,
						ow.getOverworldFrames() + wastedFrames);
				// overworldSearch(newState, prune || dsumPrune);
				overworldSearch(newState);
				break;
			default:
				break;
			}
			gb.loadState(curSave);
		}
	}

	// Need this, otherwise dsum inference gets confused about how much longer
	// the manip can take
	private static int effectiveWastableSteps(OverworldTile pos) {
		int effWastableSteps = 0;
		if (pos.getMap() == 33 && pos.getX() >= 30 && pos.getX() <= 33 && pos.getY() >= 8 && pos.getY() <= 12) {
			effWastableSteps = Math.abs(pos.getX() - 33) + Math.abs(pos.getY() - 11) - 1;
		}
		return effWastableSteps;
	}

	private static boolean inferDsumDebug(OverworldState ow, OverworldEdge edge, double predictHigh,
			double predictLow) {
		if (Math.abs(predictHigh - predictLow) >= 256.0) {
			return false;
		} else {
			while (predictHigh >= 256.0) {
				predictHigh -= 256.0;
				predictLow -= 256.0;
			}
			if (gameName.equals("red")) {
				if (predictLow <= 141.0
						|| (predictHigh >= 170.0 && (predictLow <= 256.0 || predictLow % 256.0 <= 141.0))) {
					writer.println("PRUNED: " + ow.toString() + " [" + edge.getAction().logStr() + " -> "
							+ edge.getNextPos().getX() + "," + edge.getNextPos().getY() + "]" + " -- High: "
							+ String.format("%.3f", predictHigh) + ", Low: "
							+ String.format("%.3f", (predictLow % 256.0)));
					return true;
				}
			} else if (gameName.equals("blue")) {
				if (predictLow <= 253.0 && predictHigh >= 4.0) {
					writer.println("PRUNED: " + ow.toString() + " [" + edge.getAction().logStr() + " -> "
							+ edge.getNextPos().getX() + "," + edge.getNextPos().getY() + "]" + " -- High: "
							+ String.format("%.3f", predictHigh) + ", Low: "
							+ String.format("%.3f", (predictLow % 256.0)));
					return true;
				}
			}
			return false;
		}
	}

	// returns true if should prune
	private static boolean inferDsum(RBMap encounterMap, double predictHigh, double predictLow) {
		if (Math.abs(predictHigh - predictLow) >= 256.0) {
			return false;
		} else {
			while (predictHigh >= 256.0) {
				predictHigh -= 256.0;
				predictLow -= 256.0;
			}
			if ((SLOTS & SLOT1) != 0 && predictLow <= 0
					&& predictHigh >= (50 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT2) != 0 && predictLow <= 51
					&& predictHigh >= (101 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT3) != 0 && predictLow <= 102
					&& predictHigh >= (140 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT4) != 0 && predictLow <= 141
					&& predictHigh >= (165 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT5) != 0 && predictLow <= 166
					&& predictHigh >= (190 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT6) != 0 && predictLow <= 191
					&& predictHigh >= (215 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT7) != 0 && predictLow <= 216
					&& predictHigh >= (228 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT8) != 0 && predictLow <= 229
					&& predictHigh >= (241 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT9) != 0 && predictLow <= 242
					&& predictHigh >= (252 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			if ((SLOTS & SLOT10) != 0 && predictLow <= 253
					&& predictHigh >= (255 + encounterMap.getGlobalEncounterRate() - 1) % 256) {
				return true;
			}
			return false;
		}
	}

	// returns true if should prune
	private static boolean inferDsum(OverworldState ow) {
		int minSteps = ow.getPos().getMinStepsToGrass();
		double dsum = (double) (2048 + ow.getHra() + ow.getHrs());
		int effWastableSteps = effectiveWastableSteps(ow.getPos());
		double lowFrames = ((double) (minSteps)) * 17.0;
		double highFrames = 17.0 * ((double) (minSteps + effWastableSteps)) + MAX_COST - ow.getWastedFrames();
		double predictLow = dsum - lowFrames * DSUM_LOW_COEF + DSUM_MARGIN_OF_ERROR;
		double predictHigh = dsum - highFrames * DSUM_HIGH_COEF - DSUM_MARGIN_OF_ERROR;
		return inferDsum(RBMap.getMapByID(ow.getMap()), predictHigh, predictLow);
	}

	/*
	 * private static int readX() { return gb.readMemory(0xD362); } private
	 * static int readY() { return gb.readMemory(0xD361); } private static int
	 * readMap() { return gb.readMemory(0xD35E); } private static int readHRA()
	 * { return gb.readMemory(0xFFD3); } private static int readHRS() { return
	 * gb.readMemory(0xFFD4); }
	 */

	private static int readIGT() {
		return 3600 * gb.readMemory(0xDA43) + 60 * gb.readMemory(0xDA44) + gb.readMemory(0xDA45);
	}

	public static void makeSave(RBMap map, int x, int y) throws IOException {
		String prefix = (map == RBMap.VIRIDIAN_CITY) ? "viridian" : "r22";
		byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/" + prefix + "_ffef_" + gameName + ".sav");
		int baseX = x;
		int baseY = y;
		int tlPointer = map.getTLPointer(baseX, baseY);
		baseSave[0x260B] = (byte) (tlPointer & 0xFF);
		baseSave[0x260C] = (byte) (tlPointer >> 8);
		baseSave[0x260D] = (byte) baseY;
		baseSave[0x260E] = (byte) baseX;
		baseSave[0x260F] = (byte) (baseY % 2);
		baseSave[0x2610] = (byte) (baseX % 2);
		baseSave[0x2CEF] = (byte) 6;
		baseSave[0x2CF0] = (byte) 9;
		baseSave[0x2CF1] = (byte) 12;
		int csum = 0;
		for (int i = 0x2598; i < 0x3523; i++) {
			csum += baseSave[i] & 0xFF;
		}
		baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
		FileFunctions.writeBytesToFile("roms/poke" + gameName + ".sav", baseSave);
	}

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
			for (int i = 0; i < addr.length; i++) {
				wrap.advanceToAddress(addr[i]);
				wrap.injectRBInput(input[i]);
				for (int j = 0; j < advanceFrames[i]; j++) {
					wrap.advanceFrame();
				}
			}
		}
	}

	private static class PalStrat extends Strat {
		PalStrat(String name, int cost, Integer[] addr, Integer[] input, Integer[] advanceFrames) {
			super(name, cost, addr, input, advanceFrames);
		}

		@Override
		public void execute(GBWrapper wrap) {
			for (int i = 0; i < addr.length; i++) {
				wrap.advanceWithJoypadToAddress(input[i], addr[i]);
				wrap.advanceFrame(input[i]);
				for (int j = 0; j < advanceFrames[i]; j++) {
					wrap.advanceFrame();
				}
			}
		}
	}

	static class IntroSequence extends ArrayList<Strat> implements Comparable<IntroSequence> {
		IntroSequence(Strat... strats) {
			super(Arrays.asList(strats));
		}

		IntroSequence(IntroSequence other) {
			super(other);
		}

		@Override
		public String toString() {
			String ret = gameName;
			for (Strat s : this) {
				ret += s.name;
			}
			return ret;
		}

		void execute(GBWrapper wrap) {
			for (Strat s : this) {
				s.execute(wrap);
			}
		}

		int cost() {
			return this.stream().mapToInt((Strat s) -> s.cost).sum();
		}

		@Override
		public int compareTo(IntroSequence o) {
			return this.cost() - o.cost();
		}
	}

	private static PalStrat pal = new PalStrat("_pal", 0, new Integer[] { RedBlueAddr.biosReadKeypadAddr },
			new Integer[] { UP }, new Integer[] { 1 });
	private static PalStrat nopal = new PalStrat("_nopal", 0, new Integer[] { RedBlueAddr.biosReadKeypadAddr },
			new Integer[] { NO_INPUT }, new Integer[] { 1 });
	private static PalStrat abss = new PalStrat("_nopal(ab)", 0,
			new Integer[] { RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr }, new Integer[] { A, A },
			new Integer[] { 0, 0 });
	private static PalStrat holdpal = new PalStrat("_pal(hold)", 0,
			new Integer[] { RedBlueAddr.biosReadKeypadAddr, RedBlueAddr.initAddr }, new Integer[] { UP, UP },
			new Integer[] { 0, 0 });

	private static Strat nido0 = new Strat("_hop0", 0, new Integer[] { RedBlueAddr.joypadAddr },
			new Integer[] { UP | SELECT | B }, new Integer[] { 1 });
	private static Strat nido1 = new Strat("_hop1", 131,
			new Integer[] { RedBlueAddr.animateNidorinoAddr, RedBlueAddr.checkInterruptAddr, RedBlueAddr.joypadAddr },
			new Integer[] { NO_INPUT, NO_INPUT, A }, new Integer[] { 0, 0, 1 });

	private static Strat cont = new Strat("", 0, new Integer[] { RedBlueAddr.joypadAddr }, new Integer[] { A },
			new Integer[] { 1 });
	// private static Strat cont = new Strat("_cont", 0, new Integer[]
	// {RedBlueAddr.joypadAddr}, new Integer[] {A}, new Integer[] {1});

	private static Strat backout = new Strat("_backout", 97, new Integer[] { RedBlueAddr.joypadAddr },
			new Integer[] { B }, new Integer[] { 1 });

	private static Strat gfSkip = new Strat("", 0, new Integer[] { RedBlueAddr.joypadAddr },
			new Integer[] { UP | SELECT | B }, new Integer[] { 1 });
	private static Strat title0 = new Strat("", 0, new Integer[] { RedBlueAddr.joypadAddr }, new Integer[] { START },
			new Integer[] { 1 });
	// private static Strat gfSkip = new Strat("_gfskip", 0,
	// new Integer[] {RedBlueAddr.joypadAddr},
	// new Integer[] {UP | SELECT | B},
	// new Integer[] {1});
	// private static Strat title0 = new Strat("_title0", 0,
	// new Integer[] {RedBlueAddr.joypadAddr},
	// new Integer[] {START},
	// new Integer[] {1});

	private static final int PW_START_X = 30;
	private static final int PW_START_Y = 174;
	private static final int PW_WIDTH = 44;
	private static final int PW_HEIGHT = 13;

	private static List<SaveTile> saveTiles = new ArrayList<>();
	private static HashSet<String> seenStates = new HashSet<>();
	private static final OverworldTile[][] pw = new OverworldTile[PW_WIDTH + 1][PW_HEIGHT + 1];

	private static Gb gb;
	private static GBWrapper wrap;
	private static GBMemory mem;

	// Put tiles in terms of pokeworld for sake of sanity

	private static void initTiles(boolean onBike, RBMapDestination... destinations) {
		long startTime = System.currentTimeMillis();
		int numFramesPerStep = onBike ? 9 : 17;
		HashMap<RBMap, RBMapDestination> dests = new HashMap<RBMap, RBMapDestination>();
		HashMap<OverworldTile, List<Node>> paths = new HashMap<OverworldTile, List<Node>>();
		for (RBMapDestination dest : destinations) {
			dests.put(dest.getMap(), dest);
		}
		for (int i = 0; i <= PW_WIDTH; i++) {
			for (int j = 0; j <= PW_HEIGHT; j++) {
				int pwX = i + PW_START_X;
				int pwY = j + PW_START_Y;
				RBMap map = RBMap.getMapByPosition(pwX, pwY);
				int tileX = pwX - map.getPokeworldOffsetX();
				int tileY = pwY - map.getPokeworldOffsetY();
				pw[i][j] = map.getOverworldTile(tileX, tileY);
			}
		}
		for (int i = 0; i <= PW_WIDTH; i++) {
			for (int j = 0; j <= PW_HEIGHT; j++) {
				if (pw[i][j] == null) {
					// tile is solid, don't want to waste calculating paths
					continue;
				}
				RBMap map = RBMap.getMapByID(pw[i][j].getMap());
				RBMapDestination dest = dests.get(map);
				if (dest == null) {
					dest = new RBMapDestination(map);
					dests.put(map, dest);
				}
				OverworldTilePath path = new OverworldTilePath(pw[i][j], dest);
				if (path.getShortestPath() == null) {
					// could not find a path to the destination
					continue;
				}
				List<Node> shortestPath = path.getShortestPath();
				pw[i][j].setMinStepsToGrass(shortestPath.size());
				paths.put(pw[i][j], shortestPath);
			}
		}
		for (int i = 0; i <= PW_WIDTH; i++) {
			for (int j = 0; j <= PW_HEIGHT; j++) {
				if (pw[i][j] == null) {
					// tile is solid, don't want to waste calculating paths
					continue;
				}
				RBMap map = RBMap.getMapByID(pw[i][j].getMap());
				RBMapDestination dest = dests.get(map);
				List<Node> path = paths.get(pw[i][j]);
				if (path == null) {
					continue;
				}
				if (dest.getMode() == RBMapDestination.WEST_CONNECTION
						&& path.get(path.size() - 1).getPosition().x == 0) {
					RBMap destMap = map.getWestConnection();
					int tileX = destMap.getWidthInTiles() - 1;
					int tileY = path.get(path.size() - 1).getPosition().y + map.getPokeworldOffsetY()
							- destMap.getPokeworldOffsetY();
					pw[i][j].setMinStepsToGrass(pw[i][j].getMinStepsToGrass()
							+ destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == RBMapDestination.EAST_CONNECTION
						&& path.get(path.size() - 1).getPosition().x == map.getWidthInTiles() - 1) {
					RBMap destMap = map.getWestConnection();
					int tileX = destMap.getWidthInTiles() + 1;
					int tileY = path.get(path.size() - 1).getPosition().y + map.getPokeworldOffsetY()
							- destMap.getPokeworldOffsetY();
					pw[i][j].setMinStepsToGrass(pw[i][j].getMinStepsToGrass()
							+ destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == RBMapDestination.SOUTH_CONNECTION
						&& path.get(path.size() - 1).getPosition().y == map.getHeightInTiles() - 1) {
					RBMap destMap = map.getWestConnection();
					int tileX = path.get(path.size() - 1).getPosition().x + map.getPokeworldOffsetX()
							- destMap.getPokeworldOffsetX();
					int tileY = destMap.getHeightInTiles() + 1;
					pw[i][j].setMinStepsToGrass(pw[i][j].getMinStepsToGrass()
							+ destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == RBMapDestination.NORTH_CONNECTION
						&& path.get(path.size() - 1).getPosition().y == 0) {
					RBMap destMap = map.getWestConnection();
					int tileX = path.get(path.size() - 1).getPosition().x + map.getPokeworldOffsetX()
							- destMap.getPokeworldOffsetX();
					int tileY = destMap.getHeightInTiles() - 1;
					pw[i][j].setMinStepsToGrass(pw[i][j].getMinStepsToGrass()
							+ destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
			}
		}
		for (int i = 0; i <= PW_WIDTH; i++) {
			for (int j = 0; j <= PW_HEIGHT; j++) {
				if (pw[i][j] == null) {
					// tile is solid, don't want to waste calculating edges
					continue;
				}
				RBMap map = RBMap.getMapByID(pw[i][j].getMap());
				RBMapTile tile = map.getTile(pw[i][j].getX(), pw[i][j].getY());
				if (tile.canMoveLeft()) {
					if (i != 0) {
						OverworldTile destTile = pw[i - 1][j];
						if (destTile != null) {
							int cost = Math.abs(
									numFramesPerStep * (destTile.getMinStepsToGrass() - pw[i][j].getMinStepsToGrass() + 1));
							RBMap destMap = RBMap.getMapByID(destTile.getMap());
							if (destMap.getTile(destTile.getX(), destTile.getY()).isGrassTile()
									&& dests.get(destMap).getMode() == RBMapDestination.GRASS_PATCHES) {
								cost = 0;
							}
							pw[i][j].addEdge(new OverworldEdge(OverworldAction.LEFT, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveRight()) {
					if (i != PW_WIDTH) {
						OverworldTile destTile = pw[i + 1][j];
						if (destTile != null) {
							int cost = Math.abs(
									numFramesPerStep * (destTile.getMinStepsToGrass() - pw[i][j].getMinStepsToGrass() + 1));
							RBMap destMap = RBMap.getMapByID(destTile.getMap());
							if (destMap.getTile(destTile.getX(), destTile.getY()).isGrassTile()
									&& dests.get(destMap).getMode() == RBMapDestination.GRASS_PATCHES) {
								cost = 0;
							}
							pw[i][j].addEdge(new OverworldEdge(OverworldAction.RIGHT, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveUp()) {
					if (j != 0) {
						OverworldTile destTile = pw[i][j - 1];
						if (destTile != null) {
							int cost = Math.abs(
									numFramesPerStep * (destTile.getMinStepsToGrass() - pw[i][j].getMinStepsToGrass() + 1));
							RBMap destMap = RBMap.getMapByID(destTile.getMap());
							if (destMap.getTile(destTile.getX(), destTile.getY()).isGrassTile()
									&& dests.get(destMap).getMode() == RBMapDestination.GRASS_PATCHES) {
								cost = 0;
							}
							pw[i][j].addEdge(new OverworldEdge(OverworldAction.UP, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveDown()) {
					if (j != PW_HEIGHT) {
						OverworldTile destTile = pw[i][j + 1];
						if (destTile != null) {
							int cost = Math.abs(
									numFramesPerStep * (destTile.getMinStepsToGrass() - pw[i][j].getMinStepsToGrass() + 1));
							RBMap destMap = RBMap.getMapByID(destTile.getMap());
							if (destMap.getTile(destTile.getX(), destTile.getY()).isGrassTile()
									&& dests.get(destMap).getMode() == RBMapDestination.GRASS_PATCHES) {
								cost = 0;
							}
							pw[i][j].addEdge(new OverworldEdge(OverworldAction.DOWN, cost, numFramesPerStep, destTile));
						}
					}
				}
				pw[i][j].addEdge(new OverworldEdge(OverworldAction.A, 2, 2, pw[i][j]));
				// TODO: Generic SBCost
				int sbcost = (i <= 39 - 30) ? 53 : 54;
				pw[i][j].addEdge(new OverworldEdge(OverworldAction.START_B, sbcost, sbcost, pw[i][j]));
				pw[i][j].addEdge(new OverworldEdge(OverworldAction.S_A_B_S, sbcost + 30, sbcost + 30, pw[i][j]));
				pw[i][j].addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, sbcost + 60, sbcost + 60, pw[i][j]));
				// isViridianNPC ??
				if (!map.getTile(pw[i][j].getX(), pw[i][j].getY()).isInVisionOfNPC()) {
					saveTiles.add(new SaveTile(pw[i][j], 0, map.getId() == 33 && pw[i][j].getX() < 13));
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Generic edge generation time: " + (endTime - startTime) + " ms");
	}
}
