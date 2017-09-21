package dabomstew.rta.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dabomstew.rta.astar.Location;

public class MapDestination {

	public static final int CUSTOM = -1;
	public static final int GRASS_PATCHES = 0;
	public static final int NORTH_CONNECTION = 1;
	public static final int EAST_CONNECTION = 2;
	public static final int SOUTH_CONNECTION = 3;
	public static final int WEST_CONNECTION = 4;

	private Map map;
	private int mode;
	private List<Location> destinationTiles;

	public MapDestination(Map map) {
		this(map, GRASS_PATCHES);
	}

	public MapDestination(Map map, int mode) {
		this.map = map;
		this.mode = mode;
		this.destinationTiles = new ArrayList<Location>();
		for(int x = 0; x < map.getWidthInTiles(); x++) {
			for(int y = 0; y < map.getHeightInTiles(); y++) {
				Tile tile = map.getTile(x, y);
				switch(mode) {
				case GRASS_PATCHES:
					if(tile.isGrassTile()) {
						destinationTiles.add(new Location(x, y));
					}
					break;
				case NORTH_CONNECTION:
					if(y == 0 && !tile.isSolid()) {
						destinationTiles.add(new Location(x, y));
					}
					break;
				case EAST_CONNECTION:
					if(x >= map.getWidthInTiles() && !tile.isSolid()) {
						destinationTiles.add(new Location(x, y));
					}
					break;
				case SOUTH_CONNECTION:
					if(y >= map.getHeightInTiles() && !tile.isSolid()) {
						destinationTiles.add(new Location(x, y));
					}
					break;
				case WEST_CONNECTION:
					if(x == 0 && !tile.isSolid()) {
						destinationTiles.add(new Location(x, y));
					}
					break;
				};
			}
		}
	}
	
	public MapDestination(Map map, Location... destionationTiles) {
		this.map = map;
		this.mode = CUSTOM;
		this.destinationTiles = Arrays.asList(destionationTiles);
	}

	public Map getMap() {
		return map;
	}

	public List<Location> getDestinationTiles() {
		return destinationTiles;
	}
	
	public int getMode() {
		return mode;
	}
}