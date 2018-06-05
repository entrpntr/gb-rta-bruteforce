package dabomstew.rta.entei;

public class GscEdge {
    private GscAction action;
    private GscTile nextTile;
    public GscEdge(GscAction action, GscTile nextTile) {
        this.action = action;
        this.nextTile = nextTile;
    }

    public GscAction getAction() {
        return action;
    }

    public GscTile getNextTile() {
        return nextTile;
    }
}
