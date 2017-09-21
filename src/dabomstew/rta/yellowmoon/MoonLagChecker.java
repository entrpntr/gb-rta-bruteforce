package dabomstew.rta.yellowmoon;

import java.io.File;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import dabomstew.rta.GBMemory;
import dabomstew.rta.GBMemoryYellow;
import dabomstew.rta.GBWrapper;
import dabomstew.rta.Position;
import dabomstew.rta.YellowAddr;
import dabomstew.rta.ffef.OverworldAction;
import mrwint.gbtasgen.Gb;

public class MoonLagChecker {

	public static final int NO_INPUT = 0x00;

	public static final int A = 0x01;
	public static final int B = 0x02;
	public static final int SELECT = 0x04;
	public static final int START = 0x08;

	public static final int RIGHT = 0x10;
	public static final int LEFT = 0x20;
	public static final int UP = 0x40;
	public static final int DOWN = 0x80;

	private static final int HARD_RESET = 0x800;

	private static boolean[] itemsPickedUp;
	private static int itemIdx;

	private static Gb gb;
	private static GBWrapper wrap;
	private static GBMemory mem0;
	private static GBMemoryYellow mem;

	private static PrintWriter outputlog;

	public static void main(String[] args) throws Exception {
		outputlog = new PrintWriter("./yellow_moon_igt0.log");
		Gb.loadGambatte(1);
		gb = new Gb(0, false);
		gb.startEmulator("roms/pokeyellow.gbc");
		mem0 = new GBMemory(gb);
		wrap = new GBWrapper(gb, mem0);
		mem = new GBMemoryYellow(gb);
		gb.step(HARD_RESET);

		String path = "";

		path += "U U U U U U U U U U U U U S_B R R R R R R U U U R U U U U R R R D D D D D D D D D D D R D D D D D D R R R R R R R R U R ";
		path += "U U U U A U U U U U A U U U U U U U U A U U U L U L L U U U U U U U L L ";
		path += "L L L D A L L L L L D D L D L L L L D ";
		path += "D D D D D D A D D D ";
		path += "L L L L L L L L L L L U L L U L U A U U U U U U U U U U U ";
		path += "R R A D D R R R D D D D A D D D D D A D D R R R R R R R A R R R R R R R D ";
		path += "R R S_B U U U R R R R D D R R R R R U R U A R R R D D S_B D D D D D D ";
		path += "L S_B L D A D D L D D L D D D D A L L L L L L L L L L L L L L L A L L L L L L A ";
		path += "U U U S_B U U U U U U S_B U U U U R U U U U U U A U U U";
		checkLag(path);
	}

	private static void checkLag(String path) throws Exception {
		int[] actionLengths = new int[path.length()];
		for (File file : new File("./states/").listFiles()) {
			file.delete();
		}
		logF("Path: %s\n", path);
		gfSkip.execute(wrap);
		pikachu0.execute(wrap);
		title0.execute(wrap);
		wrap.advanceToAddress(YellowAddr.igtInjectAddr);
		ByteBuffer save = gb.saveState();
		String[] actions = path.split(" ");
		outer: for (int i = 1; i <= 2; i++) {
			itemsPickedUp = new boolean[3];
			itemIdx = 0;
			gb.loadState(save);
			gb.writeMemory(0xDA43, 0);
			gb.writeMemory(0xDA44, i);
			cont.execute(wrap);
			cont.execute(wrap);
			wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
			if (actions.length > 1) {
				for (int j = 0; j < actions.length; j++) {
					int startFrames = mem.getIGT();
					OverworldAction owAction = OverworldAction.fromString(actions[j]);
					if (!execute(owAction, 0, i)) {
						continue outer;
					}
					int frames = mem.getIGT() - startFrames;
					if(i == 1) {
						actionLengths[j] = frames;
					} else {
						if(actionLengths[j] != frames) {
							System.out.printf("Lag at map %d x %d y %d - igt frame 0 action length: %d, igt frame 1 action length: %d\n", mem.getMap(), mem.getX(), mem.getY(), actionLengths[j], frames);
						}
					}
				}
			}
		}
	}

	private static boolean execute(OverworldAction owAction, int second, int frame) {
		int res;
		// System.out.print(owAction.logStr());
		switch (owAction) {
		case LEFT:
		case UP:
		case RIGHT:
		case DOWN:
			int input = 16 * (int) (Math.pow(2.0, (owAction.ordinal())));
			// Execute the action
			Position dest = getDestination(mem, input);
			wrap.injectYInput(input);
			wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr + 1);
			int result = wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr, YellowAddr.newBattleAddr, YellowAddr.manualTextScrollAddr);
			if (result == YellowAddr.manualTextScrollAddr) {
				System.out.println("TEXTBOX HIT AT " + mem.getX() + " " + mem.getY());
				return false;
			}
			// Did we turnframe or hit an
			// ignored input frame after
			// a
			// warp?
			while (mem.getX() != dest.x || mem.getY() != dest.y) {
				if (result == YellowAddr.newBattleAddr) {
					// Check for garbage
					int result2 = wrap.advanceWithJoypadToAddress(input, YellowAddr.encounterTestAddr, YellowAddr.joypadOverworldAddr);

					if (result2 == YellowAddr.encounterTestAddr) {
						// Yes we can. What's up
						// on this tile?
						int hra = mem.getHRA();
						// logLN("hrandom add was "+hra);
						if (hra < 10) {
							wrap.advanceFrame();
							wrap.advanceFrame();
							wrap.advanceFrame();
							return false;
						}
					}
				}
				// Do that input again
				wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
				wrap.injectYInput(input);
				wrap.advanceWithJoypadToAddress(input, YellowAddr.joypadOverworldAddr + 1);
				result = wrap.advanceWithJoypadToAddress(input, YellowAddr.newBattleAddr, YellowAddr.joypadOverworldAddr);
			}
			// Can we get an encounter now?
			int result2 = wrap.advanceToAddress(YellowAddr.encounterTestAddr, YellowAddr.joypadOverworldAddr, YellowAddr.manualTextScrollAddr);
			if (result2 == YellowAddr.manualTextScrollAddr) {
				System.out.println("TEXTBOX HIT AT " + mem.getX() + " " + mem.getY());
				return false;
			}
			if (result2 == YellowAddr.encounterTestAddr) {
				int hra = mem.getHRA();
				if (hra < 10) {
					wrap.advanceFrame();
					wrap.advanceFrame();
					wrap.advanceFrame();
					return false;
				}
				wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
				if (timeToPickUpItem(mem.getMap(), mem.getX(), mem.getY(), itemsPickedUp)) {
					// Pick it up
					wrap.injectYInput(A);
					wrap.advanceWithJoypadToAddress(A, YellowAddr.textJingleCommandAddr);
					wrap.advanceToAddress(YellowAddr.joypadOverworldAddr);
					itemsPickedUp[itemIdx++] = true;
				}
			}
			return true;
		case A:
			wrap.injectYInput(A);
			wrap.advanceFrame(A);
			res = wrap.advanceWithJoypadToAddress(A, YellowAddr.joypadOverworldAddr, YellowAddr.printLetterDelayAddr, YellowAddr.manualTextScrollAddr);
			if (res == YellowAddr.manualTextScrollAddr) {
				System.out.println("TEXTBOX HIT AT " + mem.getX() + " " + mem.getY());
				return false;
			}
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

	public static boolean timeToPickUpItem(int map, int x, int y, boolean[] pickedUpItems) {
		if (!pickedUpItems[0] && x == 34 && y == 31 && map == 59) {
			return true;
		}
		if (!pickedUpItems[1] && x == 28 && y == 5 && map == 61) {
			return true;
		}
		if (!pickedUpItems[2] && x == 2 && y == 3 && map == 59) {
			return true;
		}
		return false;
	}

	public static void logLN(String text) {
		System.out.println(text);
		outputlog.println(text);
		outputlog.flush();
	}

	public static void logF(String format, Object... args) {
		System.out.printf(format, args);
		outputlog.printf(format, args);
		outputlog.flush();
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
			String ret = "yellow";
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

	private static Strat cont = new Strat("", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { A }, new Integer[] { 1 });

	private static Strat pikachu0 = new Strat("_pikachu0", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { A }, new Integer[] { 1 });

	private static Strat backout = new Strat("_backout", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { B }, new Integer[] { 1 });
	private static Strat gfSkip = new Strat("_gfskip", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { START }, new Integer[] { 1 });
	private static Strat gfWait = new Strat("_gfwait", 0, new Integer[] { YellowAddr.delayAtEndOfShootingStarAddr }, new Integer[] { NO_INPUT }, new Integer[] { 0 });
	private static Strat title0 = new Strat("", 0, new Integer[] { YellowAddr.joypadAddr }, new Integer[] { START }, new Integer[] { 1 });
}