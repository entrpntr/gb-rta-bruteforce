package dabomstew.rta.entei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GscTile {
    public int getMap() {
        return map;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override public String toString() {
        return "[" + map + "#" + x + "," + y + "]";
    }

    public HashMap<Integer, List<GscEdge>> getEdges() {
        return edges;
    }

    public void addEdge(int edgeset, GscEdge edge) {
        if(!edges.containsKey(edgeset)) {
            edges.put(edgeset, new ArrayList<>());
        }
        edges.get(edgeset).add(edge);
    }

    private GscCoord coord;
    private int map;
    private int x;
    private int y;
    private HashMap<Integer, List<GscEdge>> edges;

    public GscTile(int map, int x, int y) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.coord = GscCoord.at(map, x, y);
        this.edges = new HashMap<>();
    }

    public GscTile(GscCoord coord) {
        this.coord = coord;
        this.x = coord.getX();
        this.y = coord.getY();
        this.map = coord.getMap();
        this.edges = new HashMap<>();
    }
}
