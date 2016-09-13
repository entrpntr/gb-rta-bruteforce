package dabomstew.rta;

public class Encounter {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((battleRNG == null) ? 0 : battleRNG.hashCode());
		result = prime * result + dvs;
		result = prime * result + level;
		result = prime * result + species;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Encounter other = (Encounter) obj;
		if (battleRNG == null) {
			if (other.battleRNG != null)
				return false;
		} else if (!battleRNG.equals(other.battleRNG))
			return false;
		if (dvs != other.dvs)
			return false;
		if (level != other.level)
			return false;
		if (species != other.species)
			return false;
		return true;
	}
	public Encounter(int species, int level, int dvs, String battleRNG) {
		super();
		this.species = species;
		this.level = level;
		this.dvs = dvs;
		this.battleRNG = battleRNG;
	}
	public int species;
	public int level;
	public int dvs;
	public String battleRNG;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(species);
		sb.append("/");
		sb.append(level);
		sb.append("/");
		sb.append(String.format("%04X", dvs));
		sb.append("/");
		sb.append(battleRNG);
		return sb.toString();
	}

}
