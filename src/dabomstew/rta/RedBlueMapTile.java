package dabomstew.rta;

public class RedBlueMapTile {

	// TODO: maybe make positions bytes for less ram usage?
	private RedBlueMap map;
	private int x;
	private int y;
	private boolean isSolid;
	private boolean isOccupiedByNPC;
	private boolean isGrassTile;

	public RedBlueMapTile(RedBlueMap map, int x, int y, byte data) {
		this.map = map;
		this.x = x;
		this.y = y;
		this.isSolid = (data & 1) != 0;
		this.isOccupiedByNPC = (data & 2) != 0;
		this.isGrassTile = (data & 4) != 0;
	}

	/**
	 * @return - The map of the tile
	 */
	public RedBlueMap getMap() {
		return map;
	}

	/**
	 * @return - The x position of the tile ranging from 0 to MapWidthInTiles
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return - The y position of the tile ranging from 0 to MapHeightInTiles
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return - Whether the tile is solid or not
	 */
	public boolean isSolid() {
		return isSolid;
	}

	/**
	 * @return - Whether a NPC stands on the tile
	 */
	public boolean isOccupiedByNPC() {
		return isOccupiedByNPC;
	}

	/**
	 * @return - Whether the tile is a grass tile or not
	 */
	public boolean isGrassTile() {
		return isGrassTile;
	}
}