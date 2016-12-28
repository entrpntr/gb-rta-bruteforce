package dabomstew.rta;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class RedBlueMap {

	private static RedBlueMap[] maps = new RedBlueMap[256];

	// Map ID/Width was parsed from dabomstew's 151 underflow items pastebin
	// Collision data was parsed from gifvex collision files
	// TODO: Cave maps and perhaps city buildings
	public static RedBlueMap PALLET_TOWN = new RedBlueMap(0, 10, 50, 234, "./maps/PALLET_TOWN.bin");
	public static RedBlueMap VIRIDIAN_CITY = new RedBlueMap(1, 20, 40, 162, "./maps/VIRIDIAN_CITY.bin");
	public static RedBlueMap PEWTER_CITY = new RedBlueMap(2, 20, 40, 54, "./maps/PEWTER_CITY.bin");
	public static RedBlueMap CERULEAN_CITY = new RedBlueMap(3, 20, 220, 36, "./maps/CERULEAN_CITY.bin");
	public static RedBlueMap LAVENDER_TOWN = new RedBlueMap(4, 10, 320, 116, "./maps/LAVENDER_TOWN.bin");
	public static RedBlueMap VERMILION_CITY = new RedBlueMap(5, 20, 220, 180, "./maps/VERMILION_CITY.bin");
	public static RedBlueMap CELADON_CITY = new RedBlueMap(6, 25, 150, 108, "./maps/CELADON_CITY.bin");
	public static RedBlueMap FUCHSIA_CITY = new RedBlueMap(7, 20, 160, 270, "./maps/FUCHSIA_CITY.bin");
	public static RedBlueMap CINNABAR_ISLAND = new RedBlueMap(8, 10, 50, 342, "./maps/CINNABAR_ISLAND.bin");
	// Not too sure if the pokeworld coords are correct, this location doesn't
	// really get used anyways
	public static RedBlueMap INDIGO_PLATEAU = new RedBlueMap(9, 10, 0, 8, "./maps/INDIGO_PLATEAU.bin");
	public static RedBlueMap SAFFRON_CITY = new RedBlueMap(10, 20, 220, 108, "./maps/SAFFRON_CITY.bin");
	public static RedBlueMap ROUTE_1 = new RedBlueMap(12, 10, 50, 198, "./maps/ROUTE_1.bin");
	public static RedBlueMap ROUTE_2 = new RedBlueMap(13, 10, 50, 90, "./maps/ROUTE_2.bin");
	public static RedBlueMap ROUTE_3 = new RedBlueMap(14, 35, 80, 62, "./maps/ROUTE_3.bin");
	public static RedBlueMap ROUTE_4 = new RedBlueMap(15, 45, 130, 44, "./maps/ROUTE_4.bin");
	public static RedBlueMap ROUTE_5 = new RedBlueMap(16, 10, 230, 72, "./maps/ROUTE_5.bin");
	public static RedBlueMap ROUTE_6 = new RedBlueMap(17, 10, 230, 144, "./maps/ROUTE_6.bin");
	public static RedBlueMap ROUTE_7 = new RedBlueMap(18, 10, 200, 116, "./maps/ROUTE_7.bin");
	public static RedBlueMap ROUTE_8 = new RedBlueMap(19, 30, 260, 116, "./maps/ROUTE_8.bin");
	public static RedBlueMap ROUTE_9 = new RedBlueMap(20, 30, 260, 44, "./maps/ROUTE_9.bin");
	public static RedBlueMap ROUTE_10 = new RedBlueMap(21, 10, 320, 44, "./maps/ROUTE_10.bin");
	public static RedBlueMap ROUTE_11 = new RedBlueMap(22, 30, 260, 188, "./maps/ROUTE_11.bin");
	public static RedBlueMap ROUTE_12 = new RedBlueMap(23, 10, 320, 134, "./maps/ROUTE_12.bin");
	public static RedBlueMap ROUTE_13 = new RedBlueMap(24, 30, 280, 242, "./maps/ROUTE_13.bin");
	public static RedBlueMap ROUTE_14 = new RedBlueMap(25, 10, 260, 242, "./maps/ROUTE_14.bin");
	public static RedBlueMap ROUTE_15 = new RedBlueMap(26, 30, 200, 278, "./maps/ROUTE_15.bin");
	public static RedBlueMap ROUTE_16 = new RedBlueMap(27, 20, 110, 116, "./maps/ROUTE_16.bin");
	public static RedBlueMap ROUTE_17 = new RedBlueMap(28, 10, 110, 134, "./maps/ROUTE_17.bin");
	public static RedBlueMap ROUTE_18 = new RedBlueMap(29, 25, 110, 278, "./maps/ROUTE_18.bin");
	public static RedBlueMap ROUTE_19 = new RedBlueMap(30, 10, 170, 306, "./maps/ROUTE_19.bin");
	public static RedBlueMap ROUTE_20 = new RedBlueMap(31, 50, 70, 342, "./maps/ROUTE_20.bin");
	public static RedBlueMap ROUTE_21 = new RedBlueMap(32, 10, 50, 252, "./maps/ROUTE_21.bin");
	public static RedBlueMap ROUTE_22 = new RedBlueMap(33, 20, 0, 170, "./maps/ROUTE_22.bin");
	public static RedBlueMap ROUTE_23 = new RedBlueMap(34, 10, 0, 26, "./maps/ROUTE_23.bin");
	public static RedBlueMap ROUTE_24 = new RedBlueMap(35, 10, 230, 0, "./maps/ROUTE_24.bin");
	public static RedBlueMap ROUTE_25 = new RedBlueMap(36, 30, 250, 0, "./maps/ROUTE_25.bin");
	// I don't really know how the pokeworld coords would look like for indoor
	// maps
	public static RedBlueMap SAFARI_ZONE_EAST = new RedBlueMap(217, 15, 0, 0, "./maps/SAFARI_ZONE_EAST.bin");
	public static RedBlueMap SAFARI_ZONE_NORTH = new RedBlueMap(218, 20, 0, 0, "./maps/SAFARI_ZONE_NORTH.bin");
	public static RedBlueMap SAFARI_ZONE_WEST = new RedBlueMap(219, 15, 0, 0, "./maps/SAFARI_ZONE_WEST.bin");
	public static RedBlueMap SAFARI_ZONE_CENTER = new RedBlueMap(220, 15, 0, 0, "./maps/SAFARI_ZONE_CENTER.bin");

	/**
	 * Gets the map by a given id
	 * 
	 * @param id
	 *            - input map id
	 * @return - output map
	 */
	public static RedBlueMap getMapByID(int id) {
		return maps[id];
	}

	/**
	 * Gets the map by given pokeworld coords
	 * 
	 * @param x
	 *            - pokeworld coord x
	 * @param y
	 *            - pokeworld coord y
	 * @return - output map or null if coords are out of bounds
	 */
	public static RedBlueMap getMapByPosition(int x, int y) {
		for(RedBlueMap map : maps) {
			if(map == null) {
				continue;
			}
			int xMin = map.getPokeworldOffsetX();
			int yMin = map.getPokeworldOffsetY();
			int xMax = xMin + map.getActualWidth();
			int yMax = yMin + map.getActualHeight();
			if(x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
				return map;
			}
		}
		return null;
	}

	private int id;
	private int widthInBlocks;
	private int pokeworldOffsetX;
	private int pokeworldOffsetY;
	private int widthInTiles;
	private int heightInTiles;
	private byte[][] tiles;

	/**
	 * @param id
	 *            - the map id
	 * @param width
	 *            - the map width in blocks
	 * @param path
	 *            - the path to a binary representation of the map used to
	 *            determined solid tiles and npc positions
	 */
	private RedBlueMap(int id, int width, int pokeworldOffsetX, int pokeworldOffsetY, String path) {
		try {
			this.id = id;
			this.widthInBlocks = width;
			this.pokeworldOffsetX = pokeworldOffsetX;
			this.pokeworldOffsetY = pokeworldOffsetY;
			this.widthInTiles = width * 2;
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(path));
			byte[] tileData = new byte[inputStream.available()];
			inputStream.read(tileData);
			inputStream.close();
			heightInTiles = tileData.length / widthInTiles;
			tiles = new byte[widthInTiles][heightInTiles];
			for(int i = 0; i < tileData.length; i++) {
				tiles[i % widthInTiles][i / widthInTiles] = tileData[i];
			}
			maps[id] = this;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determainds if a tile is solid. Still returns false if tile is occupied
	 * by npc
	 * 
	 * @param x
	 *            - x coordinate of the tile
	 * @param y
	 *            - y coordinate of the tile
	 * @return - wheather the tile is solid or not
	 */
	public boolean isTileSolid(int x, int y) {
		if(x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) {
			return false;
		}
		return tiles[x][y] == 1;
	}

	/**
	 * Determainds if a npc is occupying a given tile.
	 * 
	 * @param x
	 *            - x coordinate of the tile
	 * @param y
	 *            - y coordinate of the tile
	 * @return - wheather an npc is on the tile
	 */
	public boolean isNPC(int x, int y) {
		if(x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) {
			return false;
		}
		return tiles[x][y] == 2 || tiles[x][y] == 6;
	}
	
	/**
	 * Determainds if a tile is a grass tile
	 * 
	 * @param x
	 *            - x coordinate of the tile
	 * @param y
	 *            - y coordinate of the tile
	 * @return - wheather it is a grass tile
	 */
	public boolean isGrassTile(int x, int y) {
		if(x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) {
			return false;
		}
		return tiles[x][y] == 4 || tiles[x][y] == 6;
	}

	/**
	 * Gets the excluded tiles from manip searching (in map space, not pokeworld space)
	 * @return
	 * 	- The excluded tiles
	 */
	public Position[] getExcludedTiles() {
		ArrayList<Position> result = new ArrayList<Position>();
		for(int y = 0; y < heightInTiles; y++) {
			for(int x = 0; x < widthInTiles; x++) {
				switch(tiles[x][y]) {
				case 1:
					result.add(new Position(id, x, y));
					break;
				case 2:
					for(int xa = -5; xa < 4; xa++) {
						for(int ya = -4; ya < 4; ya++) {
							int xTile = xa + x;
							int yTile = ya + y;
							RedBlueMap map = getMapByPosition(xTile + pokeworldOffsetX, yTile + pokeworldOffsetY);
							if(map == null) {
								// out of bounds
								continue;
							}
							if(xTile < 0) {
								xTile = map.getActualWidth() + xTile;
							}
							if(yTile < 0) {
								yTile = map.getActualHeight() + yTile;
							}
							if(xTile >= widthInTiles) {
								xTile = widthInTiles - xTile;
							}
							if(yTile >= widthInTiles) {
								yTile = widthInTiles - yTile;
							}
							result.add(new Position(map.getId(), xTile, yTile));
						}
					}
					break;
				}
				;
			}
		}
		Position[] res = new Position[result.size()];
		result.toArray(res);
		return res;
	}

	/**
	 * Gets the included tiles for manip searching (in map space, not pokeworld space)
	 * @return
	 * 	- The included tiles
	 */
	public Position[] getIncludedTiles() {
		ArrayList<Position> result = new ArrayList<Position>();
		ArrayList<Position> excludedTiles = new ArrayList<Position>(Arrays.asList(getExcludedTiles()));
		for(int y = 0; y < heightInTiles; y++) {
			for(int x = 0; x < widthInTiles; x++) {
				Position pos = new Position(id, x, y);
				if(!excludedTiles.contains(pos)) {
					result.add(pos);
				}
			}
		}
		Position[] res = new Position[result.size()];
		result.toArray(res);
		return res;
	}

	/**
	 * Calculates the tlPointer of a given position
	 * 
	 * @param x
	 *            - x coordinate of the position
	 * @param y
	 *            - y coordinate of the position
	 * @return - the tlpointer of the position
	 */
	public int getTLPointer(int x, int y) {
		return 0xC6E8 + (y / 2 + 1) * (widthInBlocks + 6) + (x / 2 + 1);
	}

	/**
	 * @return the map id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the map width in blocks
	 */
	public int getWidthInBlocks() {
		return widthInBlocks;
	}

	/**
	 * @return the map width in tiles
	 */
	public int getActualWidth() {
		return widthInTiles;
	}

	/**
	 * @return the map height in tiles
	 */
	public int getActualHeight() {
		return heightInTiles;
	}

	/**
	 * @return the offset to translate x coordinates to pokeworld x coordinates
	 */
	public int getPokeworldOffsetX() {
		return pokeworldOffsetX;
	}

	/**
	 * @return the offset to translate y coordinates to pokeworld y coordinates
	 */
	public int getPokeworldOffsetY() {
		return pokeworldOffsetY;
	}
}