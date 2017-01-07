package dabomstew.rta.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dabomstew.rta.generic.RBMap;
import dabomstew.rta.generic.RBMapTile;

public class AStar {

	public static final int BASIC_COLLISION = 1;
	public static final int DIRECTIONAL_COLLISION = 2;

	private static NodeSorter nodeSorter = new NodeSorter();

	public static List<Node> findPath(RBMap map, Location start, Location end, boolean ableToWalkThroughGrass,
			int collisionDetection) {
		List<Node> openList = new ArrayList<Node>();
		List<Node> closedList = new ArrayList<Node>();
		Node current = new Node(start, null, 0, start.getDistance(end));
		openList.add(current);
		while (openList.size() > 0) {
			Collections.sort(openList, nodeSorter);
			current = openList.get(0);
			if (current.getPosition().equals(end)) {
				List<Node> path = new ArrayList<Node>();
				path.add(current);
				while (current.getParent() != null) {
					path.add(current);
					current = current.getParent();
				}
				openList.clear();
				closedList.clear();
				Collections.reverse(path);
				return path;
			}
			openList.remove(current);
			closedList.add(current);
			// check tiles in this format
			// 0 1 2
			// 3 4 5
			// 6 7 8
			for (int i = 0; i < 9; i++) {
				// don't check against diagonal tiles or against itself, it just
				// happens to be that every tile you don't want to check is even
				if (i % 2 == 0) {
					continue;
				}
				int x = current.getPosition().x;
				int y = current.getPosition().y;
				int xa = (i % 3) - 1;
				int ya = (i / 3) - 1;
				Location position = new Location(x + xa, y + ya);
				RBMapTile tile = map.getTile(x + xa, y + ya);
				if (tile == null) {
					continue;
				}
				if (tile.isOccupiedByNPC()) {
					continue;
				}
				if (collisionDetection == BASIC_COLLISION) {
					if (tile.isSolid()) {
						continue;
					}
				} else if (collisionDetection == DIRECTIONAL_COLLISION) {
					RBMapTile currentTile = map.getTile(current.getPosition().x, current.getPosition().y);
					if (i == 1 && !currentTile.canMoveUp()) {
						continue;
					}
					if (i == 3 && !currentTile.canMoveLeft()) {
						continue;
					}
					if (i == 5 && !currentTile.canMoveRight()) {
						continue;
					}
					if (i == 7 && !currentTile.canMoveDown()) {
						continue;
					}
				}
				if (tile.isGrassTile() && ableToWalkThroughGrass) {
					continue;
				}
				double gCost = current.getGCost() + current.getPosition().getDistance(position);
				double hCost = position.getDistance(end);
				Node node = new Node(position, current, gCost, hCost);
				if (isPositionInList(closedList, position) && gCost >= current.getGCost()) {
					continue;
				}
				if (!isPositionInList(openList, position) || gCost <= current.getGCost()) {
					openList.add(node);
				}
			}
		}
		closedList.clear();
		return null;

	}

	private static boolean isPositionInList(List<Node> list, Location position) {
		for (Node node : list) {
			if (node.getPosition().equals(position)) {
				return true;
			}
		}
		return false;
	}
}