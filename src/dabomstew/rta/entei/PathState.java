package dabomstew.rta.entei;

import java.util.ArrayList;
import java.util.List;

class PathState {
    private int aPress;
    private int edgeSet;
    private GscTile curTile;
    private ArrayList<GscAction> curPath;
    PathState(int aPress, int edgeSet, GscTile curTile, ArrayList<GscAction> curPath) {
        this.aPress = aPress;
        this.edgeSet = edgeSet;
        this.curTile = curTile;
        this.curPath = curPath;
    }

    int getaPress() {
        return aPress;
    }

    int getEdgeSet() {
        return edgeSet;
    }

    GscTile getCurTile() {
        return curTile;
    }

    ArrayList<GscAction> getCurPath() {
        return curPath;
    }

    boolean canPressA() {
        return (aPress == 0);
    }
}
