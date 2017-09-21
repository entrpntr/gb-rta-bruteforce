package dabomstew.rta.yellowmoon;

import java.nio.ByteBuffer;

import dabomstew.rta.ffef.OverworldState;
import dabomstew.rta.ffef.OverworldTile;

public class OverworldStateMP {

	private String str;
	private OverworldTile pos;
	private ByteBuffer[] saves;
	private int aPress;
	private boolean startPress;
	private int rdiv;
	private int hra;
	private int hrs;
	private int turnframe;
	private String npcTimers;
	private int wastedFrames;
	private int overworldFrames;

	public OverworldStateMP(String str, OverworldTile pos, ByteBuffer[] saves, int aPress, boolean startPress, int rDiv, int hra, int hrs, int turnframe, String npcTimers, int wastedFrames, int overworldFrames) {
		this.str = str;
		this.pos = pos;
		this.aPress = aPress;
		this.saves = saves;
		this.startPress = startPress;
		this.rdiv = rDiv;
		this.hra = hra;
		this.hrs = hrs;
		this.turnframe = turnframe;
		this.npcTimers = npcTimers;
		this.wastedFrames = wastedFrames;
		this.overworldFrames = overworldFrames;
	}

	public int getTurnframeStatus() {
		return turnframe;
	}

	public String getNpcTimers() {
		return npcTimers;
	}

	public int getOverworldFrames() {
		return overworldFrames;
	}

	public int getWastedFrames() {
		return wastedFrames;
	}

	public int getMap() {
		return pos.getMap();
	}

	public int getX() {
		return pos.getX();
	}

	public int getY() {
		return pos.getY();
	}

	public int aPressCounter() {
		return aPress;
	}

	public boolean canPressStart() {
		return startPress;
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

	public int getDsum() {
		return ((hrs + hra) % 256);
	}

	public OverworldTile getPos() {
		return pos;
	}

	public ByteBuffer[] getSaves() {
		return saves;
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public boolean equals(Object other) {
		OverworldState o = (OverworldState) other;
		return this.getMap() == o.getMap() && this.getX() == o.getX() && this.getY() == o.getY() && this.rdiv == o.getRdiv() && this.hra == o.getHra() && this.hrs == o.getHrs();
	}

	@Override
	public int hashCode() {
		return this.getMap() + 2 * this.getX() + 3 * this.getY() + 11 * rdiv + 13 * hra + 17 * hrs;
	}

	public String getUniqId() {
		return "" + pos.getMap() + "#" + pos.getX() + "," + pos.getY() + "-" + turnframe + npcTimers + "-" + rdiv + "-" + hra + "-" + hrs;
	}
}