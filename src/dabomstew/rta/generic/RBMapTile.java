package dabomstew.rta.generic;

public class RBMapTile {

	// TODO: maybe make positions bytes for less ram usage?
	private RBMap map;
	private int x;
	private int y;
	private boolean isSolid;
	private boolean isOccupiedByNPC;
	private boolean isWarp;
	private boolean isGrassTile;
	private boolean canMoveRight;
	private boolean canMoveLeft;
	private boolean canMoveUp;
	private boolean canMoveDown;

	public RBMapTile(RBMap map, int x, int y, byte data) {
		this.map = map;
		this.x = x;
		this.y = y;
		this.isSolid = (data & 1) != 0;
		this.isOccupiedByNPC = (data & 2) != 0;
		this.isWarp = (data & 4) != 0;
		this.isGrassTile = (data & 8) != 0;
		this.canMoveRight = (data & 16) != 0;
		this.canMoveLeft = (data & 32) != 0;
		this.canMoveUp = (data & 64) != 0;
		this.canMoveDown = (data & 128) != 0;
	}

	/**
	 * @return - The map of the tile
	 */
	public RBMap getMap() {
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
	 * @return - Whether this tile is a warp tile or not
	 */
	public boolean isWarp() {
		return isWarp;
	}

	/**
	 * @return - Whether you can move right if you're standing in this tile or
	 *         not
	 */
	public boolean canMoveRight() {
		return canMoveRight;
	}

	/**
	 * @return - Whether you can move left if you're standing in this tile or
	 *         not
	 */
	public boolean canMoveLeft() {
		return canMoveLeft;
	}

	/**
	 * @return - Whether you can move up if you're standing in this tile or not
	 */
	public boolean canMoveUp() {
		return canMoveUp;
	}

	/**
	 * @return - Whether you can move down if you're standing in this tile or
	 *         not
	 */
	public boolean canMoveDown() {
		return canMoveDown;
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
	
	public boolean isInVisionOfNPC() {
		for(RBNPC npc : map.getNPCs()) {
			if(npc.getTilesAround().contains(this)) {
				return true;
			}
		}
		return false;
	}
}