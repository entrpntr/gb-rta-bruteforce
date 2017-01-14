package dabomstew.rta.ffef;

public class OverworldState {
    private String str;
    private OverworldTile pos;
    private int aPress;
    private boolean startPress;
    private int rdiv;
    private int hra;
    private int hrs;
    private boolean viridianNpc;
    private int turnframe;
    private String npcTimers;
    private int wastedFrames;
    private int overworldFrames;

    public OverworldState(String str, OverworldTile pos, int aPress, boolean startPress, int rDiv, int hra, int hrs,
                          boolean viridianNpc, int turnframe, String npcTimers, int wastedFrames, int overworldFrames) {
        this.str = str;
        this.pos = pos;
        this.aPress = aPress;
        this.startPress = startPress;
        this.rdiv = rDiv;
        this.hra = hra;
        this.hrs = hrs;
        this.viridianNpc = viridianNpc;
        this.turnframe = turnframe;
        this.npcTimers = npcTimers;
        this.wastedFrames = wastedFrames;
        this.overworldFrames = overworldFrames;
    }

    public int getTurnframeStatus() {
        return turnframe;
    }

    public String getNpcTimers() {
        return npcTimers;
    }

    public int getOverworldFrames() {
        return overworldFrames;
    }

    public int getWastedFrames() {
        return wastedFrames;
    }

    public int getMap() {
        return pos.getMap();
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int aPressCounter() {
        return aPress;
    }

    public boolean canPressStart() {
        return startPress;
    }

    public int getRdiv() {
        return rdiv;
    }

    public int getHra() {
        return hra;
    }

    public int getHrs() {
        return hrs;
    }

    public int getDsum() {
        return ((hrs + hra) % 256);
    }

    public boolean isViridianNpc() {
        return viridianNpc;
    }

    public OverworldTile getPos() {
        return pos;
    }

    @Override public String toString() {
        return str;
    }

    @Override public boolean equals(Object other) {
        OverworldState o = (OverworldState) other;
        return this.getMap() == o.getMap() && this.getX() == o.getX() && this.getY() == o.getY()
                && this.rdiv == o.getRdiv() && this.hra == o.getHra() && this.hrs == o.getHrs();
    }

    @Override public int hashCode() {
        return this.getMap() + 2*this.getX() + 3*this.getY() + 11*rdiv + 13*hra + 17*hrs;
    }

    public String getUniqId() {
        return
                "" + pos.getMap() + "#" + pos.getX() + "," + pos.getY() + "-" + turnframe + npcTimers + "-" +
                rdiv + "-" + hra + "-" + hrs;
    }
}
