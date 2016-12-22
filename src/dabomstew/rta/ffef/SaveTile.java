package dabomstew.rta.ffef;

public class SaveTile {
    private OverworldTile owPos;
    private int startCost;
    private boolean viridianNpc;

    public SaveTile(OverworldTile owPos, int startCost, boolean viridianNpc) {
        this.owPos = owPos;
        this.startCost = startCost;
        this.viridianNpc = viridianNpc;
    }

    public OverworldTile getOwPos() {
        return owPos;
    }

    public int getStartCost() {
        return startCost;
    }

    public boolean isViridianNpc() {
        return viridianNpc;
    }
}
