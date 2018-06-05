package dabomstew.rta.entei;

import java.util.HashMap;

public class GscTileMap {
    private HashMap<GscCoord, GscTile> map = new HashMap<>();
    public GscTileMap() {

    }
    public void put(GscCoord c, GscTile tile) {
        map.put(c, tile);
    }
    public GscTile get(GscCoord c) {
        return map.get(c);
    }
    public GscTile get(int m, int x, int y) {
        return map.get(GscCoord.at(m, x, y));
    }
}
