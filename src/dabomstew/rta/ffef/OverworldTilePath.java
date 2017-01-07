package dabomstew.rta.ffef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dabomstew.rta.astar.AStar;
import dabomstew.rta.astar.Location;
import dabomstew.rta.astar.Node;
import dabomstew.rta.generic.RBMap;
import dabomstew.rta.generic.RBMapDestination;

public class OverworldTilePath {

	private RBMap map;
	private OverworldTile src;
	private Map<OverworldTile, List<Node>> possiblePaths;

	public OverworldTilePath(OverworldTile src, RBMapDestination dest) {
		this.src = src;
		this.map = RBMap.getMapByID(src.getMap());
		this.possiblePaths = new HashMap<OverworldTile, List<Node>>();
		for(Location location : dest.getDestinationTiles()) {
			if((dest.getMode() == RBMapDestination.WEST_CONNECTION && map.getTile(location.x, location.y).canMoveLeft()) ||
			   (dest.getMode() == RBMapDestination.NORTH_CONNECTION && map.getTile(location.x, location.y).canMoveUp()) ||
			   (dest.getMode() == RBMapDestination.EAST_CONNECTION && map.getTile(location.x, location.y).canMoveRight()) ||
			   (dest.getMode() == RBMapDestination.SOUTH_CONNECTION && map.getTile(location.x, location.y).canMoveDown()) ||
			   (dest.getMode() == RBMapDestination.GRASS_PATCHES)) {
				possiblePaths.put(map.getOverworldTile(location.x, location.y), AStar.findPath(map, new Location(src.getX(), src.getY()), location, false, AStar.DIRECTIONAL_COLLISION));
			}
		}
	}

	public RBMap getMap() {
		return map;
	}

	public OverworldTile getSrc() {
		return src;
	}

	public List<Node> getShortestPath() {
		List<Node> result = null;
		for (List<Node> path : possiblePaths.values()) {
			if (result == null) {
				result = path;
				continue;
			}
			if (path == null) {
				continue;
			}
			if (result.size() > path.size()) {
				result = path;
			}
		}
		return result;
	}
}