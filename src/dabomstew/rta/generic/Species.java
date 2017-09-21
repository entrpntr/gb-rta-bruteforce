package dabomstew.rta.generic;

public class Species {

	private static Species[] pokemon = new Species[256];

	public static final Species RHYDON = new Species("RHYDON", 1);
	public static final Species KANGASKHAN = new Species("KANGASKHAN", 2);
	public static final Species NIDORAN_MALE = new Species("NIDORAN_MALE", 3);
	public static final Species CLEFAIRY = new Species("CLEFAIRY", 4);
	public static final Species SPEAROW = new Species("SPEAROW", 5);
	public static final Species VOLTORB = new Species("VOLTORB", 6);
	public static final Species NIDOKING = new Species("NIDOKING", 7);
	public static final Species SLOWBRO = new Species("SLOWBRO", 8);
	public static final Species IVYSAUR = new Species("IVYSAUR", 9);
	public static final Species EXEGGUTOR = new Species("EXEGGUTOR", 10);
	public static final Species LICKITUNG = new Species("LICKITUNG", 11);
	public static final Species EXEGGCUTE = new Species("EXEGGCUTE", 12);
	public static final Species GRIMER = new Species("GRIMER", 13);
	public static final Species GENGAR = new Species("GENGAR", 14);
	public static final Species NIDORAN_FEMALE = new Species("NIDORAN_FEMALE", 15);
	public static final Species NIDOQUEEN = new Species("NIDOQUEEN", 16);
	public static final Species CUBONE = new Species("CUBONE", 17);
	public static final Species RHYHORN = new Species("RHYHORN", 18);
	public static final Species LAPRAS = new Species("LAPRAS", 19);
	public static final Species ARCANINE = new Species("ARCANINE", 20);
	public static final Species MEW = new Species("MEW", 21);
	public static final Species GYARADOS = new Species("GYARADOS", 22);
	public static final Species SHELLDER = new Species("SHELLDER", 23);
	public static final Species TENTACOOL = new Species("TENTACOOL", 24);
	public static final Species GASTLY = new Species("GASTLY", 25);
	public static final Species SCYTHER = new Species("SCYTHER", 26);
	public static final Species STARYU = new Species("STARYU", 27);
	public static final Species BLASTOISE = new Species("BLASTOISE", 28);
	public static final Species PINSIR = new Species("PINSIR", 29);
	public static final Species TANGELA = new Species("TANGELA", 30);
	public static final Species GROWLITHE = new Species("GROWLITHE", 33);
	public static final Species ONIX = new Species("ONIX", 34);
	public static final Species FEAROW = new Species("FEAROW", 35);
	public static final Species PIDGEY = new Species("PIDGEY", 36);
	public static final Species SLOWPOKE = new Species("SLOWPOKE", 37);
	public static final Species KADABRA = new Species("KADABRA", 38);
	public static final Species GRAVELER = new Species("GRAVELER", 39);
	public static final Species CHANSEY = new Species("CHANSEY", 40);
	public static final Species MACHOKE = new Species("MACHOKE", 41);
	public static final Species MR_MIME = new Species("MR. MIME", 42);
	public static final Species HITMONLEE = new Species("HITMONLEE", 43);
	public static final Species HITMONCHAN = new Species("HITMONCHAN", 44);
	public static final Species ARBOK = new Species("ARBOK", 45);
	public static final Species PARASECT = new Species("PARASECT", 46);
	public static final Species PSYDUCK = new Species("PSYDUCK", 47);
	public static final Species DROWZEE = new Species("DROWZEE", 48);
	public static final Species GOLEM = new Species("GOLEM", 49);
	public static final Species MAGMAR = new Species("MAGMAR", 51);
	public static final Species ELECTABUZZ = new Species("ELECTABUZZ", 53);
	public static final Species MAGNETON = new Species("MAGNETON", 54);
	public static final Species KOFFING = new Species("KOFFING", 55);
	public static final Species MANKEY = new Species("MANKEY", 57);
	public static final Species SEEL = new Species("SEEL", 58);
	public static final Species DIGLETT = new Species("DIGLETT", 59);
	public static final Species TAUROS = new Species("TAUROS", 60);
	public static final Species FARFETCHD = new Species("FARFETCH'D", 64);
	public static final Species VENONAT = new Species("VENONAT", 65);
	public static final Species DRAGONITE = new Species("DRAGONITE", 66);
	public static final Species DODUO = new Species("DODUO", 70);
	public static final Species POLIWAG = new Species("POLIWAG", 71);
	public static final Species JYNX = new Species("JYNX", 72);
	public static final Species MOLTRES = new Species("MOLTRES", 73);
	public static final Species ARTICUNO = new Species("ARTICUNO", 74);
	public static final Species ZAPDOS = new Species("ZAPDOS", 75);
	public static final Species DITTO = new Species("DITTO", 76);
	public static final Species MEOWTH = new Species("MEOWTH", 77);
	public static final Species KRABBY = new Species("KRABBY", 78);
	public static final Species VULPIX = new Species("VULPIX", 82);
	public static final Species NINETALES = new Species("NINETALES", 83);
	public static final Species PIKACHU = new Species("PIKACHU", 84);
	public static final Species RAICHU = new Species("RAICHU", 85);
	public static final Species DRATINI = new Species("DRATINI", 88);
	public static final Species DRAGONAIR = new Species("DRAGONAIR", 89);
	public static final Species KABUTO = new Species("KABUTO", 90);
	public static final Species KABUTOPS = new Species("KABUTOPS", 91);
	public static final Species HORSEA = new Species("HORSEA", 92);
	public static final Species SEADRA = new Species("SEADRA", 93);
	public static final Species SANDSHREW = new Species("SANDSHREW", 96);
	public static final Species SANDSLASH = new Species("SANDSLASH", 97);
	public static final Species OMANYTE = new Species("OMANYTE", 98);
	public static final Species OMASTAR = new Species("OMASTAR", 99);
	public static final Species JIGGLYPUFF = new Species("JIGGLYPUFF", 100);
	public static final Species WIGGLYTUFF = new Species("WIGGLYTUFF", 101);
	public static final Species EEVEE = new Species("EEVEE", 102);
	public static final Species FLAREON = new Species("FLAREON", 103);
	public static final Species JOLTEON = new Species("JOLTEON", 104);
	public static final Species VAPOREON = new Species("VAPOREON", 105);
	public static final Species MACHOP = new Species("MACHOP", 106);
	public static final Species ZUBAT = new Species("ZUBAT", 107);
	public static final Species EKANS = new Species("EKANS", 108);
	public static final Species PARAS = new Species("PARAS", 109);
	public static final Species POLIWHIRL = new Species("POLIWHIRL", 110);
	public static final Species POLIWRATH = new Species("POLIWRATH", 111);
	public static final Species WEEDLE = new Species("WEEDLE", 112);
	public static final Species KAKUNA = new Species("KAKUNA", 113);
	public static final Species BEEDRILL = new Species("BEEDRILL", 114);
	public static final Species DODRIO = new Species("DODRIO", 116);
	public static final Species PRIMEAPE = new Species("PRIMEAPE", 117);
	public static final Species DUGTRIO = new Species("DUGTRIO", 118);
	public static final Species VENOMOTH = new Species("VENOMOTH", 119);
	public static final Species DEWGONG = new Species("DEWGONG", 120);
	public static final Species CATERPIE = new Species("CATERPIE", 123);
	public static final Species METAPOD = new Species("METAPOD", 124);
	public static final Species BUTTERFREE = new Species("BUTTERFREE", 125);
	public static final Species MACHAMP = new Species("MACHAMP", 126);
	public static final Species GOLDUCK = new Species("GOLDUCK", 128);
	public static final Species HYPNO = new Species("HYPNO", 129);
	public static final Species GOLBAT = new Species("GOLBAT", 130);
	public static final Species MEWTWO = new Species("MEWTWO", 131);
	public static final Species SNORLAX = new Species("SNORLAX", 132);
	public static final Species MAGIKARP = new Species("MAGIKARP", 133);
	public static final Species MUK = new Species("MUK", 136);
	public static final Species KINGLER = new Species("KINGLER", 138);
	public static final Species CLOYSTER = new Species("CLOYSTER", 139);
	public static final Species ELECTRODE = new Species("ELECTRODE", 141);
	public static final Species CLEFABLE = new Species("CLEFABLE", 142);
	public static final Species WEEZING = new Species("WEEZING", 143);
	public static final Species PERSIAN = new Species("PERSIAN", 144);
	public static final Species MAROWAK = new Species("MAROWAK", 145);
	public static final Species HAUNTER = new Species("HAUNTER", 147);
	public static final Species ABRA = new Species("ABRA", 148);
	public static final Species ALAKAZAM = new Species("ALAKAZAM", 149);
	public static final Species PIDGEOTTO = new Species("PIDGEOTTO", 150);
	public static final Species PIDGEOT = new Species("PIDGEOT", 151);
	public static final Species STARMIE = new Species("STARMIE", 152);
	public static final Species BULBASAUR = new Species("BULBASAUR", 153);
	public static final Species VENUSAUR = new Species("VENUSAUR", 154);
	public static final Species TENTACRUEL = new Species("TENTACRUEL", 155);
	public static final Species GOLDEEN = new Species("GOLDEEN", 157);
	public static final Species SEAKING = new Species("SEAKING", 158);
	public static final Species PONYTA = new Species("PONYTA", 163);
	public static final Species RAPIDASH = new Species("RAPIDASH", 164);
	public static final Species RATTATA = new Species("RATTATA", 165);
	public static final Species RATICATE = new Species("RATICATE", 166);
	public static final Species NIDORINO = new Species("NIDORINO", 167);
	public static final Species NIDORINA = new Species("NIDORINA", 168);
	public static final Species GEODUDE = new Species("GEODUDE", 169);
	public static final Species PORYGON = new Species("PORYGON", 170);
	public static final Species AERODACTYL = new Species("AERODACTYL", 171);
	public static final Species MAGNEMITE = new Species("MAGNEMITE", 173);
	public static final Species CHARMANDER = new Species("CHARMANDER", 176);
	public static final Species SQUIRTLE = new Species("SQUIRTLE", 177);
	public static final Species CHARMELEON = new Species("CHARMELEON", 178);
	public static final Species WARTORTLE = new Species("WARTORTLE", 179);
	public static final Species CHARIZARD = new Species("CHARIZARD", 180);
	public static final Species ODDISH = new Species("ODDISH", 185);
	public static final Species GLOOM = new Species("GLOOM", 186);
	public static final Species VILEPLUME = new Species("VILEPLUME", 187);
	public static final Species BELLSPROUT = new Species("BELLSPROUT", 188);
	public static final Species WEEPINBELL = new Species("WEEPINBELL", 189);
	public static final Species VICTREEBEL = new Species("VICTREEBEL", 190);
	
	public static Species getSpeciesByIndexNumber(int indexNumber) {
		return pokemon[indexNumber];
	}
	
	private final String name;
	private final int indexNumber;

	public Species(String name, int indexNumber) {
		this.name = name;
		this.indexNumber = indexNumber;
		pokemon[indexNumber] = this;
	}

	public String getName() {
		return name;
	}

	public int getIndexNumber() {
		return indexNumber;
	}
}