package dabomstew.rta;

public class Func {
    
    public static final int A = 0x01;
    public static final int B = 0x02;
    public static final int SELECT = 0x04;
    public static final int START = 0x08;

    public static final int RIGHT = 0x10;
    public static final int LEFT = 0x20;
    public static final int UP = 0x40;
    public static final int DOWN = 0x80;

    public static final int RESET = 0x800;
    
    public static String inputName(int input) {
        switch (input) {
        case A:
            return "A";
        case B:
            return "B";
        case SELECT:
            return "s";
        case START:
            return "S";
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
    
    public static int aCount(String path, int plen) {
        int ctr = 0;
        for (int i = 0; i < plen; i++) {
            if (path.charAt(i) == 'A') {
                ctr++;
            }
        }
        return ctr;
    }

    public static int inputsUntilNextA(String path, int maxAPresses) {
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

}
