package dabomstew.nidorta;

public class GBMemory {
	
	private boolean stale;
	private int[] mem;
	
	public GBMemory() {
		stale = true;
		mem = null;
	}
	
	public int getTurnFrameStatus() {
		return read(0xCC4B);
	}
	
	public int getX() {
		return read(0xD362);
	}
	
	public int getY() {
		return read(0xD361);
	}
	
	public int getMap() {
		return read(0xD35E);
	}
	
	public int getHRA() {
		return read(0xFFD3);
	}
	
	public int getHRS() {
		return read(0xFFD4);
	}
	
	public int getBattleType() {
		return read(0xD057);
	}
	
	public int getEncounterSpecies() {
		return read(0xCFE5);
	}
	
	public int getEncounterLevel() {
		return read(0xCFF3);
	}
	
	public int getEncounterDVs() {
		return (read(0xCFF1) << 8) | read(0xCFF2);
	}
	
	public int read(int address) {
		if(stale) {
			mem = NidoBot.gb.getMemory();
			stale = false;
		}
		return mem[address];
	}

	public void setStale() {
		stale = true;
	}

}
