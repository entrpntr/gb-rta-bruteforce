package dabomstew.rta;

public class Position {
	
	public Position(int map, int x, int y) {
		super();
		this.map = map;
		this.x = x;
		this.y = y;
	}
	public int map;
	public int x;
	public int y;

    @Override public String toString() {
        return "[" + map + "#" + x + "," + y + "]";
    }
}
