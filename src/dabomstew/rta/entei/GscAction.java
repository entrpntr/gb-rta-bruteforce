package dabomstew.rta.entei;

public enum GscAction {
    RIGHT("R"), LEFT("L"), UP("U"), DOWN("D"), A("A"), START_B("S_B"), S_A_B_S("S_A_B_S"), SEL("SEL"),
    ARIGHT("A+R"), ALEFT("A+L"), AUP("A+U"), ADOWN("A+D");

    GscAction(String str) {
        this.str = str;
    }

    public String logStr() {
        return str;
    }

    public static boolean isDpad(GscAction action) {
        return (action == RIGHT  || action == LEFT  || action == UP  || action == DOWN ||
                action == ARIGHT || action == ALEFT || action == AUP || action == ADOWN);
    }

    public static GscAction fromString(String str) {
        if(str.equals("R")) {
            return RIGHT;
        } else if(str.equals("L")) {
            return LEFT;
        } else if(str.equals("U")) {
            return UP;
        } else if(str.equals("D")) {
            return DOWN;
        } else if(str.equals("A+L")) {
            return ALEFT;
        } else if(str.equals("A+U")) {
            return AUP;
        } else if(str.equals("A+D")) {
            return ADOWN;
        } else if(str.equals("A+R")) {
            return ARIGHT;
        } else if(str.equals("A")) {
            return A;
        } else if(str.equals("S_B")) {
            return START_B;
        } else if(str.equals("S_A_B_S")) {
            return S_A_B_S;
        } else if(str.equals("SEL")) {
            return SEL;
        } else {
            throw new IllegalArgumentException();
        }
    }
    private String str;
}
