package dabomstew.rta.ffef;

public enum OverworldAction {
    RIGHT("R"), LEFT("L"), UP("U"), DOWN("D"), A("A"), START_B("S_B"), S_A_B_S("S_A_B_S"), S_A_B_A_B_S("S_A_B_A_B_S");

    OverworldAction(String str) {
        this.str = str;
    }

    public String logStr() {
        return str;
    }

    public static boolean isDpad(OverworldAction action) {
        return (action == RIGHT || action == LEFT || action == UP || action == DOWN);
    }

    public static OverworldAction fromString(String str) {
        if(str.equals("R")) {
            return RIGHT;
        } else if(str.equals("L")) {
            return LEFT;
        } else if(str.equals("U")) {
            return UP;
        } else if(str.equals("D")) {
            return DOWN;
        } else if(str.equals("A")) {
            return A;
        } else if(str.equals("S_B")) {
            return START_B;
        } else if(str.equals("S_A_B_S")) {
            return S_A_B_S;
        } else if(str.equals("S_A_B_A_B_S")) {
            return S_A_B_A_B_S;
        } else {
            throw new IllegalArgumentException();
        }
    }
    private String str;
}
