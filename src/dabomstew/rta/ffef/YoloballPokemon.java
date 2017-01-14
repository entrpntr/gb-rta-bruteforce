package dabomstew.rta.ffef;

import dabomstew.rta.generic.RBPokemon;

public class YoloballPokemon {

	private RBPokemon species;
	private int nameLength;
	
	public YoloballPokemon(RBPokemon species, int nameLength) {
		this.species = species;
		this.nameLength = nameLength;
	}

	public RBPokemon getSpecies() {
		return species;
	}

	public int getNameLength() {
		return nameLength;
	}
}