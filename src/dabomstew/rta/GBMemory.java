package dabomstew.rta;

import mrwint.gbtasgen.Gb;

public class GBMemory {
	
	private boolean stale;
	private int[] mem;
	private Gb gb;
	
	public GBMemory(Gb gb) {
		stale = true;
		mem = null;
		this.gb = gb;
	}
	
	public int getTurnFrameStatus() {
		return read(0x10);
	}
	
	public int getX() {
		return read(0x11);
	}
	
	public int getY() {
		return read(0x12);
	}
	
	public int getMap() {
		return read(0x13);
	}
	
	public int getHRA() {
		return read(0x14);
	}
	
	public int getHRS() {
		return read(0x15);
	}
	
	public int getBattleType() {
		return read(0x16);
	}
	
	public int getEncounterSpecies() {
		return read(0x17);
		//return gb.readMemory(0xCFE5);
	}
	
	public int getEncounterLevel() {
		return read(0x18);
		//return gb.readMemory(0xCFF3);
	}
	
	public int getEncounterDVs() {
		//return ((gb.readMemory(0xCFF1) << 8) | gb.readMemory(0xCFF2));
		return (read(0x19) << 8) | read(0x1A);
	}
	
	public int getCurrentSprite() {
	    return read(0x1B) >> 4;
	}
	
	public boolean isDroppingInputs() {
	    return (read(0x1C) & 0x20) > 0;
	}
	
	public int getNPCTimer(int npc) {
	    return read(npc);
	}
	
	private int read(int address) {
		if(stale) {
			mem = gb.getInterestingMemory();
			stale = false;
		}
		return mem[address];
	}

	public void setStale() {
		stale = true;
	}
	
	public String getUniqid() {
        StringBuilder sb = new StringBuilder();
        sb.append(gb.getDivState());
        sb.append("-");
        sb.append(getHRA());
        sb.append("-");
        sb.append(getHRS());
        sb.append("-");
        sb.append(getTurnFrameStatus());
        sb.append("-");
        sb.append(getX());
        sb.append("-");
        sb.append(getY());
        sb.append("-");
        sb.append(getMap());
        for (int i = 1; i <= 15; i++) {
            // NPC counters
            sb.append("-");
            sb.append(getNPCTimer(i));
        }
        return sb.toString();
    }

    public String getNPCTimers() {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= 15; i++) {
			// NPC counters
			sb.append("-");
			sb.append(getNPCTimer(i));
		}
		return sb.toString();
	}

/*
    private int readHRA() {
		return gb.readMemory(0xFFD3);
	}
	private int readHRS() {
		return gb.readMemory(0xFFD4);
	}
*/

	public String getRNGState() {
        StringBuilder sb = new StringBuilder();
        sb.append(gb.getDivState());
        sb.append("-");
        sb.append(getHRA());
        sb.append("-");
        sb.append(getHRS());
        return sb.toString();
    }

	public String getRNGStateWithDsum() {
		StringBuilder sb = new StringBuilder();
		sb.append(gb.getDivState());
		sb.append("-");
		sb.append(getHRA());
		sb.append("-");
		sb.append(getHRS());
		sb.append("-");
		sb.append(Integer.toString((getHRS()+getHRA()) % 256));
		return sb.toString();
	}

    public String getRNGStateHRAOnly() {
        StringBuilder sb = new StringBuilder();
        sb.append(gb.getDivState());
        sb.append("-");
        sb.append(getHRA());
        return sb.toString();
    }

    public int getIGT() {
		return 3600*read(0x1D)+60*read(0x1E)+read(0x1F);
	}

}
