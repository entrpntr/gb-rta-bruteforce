package dabomstew.nidorta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import mrwint.gbtasgen.Gb;

public class NidoBot {

	public static final int A = 0x01;
	public static final int B = 0x02;
	public static final int SELECT = 0x04;
	public static final int START = 0x08;

	public static final int RIGHT = 0x10;
	public static final int LEFT = 0x20;
	public static final int UP = 0x40;
	public static final int DOWN = 0x80;

	public static final int RESET = 0x800;

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static Gb gb;
	public static GBMemory mem;
	public static NidoGBWrapper wrap;

	public static String getUniqid() {
		StringBuilder sb = new StringBuilder();
		sb.append(gb.getDivState());
		sb.append("-");
		sb.append(mem.getHRA());
		sb.append("-");
		sb.append(mem.getHRS());
		sb.append("-");
		sb.append(mem.getTurnFrameStatus());
		sb.append("-");
		sb.append(mem.getX());
		sb.append("-");
		sb.append(mem.getY());
		for (int i = 1; i <= 15; i++) {
			// NPC counters
			sb.append("-");
			sb.append(mem.read(0xC208 + i * 16));
		}
		return sb.toString();
	}

	public static String getRNGState() {
		StringBuilder sb = new StringBuilder();
		sb.append(gb.getDivState());
		sb.append("-");
		sb.append(mem.getHRA());
		sb.append("-");
		sb.append(mem.getHRS());
		return sb.toString();
	}

	public static String getRNGStateHRAOnly() {
		StringBuilder sb = new StringBuilder();
		sb.append(gb.getDivState());
		sb.append("-");
		sb.append(mem.getHRA());
		return sb.toString();
	}

	public static String inputName(int input) {
		switch (input) {
		case A:
			return "A";
		case B:
			return "B";
		case SELECT:
			return "s";
		case START:
			return "s";
		case UP:
			return "U";
		case DOWN:
			return "D";
		case LEFT:
			return "L";
		case RIGHT:
			return "R";
		default:
			return "";
		}
	}

	public static boolean[] godStats;
	private static String runName;
	private static final int joypadAddr = 0x019A;
	private static final int animateNidorinoAddr = 0x41793;
	private static final int checkInterruptAddr = 0x12F8;
	private static final int joypadOverworldAddr = 0x0F4D;
	private static final int printLetterDelayAddr = 0x38D3;
	private static final int newBattleAddr = 0x0683;
	public static final int[] hopCosts = { 0, 130, 190, 299, 447 };

	public static int aCount(String path, int plen) {
		int ctr = 0;
		for (int i = 0; i < plen; i++) {
			if (path.charAt(i) == 'A') {
				ctr++;
			}
		}
		return ctr;
	}

	public static int inputsUntilNextA(String path) {
		if (maxAPresses == 0) {
			return 99999;
		}
		int plen = path.length();
		if (plen == 1) {
			return 0;
		} else if (plen == 0) {
			return 1;
		} else {
			if (plen >= 2 + (maxAPresses - 1) * 3) {
				if (aCount(path, plen) >= maxAPresses) {
					return 99999;
				}
			}
			int idx = path.lastIndexOf('A');
			return idx == -1 ? 0 : (idx == plen - 1 ? 2 : (idx == plen - 2 ? 1 : 0));
		}
	}

	// Config

	public static final int maxAPresses = 3;
	public static final int minAPresses = 0;
	public static int hops = 0;
	public static final String gameName = "red";
	public static final boolean checkExtraStepStartingPoints = true;
	public static final int godDefenseDV = 15;
	public static final int godSpecialDV = 15;
	public static final int maxCostAtStart = 999999;
	public static final int maxCostOfPath = 999999;
	public static final int maxStepsInGrassArea = 50;

	public static void main(String[] args) throws IOException {
	    
	    // Make folder if necessary
	    if(!new File("logs").exists()) {
	        new File("logs").mkdir();
	    }
	    
	    if(!new File("roms").exists()) {
            new File("roms").mkdir();
            System.err.println("I need ROMs to simulate!");
            System.exit(0);
        }
	    
	    // Check byte buffer limits
	    int maxBuffers = 0;
	    int bbLimit = 0;
	    List<ByteBuffer> allMyBuffers = new ArrayList<ByteBuffer>();
	    try {
	        while(true) {
	            allMyBuffers.add(Gb.createDirectByteBuffer(190000));
	            maxBuffers++;
	        }
	    }
	    catch (OutOfMemoryError ex) {
	        bbLimit = maxBuffers*95/100;
	        System.out.println("ran out of memory at "+maxBuffers+" byte buffers, set limit to "+bbLimit);
        }
	    allMyBuffers.clear();
	    allMyBuffers = null;
	    System.gc();

		// Init gambatte with 1 screen
		Gb.loadGambatte(1);

		for (hops = 1; hops < 3; hops++) {

			// config

			runName = "nopalette_" + gameName + "_hop_" + hops;
			initLog();

			int[] firstLoopAddresses = { joypadOverworldAddr };
			int[] subsLoopAddresses = { joypadOverworldAddr, printLetterDelayAddr };

			// setup starting positions
			// Compile starting positions
			StartingPositionManager spm = new StartingPositionManager();
			spm.includeRect(35, 182, 37, 182);
			spm.includeRect(35, 177, 37, 182);
			spm.includeRect(36, 176, 46, 179);
			spm.includeRect(46, 176, 46, 179);
			spm.includeRect(44, 178, 47, 180);

			// viridian area now
			spm.includeRect(48, 176, 75, 183);
			spm.excludeRect(48, 176, 55, 178); // walls
			spm.excludeRect(56, 176, 56, 177); // walls
			spm.exclude(57, 179); // sign
			spm.excludeRect(60, 176, 63, 177); // house
			spm.excludeRect(60, 179, 63, 179); // wall in front of house
			spm.excludeRect(68, 178, 71, 181); // mart
			spm.excludeRect(62, 184, 65, 187); // pokecenter
			spm.exclude(48, 184); // cut bush
			spm.excludeRect(48, 185, 49, 185); // bushes
			spm.excludeRect(48, 186, 53, 189); // surf pond
			spm.excludeNpc(53, 182); // walker on way to r22
			spm.excludeNpc(46, 185); // fat guy
			spm.excludeNpc(70, 187); // mart guy
			spm.excludeRect(64, 176, 75, 177); // too many extra steps
			spm.excludeRect(72, 178, 75, 181); // likewise
			Map<String, Integer> encountersCosts = new HashMap<>();
			Map<String, Integer> startPositionsCosts = new HashMap<>();
			Map<String, List<String>> startPositionsEncs = new HashMap<>();

			// god nidos
			godStats = new boolean[65536];
			for (int defDV = godDefenseDV; defDV <= 15; defDV++) {
				for (int spcDV = godSpecialDV; spcDV <= 15; spcDV++) {
					for (int variance = -8; variance < 9; variance++) {
						int atkDef = (0xF0 + defDV + variance) & 0xFF;
						int speSpc = (0xE0 + spcDV + variance) & 0xFF;
						godStats[(atkDef << 8) | speSpc] = true;
					}
				}
			}

			int importedPos = 0;
			if (new File(gameName + "_statesimport.txt").exists()) {
				Scanner sc = new Scanner(new File(gameName + "_statesimport.txt"), "UTF-8");
				while (sc.hasNextLine()) {
					String ln = sc.nextLine().trim();
					if (!ln.isEmpty()) {
						String secondln = sc.nextLine().trim();
						int count = Integer.parseInt(secondln);
						startPositionsCosts.put(ln, count);
						startPositionsEncs.put(ln, new ArrayList<String>());
						String encLn = sc.nextLine().trim();
						while (!encLn.equalsIgnoreCase("EOSTATE")) {
							startPositionsEncs.get(ln).add(encLn);
							encLn = sc.nextLine().trim();
						}
						importedPos++;
					}
				}
				sc.close();

			}
			logF("Imported %d old start positions already checked\n", importedPos);
			int importedEncs = 0;
			if (new File(gameName + "_encounters.txt").exists()) {
				Scanner sc = new Scanner(new File(gameName + "_encounters.txt"), "UTF-8");
				while (sc.hasNextLine()) {
					String ln = sc.nextLine().trim();
					if (!ln.isEmpty()) {
						String secondln = sc.nextLine().trim();
						int count = Integer.parseInt(secondln);
						encountersCosts.put(ln, count);
						importedEncs++;
					}
				}
				sc.close();

			}
			logF("Imported %d old encounters already checked\n", importedEncs);
			long starttime = System.currentTimeMillis() / 1000L;
			String logFilename = "logs/nidolog_" + runName + "_" + starttime + ".log";
			if (new File(logFilename).exists()) {
				new File(logFilename).delete();
			}
			PrintStream ps = new PrintStream(logFilename, "UTF-8");

			for (Position pos : spm) {
				try {
					int baseCost = StartingExtraSteps.getSteps(pos.map, pos.x, pos.y) * 17;
					if (baseCost > 0 && !checkExtraStepStartingPoints) {
						logLN("not checking extra steps for now");
						continue;
					}
					baseCost += hopCosts[hops];

					ps.printf("Starting position: map %d x %d y %d cost %d\n", pos.map, pos.x, pos.y, baseCost);
					logF("testing starting position x=%d y=%d map=%d cost=%d\n", pos.x, pos.y, pos.map, baseCost);

					if (baseCost >= maxCostAtStart) {
						ps.println("too much guaranteed time loss, not checking this one");
						logLN("too much guaranteed time loss, not checking this one");
						continue;
					}

					if (pos.map == 1) {
						// Viridian
						makeSave("viridian", pos.x, pos.y);
					} else {
						makeSave("route22_" + gameName, pos.x, pos.y);
					}

					gb = new Gb(0, false);
					gb.startEmulator("roms/" + gameName + ".gb");

					gb.step(0); // let gambatte initialize itself
					mem = new GBMemory();
					wrap = new NidoGBWrapper(mem);
					int introInputCtr = 0;
					int[] introInputs = { B | SELECT | UP, B | SELECT | UP, START, A, A };
					if (hops > 0) {
						introInputs = new int[] { A, START, A, START | A, START | A };
					}
					while (introInputCtr < 5) {
						wrap.advanceToAddress(joypadAddr);
						// inject intro inputs
						wrap.injectInput(introInputs[introInputCtr++]);
						wrap.advanceFrame();
						if (introInputCtr == 1) {
							// hops?
							for (int h = 0; h < hops; h++) {
								// find an AnimateNidorino
								wrap.advanceToAddress(animateNidorinoAddr);
								// find a CheckForUserInterruption
								wrap.advanceToAddress(checkInterruptAddr);
							}
						}
					}

					// overworld loop
					boolean checkingPaths = true;
					Set<String> seenStates = new HashSet<String>();
					Map<String, String> statePaths = new HashMap<String, String>();
					Stack<OverworldStateAction> actionQueue = new Stack<OverworldStateAction>();
					Set<PositionEnteringGrass> endPositions = new HashSet<PositionEnteringGrass>();
					int numEndPositions = 0, numStatesChecked = 0;
					String lastPath = "";
					long start = System.currentTimeMillis();
					int[] addresses = firstLoopAddresses;
					int startX = -1, startY = -1;
					int lastInput = 0;
					while (checkingPaths) {
						int result = wrap.advanceToAddress(addresses);
						String curState = getUniqid();

						boolean garbage = mem.getTurnFrameStatus() != 0 || result != joypadOverworldAddr;
						if (!garbage && lastInput != 0 && lastInput != A) {
							if (mem.getX() == startX && mem.getY() == startY) {
								garbage = true;
							}
						}
						if (!garbage) {
							int[] actionList = PermissibleActionsHandler.actionsGoingToGrass(mem.getMap(), mem.getX(),
									mem.getY());
							int inputsNextA = inputsUntilNextA(lastPath);
							if (!seenStates.contains(curState)) {
								seenStates.add(curState);
								statePaths.put(curState, lastPath);
								ByteBuffer curSave = gb.saveState();
								if (actionList.length > 0) {
									for (int input : actionList) {
										OverworldStateAction action = new OverworldStateAction(curState, curSave,
												input);
										actionQueue.add(action);
									}
									if (inputsNextA == 0) {
										OverworldStateAction action = new OverworldStateAction(curState, curSave, A);
										actionQueue.add(action);
									}
								} else {
									if (minAPresses == 0 || aCount(lastPath, lastPath.length()) >= minAPresses) {
										endPositions.add(new PositionEnteringGrass(curSave, lastPath, getRNGState()));
										numEndPositions++;
										if (numEndPositions >= bbLimit) {
											long end = System.currentTimeMillis();
											logLN(".done part 1; cutoff early after " + numStatesChecked
													+ " states and found " + numEndPositions + " results in "
													+ (end - start) + "ms");
											checkingPaths = false;
										}
									}
								}
							} else if (inputsNextA < inputsUntilNextA(statePaths.get(curState))) {
								statePaths.put(curState, lastPath);
								if (inputsNextA == 0 && actionList.length > 0) {
									ByteBuffer curSave = gb.saveState();
									OverworldStateAction action = new OverworldStateAction(curState, curSave, A);
									actionQueue.push(action);
								}
							}
						}

						// Next position
						if (!actionQueue.isEmpty() && checkingPaths) {
							numStatesChecked++;
							addresses = subsLoopAddresses;
							OverworldStateAction actionToTake = actionQueue.pop();
							String inputRep = inputName(actionToTake.nextInput);
							gb.loadState(actionToTake.savedState);
							wrap.injectInput(actionToTake.nextInput);
							lastInput = actionToTake.nextInput;
							startX = mem.getX();
							startY = mem.getY();
							lastPath = statePaths.get(actionToTake.statePos) + inputRep;
							// skip the joypadoverworld we just hit
							wrap.advanceFrame();
						} else if (checkingPaths) {
							long end = System.currentTimeMillis();
							logLN(".done part 1; checked " + numStatesChecked + " states and found " + numEndPositions
									+ " results in " + (end - start) + "ms");
							checkingPaths = false;
						}
					}
					ps.flush();

					// Encounter bruteforcing

					long lastOffset = System.currentTimeMillis();
					{

						for (PositionEnteringGrass peg : endPositions) {
							int pathACost = aCount(peg.path, peg.path.length()) * 2;
							int pathCost = baseCost + pathACost;
							if (pathCost >= maxCostOfPath) {
								// too long, not interested
								continue;
							}
							if (!startPositionsCosts.containsKey(peg.rngState)) {
								startPositionsCosts.put(peg.rngState, pathCost);
								startPositionsEncs.put(peg.rngState, new ArrayList<String>());
								int oogDir = LEFT;
								ByteBuffer curState = peg.savedState;
								gb.loadState(curState);
								int maxSteps = Math.min((maxCostOfPath - pathCost) / 17, maxStepsInGrassArea);
								for (int step = 0; step < maxSteps; step++) {
									int numSteps = step + 1;
									if (step % 2 == 1) {
										numSteps++;
									}
									int stepsFrameCost = numSteps * 17;

									if (pathCost + stepsFrameCost >= maxCostOfPath) {
										// too long, not interested
										break;
									}
									// first try stepping into the grass
									wrap.injectInput(UP);
									wrap.advanceToAddress(newBattleAddr);

									// encounter found?
									if (mem.getHRA() >= 0 && mem.getHRA() <= 5) { // 24
										// ok got possible FFEF encounter, note
										// what it is
										String rngAtEnc = getRNGState();
										wrap.advanceFrame();
										wrap.advanceFrame();
										Encounter enc = new Encounter(mem.getEncounterSpecies(),
												mem.getEncounterLevel(), mem.getEncounterDVs(), getRNGStateHRAOnly());

										int totalEncCost = pathCost + stepsFrameCost;
										String encRep = enc.toString();
										startPositionsEncs.get(peg.rngState)
												.add(encRep + "/" + stepsFrameCost + "/" + rngAtEnc + "/" + step);

										if (!encountersCosts.containsKey(encRep)
												|| encountersCosts.get(encRep) > totalEncCost) {
											ps.printf(
													"inputs %s step %d cost %d encounter: species %d lv%d DVs %04X rng %s encrng %s\n",
													peg.path, step + 1, totalEncCost, enc.species, enc.level, enc.dvs,
													enc.battleRNG, rngAtEnc);
											encountersCosts.put(encRep, totalEncCost);
										}

										if (enc.species == 3 && enc.level == 4 && godStats[enc.dvs]) {
											logLN("POTENTIAL GOD NIDO FOUND!");
											logF("inputs %s step %d cost %d encounter: species %d lv%d DVs %04X rng %s encrng %s\n",
													peg.path, step + 1, totalEncCost, enc.species, enc.level, enc.dvs,
													enc.battleRNG, rngAtEnc);
										}
									}

									// progress
									gb.loadState(curState);
									wrap.injectInput(oogDir);
									// skip past OJP we just hit, and then reach
									// next one
									wrap.advanceFrame();
									wrap.advanceToAddress(joypadOverworldAddr);
									// state save for next loop
									curState = gb.saveState();
									// change out-of-grass walking direction?
									if (mem.getX() == 30 && oogDir == LEFT) {
										oogDir = RIGHT;
									} else if (mem.getX() == 33 && oogDir == RIGHT) {
										oogDir = LEFT;
									}
								}
							} else if (pathCost < startPositionsCosts.get(peg.rngState)) {
								// Don't retest, but do reconsider encounters
								startPositionsCosts.put(peg.rngState, pathCost);
								for (String stateEnc : startPositionsEncs.get(peg.rngState)) {
									String[] encBits = stateEnc.split("\\/");
									int cost = Integer.parseInt(encBits[4]);
									int step = Integer.parseInt(encBits[6]);
									String encRep = encBits[0] + "/" + encBits[1] + "/" + encBits[2] + "/" + encBits[3];
									int totalEncCost = pathCost + cost;
									if (encountersCosts.get(encRep) > totalEncCost) {
										ps.printf(
												"inputs %s step %d cost %d encounter: species %s lv%s DVs %s rng %s encrng %s\n",
												peg.path, step + 1, totalEncCost, encBits[0], encBits[1], encBits[2],
												encBits[3], encBits[5]);
										encountersCosts.put(encRep, totalEncCost);
									}
								}
							}
						}
					}

					logLN(".done part 2 in " + (System.currentTimeMillis() - lastOffset) + "ms");
				} catch (OutOfMemoryError ex) {
					logLN("failed due to memory fail");
				}
				System.gc();
			}
			ps.flush();
			ps.close();

			long currtime = System.currentTimeMillis() / 1000L;

			PrintStream statesLog = new PrintStream("logs/statestested_" + runName + "_" + currtime + ".log", "UTF-8");
			for (String state : startPositionsCosts.keySet()) {
				statesLog.println(state);
				statesLog.println(startPositionsCosts.get(state));
				for (String stateEnc : startPositionsEncs.get(state)) {
					statesLog.println(stateEnc);
				}
				statesLog.println("EOSTATE");
			}
			statesLog.flush();
			statesLog.close();
			logLN("dumped rng states to statestested_" + runName + "_" + currtime + ".log");
			Files.copy(new File("logs/statestested_" + runName + "_" + currtime + ".log").toPath(),
					new File(gameName + "_statesimport.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);

			PrintStream encsLog = new PrintStream("logs/encsfound_" + runName + "_" + currtime + ".log", "UTF-8");
			for (String enc : encountersCosts.keySet()) {
				encsLog.println(enc);
				encsLog.println(encountersCosts.get(enc));
			}
			encsLog.flush();
			encsLog.close();
			logLN("dumped encounters to encsfound_" + runName + "_" + currtime + ".log");
			Files.copy(new File("logs/encsfound_" + runName + "_" + currtime + ".log").toPath(),
					new File(gameName + "_encounters.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);

			closeLog();
		}
	}

	public static void makeSave(String baseName, int x, int y) throws IOException {
		byte[] baseSave = FileFunctions.readFileFullyIntoBuffer("baseSaves/" + baseName + ".sav");
		int mapWidth = 20; // temp, but applies to both viridian and route 22
		int baseX = x;
		int baseY = y;
		int tlPointer = 0xC6E8 + (baseY / 2 + 1) * (mapWidth + 6) + (baseX / 2 + 1);
		baseSave[0x260B] = (byte) (tlPointer & 0xFF);
		baseSave[0x260C] = (byte) (tlPointer >> 8);
		baseSave[0x260D] = (byte) baseY;
		baseSave[0x260E] = (byte) baseX;
		baseSave[0x260F] = (byte) (baseY % 2);
		baseSave[0x2610] = (byte) (baseX % 2);
		int csum = 0;
		for (int i = 0x2598; i < 0x3523; i++) {
			csum += baseSave[i] & 0xFF;
		}
		baseSave[0x3523] = (byte) ((csum & 0xFF) ^ 0xFF); // cpl
		FileFunctions.writeBytesToFile("roms/" + gameName + ".sav", baseSave);
	}

	private static PrintStream mainLogCopy;

	public static void initLog() throws FileNotFoundException, UnsupportedEncodingException {
		long currtime = System.currentTimeMillis() / 1000L;
		mainLogCopy = new PrintStream("logs/consolelog_" + runName + "_" + currtime + ".log", "UTF-8");
	}

	public static void closeLog() {
		mainLogCopy.flush();
		mainLogCopy.close();
	}

	public static void logF(String format, Object... args) {
		logDate();
		System.out.printf(format, args);
		mainLogCopy.printf(format, args);
		mainLogCopy.flush();
	}

	public static void logLN(String ln) {
		logDate();
		System.out.println(ln);
		mainLogCopy.println(ln);
		mainLogCopy.flush();
	}

	private static void logDate() {
		Date date = new Date();
		mainLogCopy.print("[" + dateFormat.format(date) + "] ");
	}

}
