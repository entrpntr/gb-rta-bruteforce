package dabomstew.rta.ffef;

import java.util.ArrayList;
import java.util.List;

import dabomstew.rta.astar.Node;

public class OverworldTile {
    private int map;
    private int x;
    private int y;
    private List<Node> path;
    private List<OverworldEdge> edgeList;
    private int minStepsToGrass;
    private boolean encounterTile;

    public OverworldTile(int map, int x, int y) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.edgeList = new ArrayList<>();
        this.encounterTile = false;
    }

    public int getMinStepsToGrass() {
        return minStepsToGrass;
    }

    public void setMinStepsToGrass(int minStepsToGrass) {
        this.minStepsToGrass = minStepsToGrass;
    }

    public boolean isEncounterTile() {
        return encounterTile;
    }

    public OverworldTile(int map, int x, int y, boolean encounterTile) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.edgeList = new ArrayList<>();
        this.encounterTile = encounterTile;
    }

    @Override public String toString() {
        return "[" + map + "#" + x + "," + y + "]";
    }

    public void addEdge(OverworldEdge edge) {
        edgeList.add(edge);
    }

    public int getMap() {
        return map;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<OverworldEdge> getEdgeList() {
        return edgeList;
    }

	public List<Node> getPath() {
		return path;
	}

	public void setPath(List<Node> path) {
		this.path = path;
	}
}
