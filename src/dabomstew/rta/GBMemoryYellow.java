package dabomstew.rta;

import mrwint.gbtasgen.Gb;

public class GBMemoryYellow {

	private Gb gb;
	
	public GBMemoryYellow(Gb gb) {
		this.gb = gb;
	}
	
	public int getX() {
		return gb.readMemory(0xD361);
	}
	
	public int getY() {
		return gb.readMemory(0xD360);
	}
	
	public int getMap() {
		return gb.readMemory(0xD35D);
	}
	
	public int getHRA() {
		return gb.readMemory(0xFFD3);
	}
	
	public int getHRS() {
		return gb.readMemory(0xFFD4);
	}
	
	public int getEncounterSpecies() {
		return gb.readMemory(0xCFE4);
	}
	
	public int getEncounterLevel() {
		return gb.readMemory(0xCFF2);
	}
	
	public int getTurnFrameStatus() {
		return gb.readMemory(0xCC4B);
	}
	
	public int getNPCTimer(int npc) {
		return gb.readMemory(0xC208) + npc * 0x10;
	}
	
	public int getEncounterDVs() {
		return (gb.readMemory(0xCFF0) << 8) | gb.readMemory(0xCFF1);
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
		for(int i = 1; i <= 15; i++) {
			sb.append("-");
			sb.append(getNPCTimer(i));
		}
		return sb.toString();
	}
	
    public String getNPCTimers() {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= 15; i++) {
			sb.append("-");
			sb.append(getNPCTimer(i));
		}
		return sb.toString();
	}
    
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
	
	public int getIGT() {
		return 3600 * gb.readMemory(0xda42) + 60 * gb.readMemory(0xda43) + gb.readMemory(0xda44);
	}

    public String getRNGStateHRAOnly() {
        StringBuilder sb = new StringBuilder();
        sb.append(gb.getDivState());
        sb.append("-");
        sb.append(getHRA());
        return sb.toString();
    }
}