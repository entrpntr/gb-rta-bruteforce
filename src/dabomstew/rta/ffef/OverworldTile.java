package dabomstew.rta.ffef;

import java.util.ArrayList;
import java.util.List;

public class OverworldTile {
    private int map;
    private int x;
    private int y;
    private List<OverworldEdge> edgeList;
    private int minStepsToGrass;
    private boolean encounterTile;
    private OverworldTile closestGrassTile;

    public OverworldTile(OverworldTile other) {
        this.map = other.getMap();
        this.x = other.getX();
        this.y = other.getY();
        this.edgeList = new ArrayList<>();
        this.encounterTile = other.isEncounterTile();
        this.minStepsToGrass = other.getMinStepsToGrass();
    }
    
    public OverworldTile(int map, int x, int y) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.edgeList = new ArrayList<>();
        this.encounterTile = false;
    }
    
    public OverworldTile(int map, int x, int y, OverworldTile closestGrassTile) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.edgeList = new ArrayList<>();
        this.encounterTile = false;
        this.closestGrassTile = closestGrassTile;
    }

    public void setClosestGrassTile(OverworldTile closestGrassTile) {
		this.closestGrassTile = closestGrassTile;
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
    
    public OverworldTile getClosestGrassTile() {
		return closestGrassTile;
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
    
    public OverworldEdge getEdge(OverworldAction action) {
    	for(OverworldEdge edge : edgeList) {
    		if(edge.getAction() == action) {
    			return edge;
    		}
    	}
    	return null;
    }
}
