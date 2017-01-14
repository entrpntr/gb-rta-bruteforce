package dabomstew.rta.ffef;

public class OverworldEdge implements Comparable<Object> {
    private OverworldAction action;
    private int cost;
    private int frames;
    private OverworldTile nextPos;

    public OverworldEdge(OverworldAction action, int cost, int frames, OverworldTile nextPos) {
        this.action = action;
        this.cost = cost;
        this.frames = frames;
        this.nextPos = nextPos;
    }

    public OverworldAction getAction() {
        return action;
    }

    public int getCost() {
        return cost;
    }

    public OverworldTile getNextPos() {
        return nextPos;
    }

    public int getFrames() {
        return frames;
    }

    @Override public int compareTo(Object other) {
        OverworldEdge o = (OverworldEdge) other;
        return cost - o.getCost();
    }
}
