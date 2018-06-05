package dabomstew.rta.entei;

public class GscCoord {
    public int map;
    public int x;
    public int y;
    public GscCoord(int map, int x, int y) {
        this.map = map;
        this.x = x;
        this.y = y;
    }

    public static GscCoord at(int map, int x, int y) {
        return new GscCoord(map, x, y);
    }

    public int getMap() {
        return map;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GscCoord coord = (GscCoord) o;

        if (map != coord.map) return false;
        if (x != coord.x) return false;
        return y == coord.y;
    }

    @Override
    public int hashCode() {
        int result = map;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
