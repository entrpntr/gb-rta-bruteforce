package dabomstew.rta.generic;

public class RBPokemon {

	private static RBPokemon[] pokemon = new RBPokemon[256];

	public static final RBPokemon RHYDON = new RBPokemon("RHYDON", 1);
	public static final RBPokemon KANGASKHAN = new RBPokemon("KANGASKHAN", 2);
	public static final RBPokemon NIDORAN_MALE = new RBPokemon("NIDORAN_MALE", 3);
	public static final RBPokemon CLEFAIRY = new RBPokemon("CLEFAIRY", 4);
	public static final RBPokemon SPEAROW = new RBPokemon("SPEAROW", 5);
	public static final RBPokemon VOLTORB = new RBPokemon("VOLTORB", 6);
	public static final RBPokemon NIDOKING = new RBPokemon("NIDOKING", 7);
	public static final RBPokemon SLOWBRO = new RBPokemon("SLOWBRO", 8);
	public static final RBPokemon IVYSAUR = new RBPokemon("IVYSAUR", 9);
	public static final RBPokemon EXEGGUTOR = new RBPokemon("EXEGGUTOR", 10);
	public static final RBPokemon LICKITUNG = new RBPokemon("LICKITUNG", 11);
	public static final RBPokemon EXEGGCUTE = new RBPokemon("EXEGGCUTE", 12);
	public static final RBPokemon GRIMER = new RBPokemon("GRIMER", 13);
	public static final RBPokemon GENGAR = new RBPokemon("GENGAR", 14);
	public static final RBPokemon NIDORAN_FEMALE = new RBPokemon("NIDORAN_FEMALE", 15);
	public static final RBPokemon NIDOQUEEN = new RBPokemon("NIDOQUEEN", 16);
	public static final RBPokemon CUBONE = new RBPokemon("CUBONE", 17);
	public static final RBPokemon RHYHORN = new RBPokemon("RHYHORN", 18);
	public static final RBPokemon LAPRAS = new RBPokemon("LAPRAS", 19);
	public static final RBPokemon ARCANINE = new RBPokemon("ARCANINE", 20);
	public static final RBPokemon MEW = new RBPokemon("MEW", 21);
	public static final RBPokemon GYARADOS = new RBPokemon("GYARADOS", 22);
	public static final RBPokemon SHELLDER = new RBPokemon("SHELLDER", 23);
	public static final RBPokemon TENTACOOL = new RBPokemon("TENTACOOL", 24);
	public static final RBPokemon GASTLY = new RBPokemon("GASTLY", 25);
	public static final RBPokemon SCYTHER = new RBPokemon("SCYTHER", 26);
	public static final RBPokemon STARYU = new RBPokemon("STARYU", 27);
	public static final RBPokemon BLASTOISE = new RBPokemon("BLASTOISE", 28);
	public static final RBPokemon PINSIR = new RBPokemon("PINSIR", 29);
	public static final RBPokemon TANGELA = new RBPokemon("TANGELA", 30);
	public static final RBPokemon GROWLITHE = new RBPokemon("GROWLITHE", 33);
	public static final RBPokemon ONIX = new RBPokemon("ONIX", 34);
	public static final RBPokemon FEAROW = new RBPokemon("FEAROW", 35);
	public static final RBPokemon PIDGEY = new RBPokemon("PIDGEY", 36);
	public static final RBPokemon SLOWPOKE = new RBPokemon("SLOWPOKE", 37);
	public static final RBPokemon KADABRA = new RBPokemon("KADABRA", 38);
	public static final RBPokemon GRAVELER = new RBPokemon("GRAVELER", 39);
	public static final RBPokemon CHANSEY = new RBPokemon("CHANSEY", 40);
	public static final RBPokemon MACHOKE = new RBPokemon("MACHOKE", 41);
	public static final RBPokemon MR_MIME = new RBPokemon("MR. MIME", 42);
	public static final RBPokemon HITMONLEE = new RBPokemon("HITMONLEE", 43);
	public static final RBPokemon HITMONCHAN = new RBPokemon("HITMONCHAN", 44);
	public static final RBPokemon ARBOK = new RBPokemon("ARBOK", 45);
	public static final RBPokemon PARASECT = new RBPokemon("PARASECT", 46);
	public static final RBPokemon PSYDUCK = new RBPokemon("PSYDUCK", 47);
	public static final RBPokemon DROWZEE = new RBPokemon("DROWZEE", 48);
	public static final RBPokemon GOLEM = new RBPokemon("GOLEM", 49);
	public static final RBPokemon MAGMAR = new RBPokemon("MAGMAR", 51);
	public static final RBPokemon ELECTABUZZ = new RBPokemon("ELECTABUZZ", 53);
	public static final RBPokemon MAGNETON = new RBPokemon("MAGNETON", 54);
	public static final RBPokemon KOFFING = new RBPokemon("KOFFING", 55);
	public static final RBPokemon MANKEY = new RBPokemon("MANKEY", 57);
	public static final RBPokemon SEEL = new RBPokemon("SEEL", 58);
	public static final RBPokemon DIGLETT = new RBPokemon("DIGLETT", 59);
	public static final RBPokemon TAUROS = new RBPokemon("TAUROS", 60);
	public static final RBPokemon FARFETCHD = new RBPokemon("FARFETCH'D", 64);
	public static final RBPokemon VENONAT = new RBPokemon("VENONAT", 65);
	public static final RBPokemon DRAGONITE = new RBPokemon("DRAGONITE", 66);
	public static final RBPokemon DODUO = new RBPokemon("DODUO", 70);
	public static final RBPokemon POLIWAG = new RBPokemon("POLIWAG", 71);
	public static final RBPokemon JYNX = new RBPokemon("JYNX", 72);
	public static final RBPokemon MOLTRES = new RBPokemon("MOLTRES", 73);
	public static final RBPokemon ARTICUNO = new RBPokemon("ARTICUNO", 74);
	public static final RBPokemon ZAPDOS = new RBPokemon("ZAPDOS", 75);
	public static final RBPokemon DITTO = new RBPokemon("DITTO", 76);
	public static final RBPokemon MEOWTH = new RBPokemon("MEOWTH", 77);
	public static final RBPokemon KRABBY = new RBPokemon("KRABBY", 78);
	public static final RBPokemon VULPIX = new RBPokemon("VULPIX", 82);
	public static final RBPokemon NINETALES = new RBPokemon("NINETALES", 83);
	public static final RBPokemon PIKACHU = new RBPokemon("PIKACHU", 84);
	public static final RBPokemon RAICHU = new RBPokemon("RAICHU", 85);
	public static final RBPokemon DRATINI = new RBPokemon("DRATINI", 88);
	public static final RBPokemon DRAGONAIR = new RBPokemon("DRAGONAIR", 89);
	public static final RBPokemon KABUTO = new RBPokemon("KABUTO", 90);
	public static final RBPokemon KABUTOPS = new RBPokemon("KABUTOPS", 91);
	public static final RBPokemon HORSEA = new RBPokemon("HORSEA", 92);
	public static final RBPokemon SEADRA = new RBPokemon("SEADRA", 93);
	public static final RBPokemon SANDSHREW = new RBPokemon("SANDSHREW", 96);
	public static final RBPokemon SANDSLASH = new RBPokemon("SANDSLASH", 97);
	public static final RBPokemon OMANYTE = new RBPokemon("OMANYTE", 98);
	public static final RBPokemon OMASTAR = new RBPokemon("OMASTAR", 99);
	public static final RBPokemon JIGGLYPUFF = new RBPokemon("JIGGLYPUFF", 100);
	public static final RBPokemon WIGGLYTUFF = new RBPokemon("WIGGLYTUFF", 101);
	public static final RBPokemon EEVEE = new RBPokemon("EEVEE", 102);
	public static final RBPokemon FLAREON = new RBPokemon("FLAREON", 103);
	public static final RBPokemon JOLTEON = new RBPokemon("JOLTEON", 104);
	public static final RBPokemon VAPOREON = new RBPokemon("VAPOREON", 105);
	public static final RBPokemon MACHOP = new RBPokemon("MACHOP", 106);
	public static final RBPokemon ZUBAT = new RBPokemon("ZUBAT", 107);
	public static final RBPokemon EKANS = new RBPokemon("EKANS", 108);
	public static final RBPokemon PARAS = new RBPokemon("PARAS", 109);
	public static final RBPokemon POLIWHIRL = new RBPokemon("POLIWHIRL", 110);
	public static final RBPokemon POLIWRATH = new RBPokemon("POLIWRATH", 111);
	public static final RBPokemon WEEDLE = new RBPokemon("WEEDLE", 112);
	public static final RBPokemon KAKUNA = new RBPokemon("KAKUNA", 113);
	public static final RBPokemon BEEDRILL = new RBPokemon("BEEDRILL", 114);
	public static final RBPokemon DODRIO = new RBPokemon("DODRIO", 116);
	public static final RBPokemon PRIMEAPE = new RBPokemon("PRIMEAPE", 117);
	public static final RBPokemon DUGTRIO = new RBPokemon("DUGTRIO", 118);
	public static final RBPokemon VENOMOTH = new RBPokemon("VENOMOTH", 119);
	public static final RBPokemon DEWGONG = new RBPokemon("DEWGONG", 120);
	public static final RBPokemon CATERPIE = new RBPokemon("CATERPIE", 123);
	public static final RBPokemon METAPOD = new RBPokemon("METAPOD", 124);
	public static final RBPokemon BUTTERFREE = new RBPokemon("BUTTERFREE", 125);
	public static final RBPokemon MACHAMP = new RBPokemon("MACHAMP", 126);
	public static final RBPokemon GOLDUCK = new RBPokemon("GOLDUCK", 128);
	public static final RBPokemon HYPNO = new RBPokemon("HYPNO", 129);
	public static final RBPokemon GOLBAT = new RBPokemon("GOLBAT", 130);
	public static final RBPokemon MEWTWO = new RBPokemon("MEWTWO", 131);
	public static final RBPokemon SNORLAX = new RBPokemon("SNORLAX", 132);
	public static final RBPokemon MAGIKARP = new RBPokemon("MAGIKARP", 133);
	public static final RBPokemon MUK = new RBPokemon("MUK", 136);
	public static final RBPokemon KINGLER = new RBPokemon("KINGLER", 138);
	public static final RBPokemon CLOYSTER = new RBPokemon("CLOYSTER", 139);
	public static final RBPokemon ELECTRODE = new RBPokemon("ELECTRODE", 141);
	public static final RBPokemon CLEFABLE = new RBPokemon("CLEFABLE", 142);
	public static final RBPokemon WEEZING = new RBPokemon("WEEZING", 143);
	public static final RBPokemon PERSIAN = new RBPokemon("PERSIAN", 144);
	public static final RBPokemon MAROWAK = new RBPokemon("MAROWAK", 145);
	public static final RBPokemon HAUNTER = new RBPokemon("HAUNTER", 147);
	public static final RBPokemon ABRA = new RBPokemon("ABRA", 148);
	public static final RBPokemon ALAKAZAM = new RBPokemon("ALAKAZAM", 149);
	public static final RBPokemon PIDGEOTTO = new RBPokemon("PIDGEOTTO", 150);
	public static final RBPokemon PIDGEOT = new RBPokemon("PIDGEOT", 151);
	public static final RBPokemon STARMIE = new RBPokemon("STARMIE", 152);
	public static final RBPokemon BULBASAUR = new RBPokemon("BULBASAUR", 153);
	public static final RBPokemon VENUSAUR = new RBPokemon("VENUSAUR", 154);
	public static final RBPokemon TENTACRUEL = new RBPokemon("TENTACRUEL", 155);
	public static final RBPokemon GOLDEEN = new RBPokemon("GOLDEEN", 157);
	public static final RBPokemon SEAKING = new RBPokemon("SEAKING", 158);
	public static final RBPokemon PONYTA = new RBPokemon("PONYTA", 163);
	public static final RBPokemon RAPIDASH = new RBPokemon("RAPIDASH", 164);
	public static final RBPokemon RATTATA = new RBPokemon("RATTATA", 165);
	public static final RBPokemon RATICATE = new RBPokemon("RATICATE", 166);
	public static final RBPokemon NIDORINO = new RBPokemon("NIDORINO", 167);
	public static final RBPokemon NIDORINA = new RBPokemon("NIDORINA", 168);
	public static final RBPokemon GEODUDE = new RBPokemon("GEODUDE", 169);
	public static final RBPokemon PORYGON = new RBPokemon("PORYGON", 170);
	public static final RBPokemon AERODACTYL = new RBPokemon("AERODACTYL", 171);
	public static final RBPokemon MAGNEMITE = new RBPokemon("MAGNEMITE", 173);
	public static final RBPokemon CHARMANDER = new RBPokemon("CHARMANDER", 176);
	public static final RBPokemon SQUIRTLE = new RBPokemon("SQUIRTLE", 177);
	public static final RBPokemon CHARMELEON = new RBPokemon("CHARMELEON", 178);
	public static final RBPokemon WARTORTLE = new RBPokemon("WARTORTLE", 179);
	public static final RBPokemon CHARIZARD = new RBPokemon("CHARIZARD", 180);
	public static final RBPokemon ODDISH = new RBPokemon("ODDISH", 185);
	public static final RBPokemon GLOOM = new RBPokemon("GLOOM", 186);
	public static final RBPokemon VILEPLUME = new RBPokemon("VILEPLUME", 187);
	public static final RBPokemon BELLSPROUT = new RBPokemon("BELLSPROUT", 188);
	public static final RBPokemon WEEPINBELL = new RBPokemon("WEEPINBELL", 189);
	public static final RBPokemon VICTREEBEL = new RBPokemon("VICTREEBEL", 190);
	
	public static RBPokemon getPokemonByIndexNumber(int indexNumber) {
		return pokemon[indexNumber];
	}
	
	private final String name;
	private final int indexNumber;

	public RBPokemon(String name, int indexNumber) {
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