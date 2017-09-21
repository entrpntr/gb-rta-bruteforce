package dabomstew.rta.generic;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

import dabomstew.rta.ffef.OverworldTile;

public class Map {

	private static Map[] maps = new Map[256];

	// Map ID/Width was parsed from dabomstew's 151 underflow items pastebin
	// Collision data was parsed from gifvex collision files
	// TODO: Cave maps and perhaps city buildings
	public static Map PALLET_TOWN = new Map(0, 10, 50, 234, 0, "./maps/PALLET_TOWN.bin");
	public static Map VIRIDIAN_CITY = new Map(1, 20, 40, 162, 0, "./maps/VIRIDIAN_CITY.bin");
	public static Map PEWTER_CITY = new Map(2, 20, 40, 54, 0, "./maps/PEWTER_CITY.bin");
	public static Map CERULEAN_CITY = new Map(3, 20, 220, 36, 0, "./maps/CERULEAN_CITY.bin");
	public static Map LAVENDER_TOWN = new Map(4, 10, 320, 116, 0, "./maps/LAVENDER_TOWN.bin");
	public static Map VERMILION_CITY = new Map(5, 20, 220, 180, 0, "./maps/VERMILION_CITY.bin");
	public static Map CELADON_CITY = new Map(6, 25, 150, 108, 0, "./maps/CELADON_CITY.bin");
	public static Map FUCHSIA_CITY = new Map(7, 20, 160, 270, 0, "./maps/FUCHSIA_CITY.bin");
	public static Map CINNABAR_ISLAND = new Map(8, 10, 50, 342, 0, "./maps/CINNABAR_ISLAND.bin");
	// Not too sure if the pokeworld coords are correct, this location doesn't
	// really get used anyways
	public static Map INDIGO_PLATEAU = new Map(9, 10, 0, 8, 0, "./maps/INDIGO_PLATEAU.bin");
	public static Map SAFFRON_CITY = new Map(10, 20, 220, 108, 0, "./maps/SAFFRON_CITY.bin");
	public static Map ROUTE_1 = new Map(12, 10, 50, 198, 25, "./maps/ROUTE_1.bin");
	public static Map ROUTE_2 = new Map(13, 10, 50, 90, 25, "./maps/ROUTE_2.bin");
	public static Map ROUTE_3 = new Map(14, 35, 80, 62, 20, "./maps/ROUTE_3.bin");
	public static Map ROUTE_4 = new Map(15, 45, 130, 44, 20, "./maps/ROUTE_4.bin");
	public static Map ROUTE_5 = new Map(16, 10, 230, 72, 15, "./maps/ROUTE_5.bin");
	public static Map ROUTE_6 = new Map(17, 10, 230, 144, 15, "./maps/ROUTE_6.bin");
	public static Map ROUTE_7 = new Map(18, 10, 200, 116, 15, "./maps/ROUTE_7.bin");
	public static Map ROUTE_8 = new Map(19, 30, 260, 116, 15, "./maps/ROUTE_8.bin");
	public static Map ROUTE_9 = new Map(20, 30, 260, 44, 15, "./maps/ROUTE_9.bin");
	public static Map ROUTE_10 = new Map(21, 10, 320, 44, 15, "./maps/ROUTE_10.bin");
	public static Map ROUTE_11 = new Map(22, 30, 260, 188, 15, "./maps/ROUTE_11.bin");
	public static Map ROUTE_12 = new Map(23, 10, 320, 134, 15, "./maps/ROUTE_12.bin");
	public static Map ROUTE_13 = new Map(24, 30, 280, 242, 20, "./maps/ROUTE_13.bin");
	public static Map ROUTE_14 = new Map(25, 10, 260, 242, 15, "./maps/ROUTE_14.bin");
	public static Map ROUTE_15 = new Map(26, 30, 200, 278, 15, "./maps/ROUTE_15.bin");
	public static Map ROUTE_16 = new Map(27, 20, 110, 116, 25, "./maps/ROUTE_16.bin");
	public static Map ROUTE_17 = new Map(28, 10, 110, 134, 25, "./maps/ROUTE_17.bin");
	public static Map ROUTE_18 = new Map(29, 25, 110, 278, 25, "./maps/ROUTE_18.bin");
	// Don't know if the global encounter rate for these should be 0 or 5.
	public static Map ROUTE_19 = new Map(30, 10, 170, 306, 5, "./maps/ROUTE_19.bin");
	public static Map ROUTE_20 = new Map(31, 50, 70, 342, 5, "./maps/ROUTE_20.bin");
	public static Map ROUTE_21 = new Map(32, 10, 50, 252, 25, "./maps/ROUTE_21.bin");
	public static Map ROUTE_22 = new Map(33, 20, 0, 170, 25, "./maps/ROUTE_22.bin");
	public static Map ROUTE_23 = new Map(34, 10, 0, 26, 10, "./maps/ROUTE_23.bin");
	public static Map ROUTE_24 = new Map(35, 10, 230, 0, 25, "./maps/ROUTE_24.bin");
	public static Map ROUTE_25 = new Map(36, 30, 250, 0, 15, "./maps/ROUTE_25.bin");
	// I don't really know how the pokeworld coords would look like for indoor
	// maps
	public static Map SAFARI_ZONE_EAST = new Map(217, 15, 500, 500, 30, "./maps/SAFARI_ZONE_EAST.bin");
	public static Map SAFARI_ZONE_NORTH = new Map(218, 20, 2000, 2000, 30, "./maps/SAFARI_ZONE_NORTH.bin");
	public static Map SAFARI_ZONE_WEST = new Map(219, 15, 0, 0, 30, "./maps/SAFARI_ZONE_WEST.bin");
	public static Map SAFARI_ZONE_CENTER = new Map(220, 15, 1000, 1000, 30, "./maps/SAFARI_ZONE_CENTER.bin");
	
	public static Map MT_MOON_1 = new Map(59, 20, 69000, 1000, 10, "./maps/MT_MOON_1.bin");
	public static Map MT_MOON_2 = new Map(60, 14, 69000, 2000, 10, "./maps/MT_MOON_2.bin");
	public static Map MT_MOON_3 = new Map(61, 20, 69000, 3000, 10, "./maps/MT_MOON_3.bin");
	
	public static Map VIRIDIAN_FOREST = new Map(51, 17, 69000, 5000, 25, "./maps/VIRIDIAN_FOREST.bin");
	
	public static Map DIGLETTS_CAVE = new Map(197, 20, 69800, 1000, 20, "./maps/DIGLETTS_CAVE.bin");
	
	static {
		PALLET_TOWN.setConnections(ROUTE_1, null, ROUTE_21, null);
		VIRIDIAN_CITY.setConnections(ROUTE_2, null, ROUTE_1, ROUTE_22);
		PEWTER_CITY.setConnections(null, ROUTE_3, ROUTE_2, null);
		CERULEAN_CITY.setConnections(ROUTE_24, ROUTE_9, ROUTE_5, ROUTE_4);
		LAVENDER_TOWN.setConnections(ROUTE_10, null, ROUTE_12, ROUTE_7);
		VERMILION_CITY.setConnections(ROUTE_6, ROUTE_11, null, null);
		CELADON_CITY.setConnections(null, ROUTE_8, null, ROUTE_16);
		FUCHSIA_CITY.setConnections(null, ROUTE_15, ROUTE_19, ROUTE_18);
		CINNABAR_ISLAND.setConnections(ROUTE_21, ROUTE_20, null, null);
		INDIGO_PLATEAU.setConnections(null, null, ROUTE_23, null);
		SAFFRON_CITY.setConnections(ROUTE_5, ROUTE_7, ROUTE_6, ROUTE_8);
		ROUTE_1.setConnections(VIRIDIAN_CITY, null, PALLET_TOWN, null);
		ROUTE_2.setConnections(PEWTER_CITY, null, VIRIDIAN_CITY, null);
		ROUTE_3.setConnections(null, ROUTE_4, null, PEWTER_CITY);
		ROUTE_4.setConnections(null, CERULEAN_CITY, null, ROUTE_3);
		ROUTE_5.setConnections(CERULEAN_CITY, null, SAFFRON_CITY, null);
		ROUTE_6.setConnections(SAFFRON_CITY, null, VERMILION_CITY, null);
		ROUTE_7.setConnections(null, LAVENDER_TOWN, null, SAFFRON_CITY);
		ROUTE_8.setConnections(null, SAFFRON_CITY, null, CELADON_CITY);
		ROUTE_9.setConnections(null, ROUTE_10, null, CERULEAN_CITY);
		ROUTE_10.setConnections(null, null, LAVENDER_TOWN, ROUTE_9);
		ROUTE_11.setConnections(null, ROUTE_12, null, VERMILION_CITY);
		ROUTE_12.setConnections(LAVENDER_TOWN, null, ROUTE_12, null);
		ROUTE_13.setConnections(null, ROUTE_12, ROUTE_14, null);
		ROUTE_14.setConnections(ROUTE_13, null, null, ROUTE_15);
		ROUTE_15.setConnections(null, ROUTE_14, null, FUCHSIA_CITY);
		ROUTE_16.setConnections(null, CELADON_CITY, ROUTE_17, null);
		ROUTE_17.setConnections(ROUTE_16, ROUTE_18, null, null);
		ROUTE_18.setConnections(null, FUCHSIA_CITY, null, ROUTE_17);
		ROUTE_19.setConnections(FUCHSIA_CITY, null, ROUTE_20, null);
		ROUTE_20.setConnections(null, ROUTE_19, null, CINNABAR_ISLAND);
		ROUTE_21.setConnections(PALLET_TOWN, null, CINNABAR_ISLAND, null);
		ROUTE_22.setConnections(ROUTE_23, VIRIDIAN_CITY, null, null);
		ROUTE_23.setConnections(INDIGO_PLATEAU, null, ROUTE_22, null);
		ROUTE_24.setConnections(null, ROUTE_25, CERULEAN_CITY, null);
	};

	/**
	 * Gets the map by a given id
	 * 
	 * @param id
	 *            - input map id
	 * @return - output map
	 */
	public static Map getMapByID(int id) {
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
	public static Map getMapByPosition(int x, int y) {
		for(Map map : maps) {
			if(map == null) {
				continue;
			}
			int xMin = map.getPokeworldOffsetX();
			int yMin = map.getPokeworldOffsetY();
			int xMax = xMin + map.getWidthInTiles();
			int yMax = yMin + map.getHeightInTiles();
			if(x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
				return map;
			}
		}
		return null;
	}

	private int id;
	private int pokeworldOffsetX;
	private int pokeworldOffsetY;
	private int globalEncounterRate;
	private Map northConnection;
	private Map eastConnection;
	private Map southConnection;
	private Map westConnection;
	private int widthInBlocks;
	private int heightInBlocks;
	private int widthInTiles;
	private int heightInTiles;
	private Tile[][] tiles;
	private OverworldTile[][] overworldTiles;
	private ArrayList<NPC> npcs;

	/**
	 * @param id
	 *            - the map id
	 * @param width
	 *            - the map width in blocks
	 * @param path
	 *            - the path to a binary representation of the map used to
	 *            determined solid tiles and npc positions
	 */
	private Map(int id, int width, int pokeworldOffsetX, int pokeworldOffsetY, int globalEncounterRate, String path) {
		try {
			this.id = id;
			this.widthInBlocks = width;
			this.pokeworldOffsetX = pokeworldOffsetX;
			this.pokeworldOffsetY = pokeworldOffsetY;
			this.globalEncounterRate = globalEncounterRate;
			this.widthInTiles = width * 2;
			this.npcs = new ArrayList<NPC>();
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(path));
			byte[] tileData = new byte[inputStream.available()];
			inputStream.read(tileData);
			inputStream.close();
			heightInBlocks = tileData.length / widthInBlocks;
			heightInTiles = tileData.length / widthInTiles;
			tiles = new Tile[widthInTiles][heightInTiles];
			overworldTiles = new OverworldTile[widthInTiles][heightInTiles];
			for(int i = 0; i < tileData.length; i++) {
				int x = i % widthInTiles;
				int y = i / widthInTiles;
				Tile tile = new Tile(this, x, y, tileData[i]);
				tiles[x][y] = tile;
				if(!tile.isSolid() && !tile.isOccupiedByNPC()) {
					overworldTiles[x][y] = new OverworldTile(id, x, y, tile.isGrassTile());
				}
				if(tile.isOccupiedByNPC()) {
					if(id == 51 && x == 25 && y == 11) {
						continue;
					}
					npcs.add(new NPC(this, x, y));
				}
			}
			maps[id] = this;
			// Hardcoded trainer vision shenanagains
			if(id == 60) {
				tiles[6][5].setCanMoveLeft(false);
				tiles[5][6].setCanMoveUp(false);
			}
			if(id == 61) {
				tiles[28][7].setCanMoveRight(false);
				tiles[28][8].setCanMoveRight(false);
				tiles[28][9].setCanMoveRight(false);
				tiles[28][10].setCanMoveRight(false);
				tiles[29][6].setCanMoveDown(false);
				
				tiles[25][16].setCanMoveDown(false);
				tiles[26][16].setCanMoveDown(false);
				tiles[27][16].setCanMoveDown(false);
				tiles[28][16].setCanMoveDown(false);
				
				tiles[25][18].setCanMoveUp(false);
				tiles[26][18].setCanMoveUp(false);
				tiles[27][18].setCanMoveUp(false);
				tiles[28][18].setCanMoveUp(false);
				
				tiles[28][6].setCanMoveUp(false);
//				tiles[28][5].setCanMoveDown(false);
				
			}
			if(id == 59) {
				overworldTiles[16][25] = null;
				overworldTiles[16][24] = null;
				overworldTiles[24][28] = null;
				overworldTiles[24][29] = null;
				overworldTiles[24][30] = null;
				overworldTiles[31][27] = null;
				overworldTiles[30][5] = null;
				overworldTiles[30][6] = null;
				overworldTiles[30][7] = null;
				tiles[16][25].setSolid(true);
				tiles[16][24].setSolid(true);
				tiles[24][28].setSolid(true);
				tiles[24][29].setSolid(true);
				tiles[24][30].setSolid(true);
				tiles[31][27].setSolid(true);
				tiles[30][5].setSolid(true);
				tiles[30][6].setSolid(true);
				tiles[30][7].setSolid(true);
				tiles[15][25].setCanMoveRight(false);
				tiles[15][24].setCanMoveRight(false);
				tiles[17][24].setCanMoveLeft(false);
				tiles[17][25].setCanMoveLeft(false);
				tiles[24][27].setCanMoveDown(false);
				tiles[25][28].setCanMoveLeft(false);
				tiles[25][29].setCanMoveLeft(false);
				tiles[25][30].setCanMoveLeft(false);
				tiles[23][28].setCanMoveRight(false);
				tiles[23][29].setCanMoveRight(false);
				tiles[23][30].setCanMoveRight(false);
				tiles[31][28].setCanMoveUp(false);
				tiles[31][26].setCanMoveDown(false);
				
				tiles[30][8].setCanMoveUp(false);
				tiles[31][7].setCanMoveLeft(false);
				tiles[31][6].setCanMoveLeft(false);
				tiles[31][5].setCanMoveLeft(false);
				tiles[29][5].setCanMoveRight(false);
				tiles[29][6].setCanMoveRight(false);
				tiles[29][7].setCanMoveRight(false);
				
				tiles[13][17].setCanMoveUp(false);
				tiles[14][17].setCanMoveUp(false);
				tiles[15][17].setCanMoveUp(false);
				tiles[13][15].setCanMoveDown(false);
				tiles[14][15].setCanMoveDown(false);
				tiles[15][15].setCanMoveDown(false);
				tiles[16][16].setCanMoveLeft(false);
				
				tiles[6][7].setCanMoveLeft(false);
				tiles[6][8].setCanMoveLeft(false);
				tiles[4][7].setCanMoveRight(false);
				tiles[4][8].setCanMoveRight(false);
				tiles[5][9].setCanMoveUp(false);
				
				tiles[24][15].setCanMoveRight(false);
				tiles[25][14].setCanMoveDown(false);
				tiles[26][15].setCanMoveLeft(false);
				tiles[25][16].setCanMoveUp(false);
			
				tiles[36][24].setCanMoveUp(false);
				tiles[35][23].setCanMoveRight(false);
				
				tiles[33][32].setCanMoveRight(false);
				
				tiles[3][3].setCanMoveUp(false);
				tiles[34][32].setCanMoveUp(false);
				
				tiles[6][5].setCanMoveLeft(false);
				tiles[5][4].setCanMoveDown(false);
			}
			if(id == 51) {
				npcs.add(new NPC(this, 13, 17));
				overworldTiles[14][17] = null;
				overworldTiles[15][17] = null;
				overworldTiles[16][17] = null;
				overworldTiles[17][17] = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a tile on the given coordinates
	 * 
	 * @param x
	 *            - x coordinate of the tile
	 * @param y
	 *            - y coordinate of the tile
	 * @return - The tile at that position
	 */
	public Tile getTile(int x, int y) {
		if(x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) {
			return null;
		}
		return tiles[x][y];
	}
	
	public OverworldTile getOverworldTile(int x, int y) {
		if(x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) {
			return null;
		}
		return overworldTiles[x][y];
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
	 * @return the map height in blocks
	 */
	public int getHeightInBlocks() {
		return heightInBlocks;
	}

	/**
	 * @return the map width in tiles
	 */
	public int getWidthInTiles() {
		return widthInTiles;
	}

	/**
	 * @return the map height in tiles
	 */
	public int getHeightInTiles() {
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

	/**
	 * @return the global encounter rate of the area, returns 0 if there are no encounters
	 */
	public int getGlobalEncounterRate() {
		return globalEncounterRate;
	}
	
	public boolean isTileInNPCVision(int x, int y) {
		return tiles[x][y].isInVisionOfNPC();
	}
	
	public ArrayList<NPC> getNPCs() {
		return npcs;
	}
	
	public Map getNorthConnection() {
		return northConnection;
	}

	public Map getEastConnection() {
		return eastConnection;
	}

	public Map getSouthConnection() {
		return southConnection;
	}

	public Map getWestConnection() {
		return westConnection;
	}

	private void setConnections(Map northConnection, Map eastConnection, Map southConnection, Map westConnection) {
		this.northConnection = northConnection;
		this.eastConnection = eastConnection;
		this.southConnection = northConnection;
		this.westConnection = westConnection;
	}
}