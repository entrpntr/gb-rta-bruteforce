package dabomstew.nidorta;

public class PermissibleActionsHandler {

	public static final int A = 0x01;
	public static final int B = 0x02;
	public static final int SELECT = 0x04;
	public static final int START = 0x08;

	public static final int RIGHT = 0x10;
	public static final int LEFT = 0x20;
	public static final int UP = 0x40;
	public static final int DOWN = 0x80;
	
	private static int[][][] grassCalcViridian, grassCalcRoute22;
	
	static {
		grassCalcViridian = new int[36][40][];
		grassCalcRoute22 = new int[18][40][];
		for(int y=0;y<36;y++) {
			for(int x=0;x<40;x++) {
				grassCalcViridian[y][x] = actionsGoingToGrassCalcInt(1, x, y);
			}
		}
		
		for(int y=0;y<18;y++) {
			for(int x=0;x<40;x++) {
				grassCalcRoute22[y][x] = actionsGoingToGrassCalcInt(33, x, y);
			}
		}
	}
	
	public static int[] actionsGoingToGrass(int map, int x, int y) {
		if (map == 1) {
			return grassCalcViridian[y][x];
		}
		else {
			return grassCalcRoute22[y][x];
		}
	}

	private static int[] actionsGoingToGrassCalcInt(int map, int x, int y) {
		if (map == 1) {
			// Viridian City
			if (x <= 5 && y < 17) {
				return new int[] { LEFT, DOWN };
			} else if(x > 31 && y >= 16 && y <= 19) {
				return new int[] { DOWN };
			} else if(x >= 28 && x<=31 && y == 15) {
				return new int[] { LEFT };
			}
			else if (x == 6 && y < 14) {
				return new int[] { DOWN };
			} else if (x == 6 && y < 17) {
				return new int[] { LEFT, DOWN };
			}  else if(y < 16) {
				if(x != 17 && x != 24) {
					return new int[] { LEFT, DOWN };
				}
				else {
					return new int[] { DOWN };
				}
			}
			else if (y == 16) {
				if (x == 7) {
					return new int[] { LEFT, DOWN };
				} else if (x == 16) {
					return new int[] { DOWN };
				} else if (x == 17) {
					return new int[] { LEFT };
				} else if (x >= 20 && x <= 23) {
					return new int[] { LEFT };
				} else {
					return new int[] { DOWN, LEFT };
				}
			} else if (y == 17) {
				if (x <= 16) {
					return new int[] { LEFT };
				} else if (x == 18 || x == 24) {
					return new int[] { DOWN };
				} else {
					return new int[] { LEFT, DOWN };
				}
			} else if (x == 4) {
				return new int[] { UP };
			} else if (y == 18 && x >= 17) {
				return new int[] { LEFT };
			} else if (x >= 28) {
				return new int[] { LEFT };
			} else if(x == 8 && y == 21) {
				return new int[] { UP };
			}
			else {
				return new int[] { LEFT, UP };
			}
		} else {
			// Route 22
			if (x < 34) {
				return new int[0];
			} else if (y == 12) {
				return new int[] { LEFT };
			} else if (x == 35) {
				return new int[] { DOWN };
			} else if (x >= 38 && y == 9) {
				return new int[] { LEFT };
			} else if(x == 36 && y == 6) {
				return new int[] { DOWN };
			}
			else {
				return new int[] { LEFT, DOWN };
			}
		}
	}

	public static int[] actionsInGrassArea(int x, int y, int lastHoriz) {
		if (y == 12) {
			// below grass
			if (x == 30) {
				return new int[] { RIGHT, UP };
			} else if (x == 33) {
				return new int[] { LEFT, UP };
			} else {
				return new int[] { lastHoriz, UP };
			}
		} else if (y == 8) {
			// top row grass
			if (x == 30) {
				return new int[] { RIGHT, DOWN };
			} else if (x == 33) {
				return new int[] { LEFT, DOWN };
			} else {
				return new int[] { lastHoriz, DOWN };
			}
		} else {
			// somewhere in the middle
			if (x == 30) {
				return new int[] { RIGHT, DOWN, UP };
			} else if (x == 33) {
				return new int[] { LEFT, DOWN, UP };
			} else {
				return new int[] { lastHoriz, DOWN, UP };
			}
		}
	}

}
