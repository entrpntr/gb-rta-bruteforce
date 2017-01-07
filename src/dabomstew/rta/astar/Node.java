package dabomstew.rta.astar;

public class Node {

	private Location position;
	private Node parent;
	private double fCost;
	private double gCost;
	private double hCost;
	
	public Node(Location position, Node parent, double gCost, double hCost) {
		this.position = position;
		this.parent = parent;
		this.gCost = gCost;
		this.hCost = hCost;
		this.fCost = gCost + hCost;
	}
	
	public Location getPosition() {
		return position;
	}

	public Node getParent() {
		return parent;
	}

	public double getFCost() {
		return fCost;
	}

	public double getGCost() {
		return gCost;
	}

	public double getHCost() {
		return hCost;
	}
	
	public String toString() {
		return "(" + position.x + " " + position.y + ")";
	}
}