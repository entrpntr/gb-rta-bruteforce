package dabomstew.rta.nidobot;

import java.io.IOException;

import dabomstew.rta.FileFunctions;

public class PermissibleActionsHandler {

    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    private static int[][] grassCalcViridian, grassCalcRoute22, grassCalcVermillion, grassCalcRoute11;

    static {
        grassCalcViridian = new int[36][40];
        grassCalcRoute22 = new int[18][40];
        grassCalcVermillion = new int[36][40];
        grassCalcRoute11 = new int[18][60];

        try {
            byte[] viridianData = FileFunctions.readFileFullyIntoBuffer("perms/viridian.bin");
            for (int y = 0; y < 36; y++) {
                for (int x = 0; x < 40; x++) {
                    grassCalcViridian[y][x] = viridianData[y * 40 + x] & 0xFF;
                }
            }
            byte[] r22Data = FileFunctions.readFileFullyIntoBuffer("perms/route22.bin");
            for (int y = 0; y < 18; y++) {
                for (int x = 0; x < 40; x++) {
                    grassCalcRoute22[y][x] = r22Data[y * 40 + x] & 0xFF;
                }
            }
            for (int y = 12; y < 16; y++) {
                for (int x = 18; x < 40; x++) {
                    if (y < 14) {
                        grassCalcVermillion[y][x] = DOWN;
                    } else if (x < 29 && y == 15) {
                        grassCalcVermillion[y][x] = UP;
                    } else {
                        grassCalcVermillion[y][x] = RIGHT;
                    }
                }
            }
            for(int y=6;y<8;y++) {
                for(int x=0;x<11;x++) {
                    grassCalcRoute11[y][x] = RIGHT;
                }
            }
            // allow move-down variance
            for(int x=30;x<40;x++) {
                grassCalcVermillion[14][x] |= DOWN;
            }
            for(int x=0;x<11;x++) {
                grassCalcRoute11[6][x] |= DOWN;
            }
        } catch (IOException e) {
        }

    }

    public static int actionsGoingToGrass(int map, int x, int y) {
        if (map == 1) {
            return grassCalcViridian[y][x];
        } else if (map == 33) {
            return grassCalcRoute22[y][x];
        } else if (map == 5) {
            return grassCalcVermillion[y][x];
        } else {
            // map = route 11 (22)
            return grassCalcRoute11[y][x];
        }
    }

    public static int[] actionsInGrassAreaNido(int x, int y, int lastHoriz) {
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
