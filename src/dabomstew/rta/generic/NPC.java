package dabomstew.rta.generic;

import java.util.ArrayList;

public class NPC {

	public static final int NPC_VISION_START_X = -5;
	public static final int NPC_VISION_START_Y = -4;
	public static final int NPC_VISION_END_X = 4;
	public static final int NPC_VISION_END_Y = 4;
	
	private Map map;
	private int x;
	private int y;
	
	public NPC(Map map, int x, int y) {
		this.map = map;
		this.x = x;
		this.y = y;
	}

	public ArrayList<Tile> getTilesAround() {
		ArrayList<Tile> result = new ArrayList<Tile>();
		for(int i = NPC_VISION_START_X; i < NPC_VISION_END_X; i++) {
			for(int j = NPC_VISION_START_Y; j < NPC_VISION_END_Y; j++) {
				int xTile = x + i;
				int yTile = y + j;
				int pwXTile = xTile + map.getPokeworldOffsetX();
				int pwYTile = yTile + map.getPokeworldOffsetY();
				Map tileMap = Map.getMapByPosition(pwXTile, pwYTile);
				if(tileMap == null) {
					continue;
				}
				if(xTile < 0) {
					// on map to the left
					xTile += tileMap.getWidthInTiles();
					yTile -= tileMap.getHeightInTiles() - map.getHeightInTiles();
				}
				if(yTile < 0) {
					// on map above
					xTile -= tileMap.getWidthInTiles() - map.getWidthInTiles();
					yTile += tileMap.getHeightInTiles();
				}
				if(xTile >= map.getWidthInTiles()) {
					// on map to the right
					xTile -= map.getWidthInTiles();
					yTile -= tileMap.getHeightInTiles() - map.getHeightInTiles();
				}
				if(yTile >= map.getHeightInTiles()) {
					// on map below
					xTile -= tileMap.getWidthInTiles() - map.getWidthInTiles();
					yTile -= map.getHeightInTiles();
				}
				result.add(tileMap.getTile(xTile, yTile));
			}
		}
		return result;
	}
	
	public Map getMap() {
		return map;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}