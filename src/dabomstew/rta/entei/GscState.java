package dabomstew.rta.entei;

public class GscState {
    private String logStr;
    private boolean onBike;
    private boolean justChangedDir;
    private boolean prevChangedDir;
    private int facingDir;
    private int rdiv;
    private int hra;
    private int hrs;
    private int x;
    private int y;
    private boolean canSelect;
    private boolean canStart;
    private boolean turnframeStatus;

    public GscState(String logStr, boolean onBike, boolean justChangedDir, boolean prevChangedDir, int facingDir, int rdiv, int hra, int hrs, int x, int y, boolean canSelect, boolean canStart, boolean turnframeStatus) {
        this.logStr = logStr;
        this.onBike = onBike;
        this.justChangedDir = justChangedDir;
        this.prevChangedDir = prevChangedDir;
        this.facingDir = facingDir;
        this.rdiv = rdiv;
        this.hra = hra;
        this.hrs = hrs;
        this.x = x;
        this.y = y;
        this.canSelect = canSelect;
        this.canStart = canStart;
        this.turnframeStatus = turnframeStatus;
    }

    public String getLogStr() {
        return logStr;
    }

    public boolean onBike() {
        return onBike;
    }

    public boolean justChangedDir() {
        return justChangedDir;
    }

    public boolean prevChangedDir() {
        return prevChangedDir;
    }

    public int getFacingDir() {
        return facingDir;
    }

    public int getRdiv() {
        return rdiv;
    }

    public int getHra() {
        return hra;
    }

    public int getHrs() {
        return hrs;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean canSelect() {
        return canSelect;
    }

    public boolean canStart() {
        return canStart;
    }

    public boolean turnframeStatus() {
        return turnframeStatus;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GscState gscState = (GscState) o;

        if (onBike != gscState.onBike) return false;
        if (justChangedDir != gscState.justChangedDir) return false;
        if (prevChangedDir != gscState.prevChangedDir) return false;
        if (facingDir != gscState.facingDir) return false;
        if (rdiv != gscState.rdiv) return false;
        if (hra != gscState.hra) return false;
        if (hrs != gscState.hrs) return false;
        if (x != gscState.x) return false;
        if (y != gscState.y) return false;
        if (canStart != gscState.canStart) return false;
        if (turnframeStatus != gscState.turnframeStatus) return false;
        return canSelect == gscState.canSelect;
    }

    @Override
    public int hashCode() {
        int result = (onBike ? 1 : 0);
        result = 31 * result + (justChangedDir ? 1 : 0);
        result = 31 * result + (prevChangedDir ? 1 : 0);
        result = 31 * result + facingDir;
        result = 31 * result + rdiv;
        result = 31 * result + hra;
        result = 31 * result + hrs;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + (canSelect ? 1 : 0);
        result = 31 * result + (canStart ? 1 : 0);
        result = 31 * result + (turnframeStatus ? 1 : 0);
        return result;
    }
}
