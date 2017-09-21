package dabomstew.rta.yellowmoon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dabomstew.rta.BufferUtils;
import dabomstew.rta.GBMemory;
import dabomstew.rta.GBMemoryYellow;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.Position;
import dabomstew.rta.YellowAddr;
import dabomstew.rta.astar.Location;
import dabomstew.rta.astar.Node;
import dabomstew.rta.ffef.OverworldAction;
import dabomstew.rta.ffef.OverworldEdge;
import dabomstew.rta.ffef.OverworldTile;
import dabomstew.rta.ffef.OverworldTilePath;
import dabomstew.rta.generic.Map;
import dabomstew.rta.generic.MapDestination;
import dabomstew.rta.generic.Tile;
import mrwint.gbtasgen.Gb;

public class YellowMoonBot {

	public static final int NO_INPUT = 0x00;

	public static final int A = 0x01;
	public static final int B = 0x02;
	public static final int SELECT = 0x04;
	public static final int START = 0x08;

	public static final int RIGHT = 0x10;
	public static final int LEFT = 0x20;
	public static final int UP = 0x40;
	public static final int DOWN = 0x80;

	private static final String gameName;
	private static PrintWriter writer;
	private static PrintWriter particallyManips;
	private static PrintWriter foundManips;

	private static final int MAX_COST;

	private static final Location start = new Location(11, 31);
	private static final Location dest = new Location(12, 9);
	private static final int MIN_CONSISTENCY = 53;

	static {
		MAX_COST = 9;
		gameName = "yellow";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
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

		long startTime = System.currentTimeMillis();

		ByteBuffer[] saves = new ByteBuffer[60];

		for (int i = 0; i < 60; i++) {
			if (!(new File("./states/" + i).exists())) {
				continue;
			}
			saves[i] = BufferUtils.loadByteBufferFromFile("./states/" + i + ".st");
		}

		System.out.println("All saves loaded! " + (System.currentTimeMillis() - startTime));

		writer = new PrintWriter(new File(gameName + "_moon_paths.txt"));
		foundManips = new PrintWriter(new File(gameName + "_foundManips.txt"));
		particallyManips = new PrintWriter(new File(gameName + "_partially_paths.txt"));

		ArrayList<IntroSequence> introSequences = new ArrayList<>();
		introSequences.add(new IntroSequence(gfSkip, pikachu0, title0, cont, cont));
		Collections.sort(introSequences);
		System.out.println("Number of intro sequences: " + introSequences.size());

		OverworldTile[][] owTiles1 = initTiles(Map.MT_MOON_3, false, new MapDestination(Map.MT_MOON_3, dest));
		
		OverworldTile savePos = owTiles1[start.x][start.y];
		Gb.loadGambatte(1);
		gb = new Gb(0, false);
		gb.startEmulator("roms/poke" + gameName + ".gbc");
		mem0 = new GBMemory(gb);
		wrap = new GBWrapper(gb, mem0);
		mem = new GBMemoryYellow(gb);
		for (IntroSequence intro : introSequences) {
			wrap.advanceToAddress(YellowAddr.joypadAddr);
			OverworldStateMP owState = new OverworldStateMP(savePos.toString() + " - " + intro.toString() + ":", savePos, saves, 1, true, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), 0, 0);
			overworldSearch(owState);
		}
		writer.close();
	}

	private static void overworldSearch(OverworldStateMP ow) {
		if (ow.getWastedFrames() > MAX_COST) {
			return;
		}
		for (OverworldEdge edge : ow.getPos().getEdgeList()) {
			OverworldAction edgeAction = edge.getAction();
			if (ow.aPressCounter() > 0 && (edgeAction == OverworldAction.A || edgeAction == OverworldAction.START_B || edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
				continue;
			}
			if (!ow.canPressStart() && (edgeAction == OverworldAction.START_B || edgeAction == OverworldAction.S_A_B_S || edgeAction == OverworldAction.S_A_B_A_B_S)) {
				continue;
			}
			int edgeCost = edge.getCost();
			if (ow.getWastedFrames() + edgeCost > MAX_COST) {
				continue;
			}
			OverworldStateMP newState;
			int owFrames = ow.getOverworldFrames() + edge.getFrames();
			//TODO: Only igt60 check if added action is a directional
			ByteBuffer[] newSaves = checkIGT0(ow, edgeAction);
			int igt0 = 60;
			for (int i = 0; i < 60; i++) {
				if (newSaves[i] == null) {
					igt0--;
				}
			}
			if (igt0 < MIN_CONSISTENCY) {
				continue;
			}
			switch (edgeAction) {
			case LEFT:
			case UP:
			case RIGHT:
			case DOWN:
				particallyManips.println(ow.toString() + " " + edgeAction.logStr() + ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames) + " - " + igt0 + "/60");
				particallyManips.flush();
				if (edge.getNextPos().getX() == dest.x && edge.getNextPos().getY() == dest.y && igt0 >= MIN_CONSISTENCY) {
					foundManips.println(ow.toString() + " " + edgeAction.logStr() + ", cost: " + (ow.getWastedFrames() + edgeCost) + ", owFrames: " + (owFrames) + " - " + igt0 + "/60 rng: " + rng);
					foundManips.flush();
					break;
				}
				newState = new OverworldStateMP(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), newSaves, Math.max(0, ow.aPressCounter() - 1), true, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + edgeCost, ow.getOverworldFrames() + edge.getFrames());
				overworldSearch(newState);
				break;
			case A:
				newState = new OverworldStateMP(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), newSaves, 2, true, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + 2, ow.getOverworldFrames() + 2);
				overworldSearch(newState);
				break;
			case START_B:
				newState = new OverworldStateMP(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), newSaves, 1, true, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + edgeCost, ow.getOverworldFrames() + edge.getFrames());
				overworldSearch(newState);
				break;
			case S_A_B_S:
				newState = new OverworldStateMP(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), newSaves, 1, false, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + edgeCost, ow.getOverworldFrames() + edge.getFrames());
				overworldSearch(newState);
				break;
			case S_A_B_A_B_S:
				newState = new OverworldStateMP(ow.toString() + " " + edgeAction.logStr(), edge.getNextPos(), newSaves , 1, false, gb.getDivState(), mem.getHRA(), mem.getHRS(), mem.getTurnFrameStatus(), mem.getNPCTimers(), ow.getWastedFrames() + edgeCost, ow.getOverworldFrames() + edge.getFrames());
				overworldSearch(newState);
				break;
			default:
				break;
			}
		}
	}
	
	private static int rng = 0;

	private static ByteBuffer[] checkIGT0(OverworldStateMP state, OverworldAction action) {
		rng = 0;
		ByteBuffer[] resultSaves = new ByteBuffer[60];
		for (int i = 0; i < 60; i++) {
			if (state.getSaves()[i] == null) {
				resultSaves[i] = null;
				continue;
			}
			gb.loadState(state.getSaves()[i]);
			wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
			if (!execute(state, action)) {
				resultSaves[i] = null;
				continue;
			} else {
				rng += mem.getHRA() + mem.getHRS();
				resultSaves[i] = gb.saveState();
			}
		}
		return resultSaves;
	}

	private static boolean execute(OverworldStateMP state, OverworldAction owAction) {
		int res;
		switch (owAction) {
		case LEFT:
		case UP:
		case RIGHT:
		case DOWN:
			int input = 16 * (int) (Math.pow(2.0, (owAction.ordinal())));
			Position dest = getDestination(mem, input);
			wrap.injectYInput(input);
			wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr + 1);
			int result = wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr, YellowAddr.newBattleAddr);
			while (mem.getX() != dest.x || mem.getY() != dest.y) {
				if (result == YellowAddr.newBattleAddr) {
					int result2 = wrap.advanceWithJoypadToAddress(input, YellowAddr.encounterTestAddr, YellowAddr.joypadOverworldAddr);

					if (result2 == YellowAddr.encounterTestAddr) {
						int hra = mem.getHRA();
						if (hra < 10) {
							rng += hra + mem.getHRS();
							return false;
						}
					}
				}
				wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
				wrap.injectYInput(input);
				wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr + 1);
				result = wrap.advanceWithJoypadToAddress(input, YellowAddr.newBattleAddr, YellowAddr.joypadOverworldAddr);
			}
			int result2 = wrap.advanceToAddress(YellowAddr.encounterTestAddr, YellowAddr.joypadOverworldAddr);
			if (result2 == YellowAddr.encounterTestAddr) {
				int hra = mem.getHRA();
				if (hra < 10) {
					rng += hra + mem.getHRS();
					return false;
				}
				if (timeToPickUpItem(mem.getMap(), mem.getX(), mem.getY())) {
					wrap.injectYInput(A);
					wrap.advanceWithJoypadToAddress(A, YellowAddr.textJingleCommandAddr);
					wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
				}
				wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
			}
			return true;
		case A:
			wrap.injectYInput(A);
			wrap.advanceFrame(A);
			res = wrap.advanceWithJoypadToAddress(A, YellowAddr.joypadOverworldAddr, YellowAddr.printLetterDelayAddr);
			if (res == YellowAddr.joypadOverworldAddr) {
				return true;
			} else {
				System.out.println("REACHED PRINTLETTERDELAY");
				return false;
			}
		case START_B:
			wrap.injectYInput(START);
			wrap.advanceFrame(START);
			wrap.advanceWithJoypadToAddress(START, YellowAddr.joypadAddr);
			wrap.injectYInput(B);
			wrap.advanceFrame(B);
			wrap.advanceWithJoypadToAddress(B, YellowAddr.joypadOverworldAddr);
			return true;
		case S_A_B_S:
			wrap.injectYInput(START);
			wrap.advanceFrame(START);
			wrap.advanceToAddress(YellowAddr.joypadAddr);
			wrap.injectYInput(A);
			wrap.advanceFrame(A);
			wrap.advanceWithJoypadToAddress(A, YellowAddr.joypadAddr);
			wrap.injectYInput(B);
			wrap.advanceFrame(B);
			wrap.advanceWithJoypadToAddress(B, YellowAddr.joypadAddr);
			wrap.injectYInput(START);
			wrap.advanceFrame(START);
			wrap.advanceWithJoypadToAddress(START, YellowAddr.joypadOverworldAddr);
			return true;
		case S_A_B_A_B_S:
			wrap.injectYInput(START);
			wrap.advanceFrame(START);
			wrap.advanceWithJoypadToAddress(START, YellowAddr.joypadAddr);
			wrap.injectYInput(A);
			wrap.advanceFrame(A);
			wrap.advanceWithJoypadToAddress(A, YellowAddr.joypadAddr);
			wrap.injectYInput(B);
			wrap.advanceFrame(B);
			wrap.advanceWithJoypadToAddress(B, YellowAddr.joypadAddr);
			wrap.injectYInput(A);
			wrap.advanceFrame(A);
			wrap.advanceWithJoypadToAddress(A, YellowAddr.joypadAddr);
			wrap.injectYInput(B);
			wrap.advanceFrame(B);
			wrap.advanceWithJoypadToAddress(B, YellowAddr.joypadAddr);
			wrap.injectYInput(START);
			wrap.advanceFrame(START);
			wrap.advanceWithJoypadToAddress(START, YellowAddr.joypadOverworldAddr);
			return true;
		default:
			return false;
		}

	}

	private static Gb gb;
	private static GBWrapper wrap;
	private static GBMemory mem0;
	private static GBMemoryYellow mem;

	private static OverworldTile[][] initTiles(Map targetMap, boolean onBike, MapDestination... destinations) {
		OverworldTile[][] tiles = new OverworldTile[targetMap.getWidthInTiles() + 1][targetMap.getHeightInTiles() + 1];
		long startTime = System.currentTimeMillis();
		int numFramesPerStep = onBike ? 9 : 17;
		int startX = targetMap.getPokeworldOffsetX();
		int startY = targetMap.getPokeworldOffsetY();
		int width = targetMap.getWidthInTiles();
		int height = targetMap.getHeightInTiles();
		HashMap<Map, MapDestination> dests = new HashMap<Map, MapDestination>();
		HashMap<OverworldTile, List<Node>> paths = new HashMap<OverworldTile, List<Node>>();
		for (MapDestination dest : destinations) {
			dests.put(dest.getMap(), dest);
		}
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				int pwX = i + startX;
				int pwY = j + startY;
				Map map = Map.getMapByPosition(pwX, pwY);
				int tileX = pwX - map.getPokeworldOffsetX();
				int tileY = pwY - map.getPokeworldOffsetY();
				OverworldTile baseTile = map.getOverworldTile(tileX, tileY);
				if (baseTile != null) {
					tiles[i][j] = new OverworldTile(baseTile.getMap(), baseTile.getX(), baseTile.getY(), baseTile.getClosestGrassTile());
				}
			}
		}
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				if (tiles[i][j] == null) {
					continue;
				}
				Map map = Map.getMapByID(tiles[i][j].getMap());
				MapDestination dest = dests.get(map);
				if (dest == null) {
					dest = new MapDestination(map);
					dests.put(map, dest);
				}
				OverworldTilePath path = new OverworldTilePath(tiles[i][j], dest);
				if (path.getShortestPath() == null) {
					continue;
				}
				List<Node> shortestPath = path.getShortestPath();
				tiles[i][j].setMinStepsToGrass(shortestPath.size());
				paths.put(tiles[i][j], shortestPath);
			}
		}
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				if (tiles[i][j] == null) {
					continue;
				}
				Map map = Map.getMapByID(tiles[i][j].getMap());
				MapDestination dest = dests.get(map);
				List<Node> path = paths.get(tiles[i][j]);
				if (path == null) {
					continue;
				}
				if (dest.getMode() == MapDestination.WEST_CONNECTION && path.get(path.size() - 1).getPosition().x == 0) {
					Map destMap = map.getWestConnection();
					int tileX = destMap.getWidthInTiles() - 1;
					int tileY = path.get(path.size() - 1).getPosition().y + map.getPokeworldOffsetY() - destMap.getPokeworldOffsetY();
					tiles[i][j].setMinStepsToGrass(tiles[i][j].getMinStepsToGrass() + destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == MapDestination.EAST_CONNECTION && path.get(path.size() - 1).getPosition().x == map.getWidthInTiles() - 1) {
					Map destMap = map.getWestConnection();
					int tileX = destMap.getWidthInTiles() + 1;
					int tileY = path.get(path.size() - 1).getPosition().y + map.getPokeworldOffsetY() - destMap.getPokeworldOffsetY();
					tiles[i][j].setMinStepsToGrass(tiles[i][j].getMinStepsToGrass() + destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == MapDestination.SOUTH_CONNECTION && path.get(path.size() - 1).getPosition().y == map.getHeightInTiles() - 1) {
					Map destMap = map.getWestConnection();
					int tileX = path.get(path.size() - 1).getPosition().x + map.getPokeworldOffsetX() - destMap.getPokeworldOffsetX();
					int tileY = destMap.getHeightInTiles() + 1;
					tiles[i][j].setMinStepsToGrass(tiles[i][j].getMinStepsToGrass() + destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
				if (dest.getMode() == MapDestination.NORTH_CONNECTION && path.get(path.size() - 1).getPosition().y == 0) {
					Map destMap = map.getWestConnection();
					int tileX = path.get(path.size() - 1).getPosition().x + map.getPokeworldOffsetX() - destMap.getPokeworldOffsetX();
					int tileY = destMap.getHeightInTiles() - 1;
					tiles[i][j].setMinStepsToGrass(tiles[i][j].getMinStepsToGrass() + destMap.getOverworldTile(tileX, tileY).getMinStepsToGrass());
				}
			}
		}
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				if (tiles[i][j] == null) {
					continue;
				}
				Map map = Map.getMapByID(tiles[i][j].getMap());
				Tile tile = map.getTile(tiles[i][j].getX(), tiles[i][j].getY());
				if (tile.canMoveLeft()) {
					if (i != 0) {
						OverworldTile destTile = tiles[i - 1][j];
						if (destTile != null) {
							int cost = Math.abs(numFramesPerStep * (destTile.getMinStepsToGrass() - tiles[i][j].getMinStepsToGrass() + 1));
							tiles[i][j].addEdge(new OverworldEdge(OverworldAction.LEFT, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveRight()) {
					if (i != width) {
						OverworldTile destTile = tiles[i + 1][j];
						if (destTile != null) {
							int cost = Math.abs(numFramesPerStep * (destTile.getMinStepsToGrass() - tiles[i][j].getMinStepsToGrass() + 1));
							tiles[i][j].addEdge(new OverworldEdge(OverworldAction.RIGHT, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveUp()) {
					if (j != 0) {
						OverworldTile destTile = tiles[i][j - 1];
						if (destTile != null) {
							int cost = Math.abs(numFramesPerStep * (destTile.getMinStepsToGrass() - tiles[i][j].getMinStepsToGrass() + 1));
							tiles[i][j].addEdge(new OverworldEdge(OverworldAction.UP, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (tile.canMoveDown()) {
					if (j != height) {
						OverworldTile destTile = tiles[i][j + 1];
						if (destTile != null) {
							int cost = Math.abs(numFramesPerStep * (destTile.getMinStepsToGrass() - tiles[i][j].getMinStepsToGrass() + 1));
							tiles[i][j].addEdge(new OverworldEdge(OverworldAction.DOWN, cost, numFramesPerStep, destTile));
						}
					}
				}
				if (!badAPresesTile(i, j)) {
					tiles[i][j].addEdge(new OverworldEdge(OverworldAction.A, 2, 2, tiles[i][j]));
				}
				int sbcost = 3;
				tiles[i][j].addEdge(new OverworldEdge(OverworldAction.START_B, sbcost, sbcost, tiles[i][j]));
				tiles[i][j].addEdge(new OverworldEdge(OverworldAction.S_A_B_S, sbcost + 30, sbcost + 30, tiles[i][j]));
				tiles[i][j].addEdge(new OverworldEdge(OverworldAction.S_A_B_A_B_S, sbcost + 60, sbcost + 60, tiles[i][j]));
				Collections.sort(tiles[i][j].getEdgeList());
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Generic edge generation time: " + (endTime - startTime) + " ms");
		return tiles;
	}

	public static boolean badAPresesTile(int x, int y) {
		if (x == 14 && y == 23) {
			return true;
		} else if (x == 15 && y == 24) {
			return true;
		} else if (x == 17 && y == 23) {
			return true;
		}
		return false;
	}

	public static Position getDestination(GBMemoryYellow mem, int input) {
		if (input == LEFT) {
			return new Position(mem.getMap(), mem.getX() - 1, mem.getY());
		} else if (input == RIGHT) {
			return new Position(mem.getMap(), mem.getX() + 1, mem.getY());
		} else if (input == UP) {
			return new Position(mem.getMap(), mem.getX(), mem.getY() - 1);
		} else if (input == DOWN) {
			return new Position(mem.getMap(), mem.getX(), mem.getY() + 1);
		} else {
			return new Position(mem.getMap(), mem.getX(), mem.getY());
		}
	}

	public static boolean timeToPickUpItem(int map, int x, int y) {
		int numItems = gb.readMemory(YellowAddr.wNumBagItems);
		int candyAddress = YellowAddr.wNumBagItems + (2 * numItems) + 1;
		int moonstoneAddress = candyAddress + 2;
		if ((gb.readMemory(candyAddress) != 28) && x == 34 && y == 31 && map == 59) {
			return true;
		}
		if ((gb.readMemory(moonstoneAddress) != 0x0A) && x == 2 && y == 3 && map == 59) {
			return true;
		}
		return false;
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
				wrap.injectYInput(input[i]);
				for (int j = 0; j < advanceFrames[i]; j++) {
					wrap.advanceFrame();
				}
			}
		}
	}

	static class IntroSequence extends ArrayList<Strat> implements Comparable<IntroSequence> {
		private static final long serialVersionUID = -7505108790448829235L;

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

		void executeTillIGT(GBWrapper wrap) {
			for (Strat s : this) {
				s.execute(wrap);
				if (s == title0) {
					break;
				}
			}
		}

		void executePostIGT(GBWrapper wrap) {
			boolean skip = false;
			for (Strat s : this) {
				if (s != cont) {
					if (!skip) {
						continue;
					}
				} else {
					skip = true;
				}
				s.execute(wrap);
			}
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

	private static Strat cont = new Strat("", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { A }, new Integer[] { 1 });

	private static Strat pikachu0 = new Strat("_pikachu0", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { A }, new Integer[] { 1 });

	private static Strat backout = new Strat("_backout", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { B }, new Integer[] { 1 });
	private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { START }, new Integer[] { 1 });
	private static Strat gfWait = new Strat("_gfwait", 0, new Integer[] { YellowAddr.delayAtEndOfShootingStarAddr }, new Integer[] { NO_INPUT }, new Integer[] { 0 });
	private static Strat title0 = new Strat("", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { START }, new Integer[] { 1 });
}
