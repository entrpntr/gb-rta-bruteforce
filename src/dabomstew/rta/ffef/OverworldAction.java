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

    private String str;
}
