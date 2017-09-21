package dabomstew.rta;

public class YellowAddr {

	public static final int joypadAddr = 0x01B9;
	public static final int joypadOverworldAddr = 0x0C51;
	public static final int newBattleAddr = 0x0480;
	public static final int enterMapAddr = 0x01D7;
	public static final int encounterTestAddr = 0x1388E;
	public static final int saveInjectAddr = 0x739D6;
	public static final int pikaInjectAddr = 0x739E7;
	public static final int manualTextScrollAddr = 0x388E;
	public static final int playCryAddr = 0x118B;
	public static final int playPikachuSoundClipAddr = 0xF0000;
	public static final int displayListMenuIdAddr = 0x2AE0;
	public static final int igtInjectAddr = 0x739D6;
	public static final int catchSuccessAddr = 0xD4D4;
	public static final int catchFailureAddr = 0xD4D6;
	public static final int textJingleCommandAddr = 0x1A0A;
	public static final int textHiddenJingleCommandAddr = 0x1A0A;
	public static final int printLetterDelayAddr = 0x23F8;
	public static final int displayTextBoxIdAddr = 0x3010;
    public static final int delayAtEndOfShootingStarAddr = 0x41A74;
    public static final int softResetAddr = 0x1D05;
    public static final int intro2Addr = 0xF996A;
    public static final int intro4Addr = 0xF9A1E;
    public static final int intro6Addr = 0xF9A6B;
    public static final int intro8Addr = 0xF9AD8;
    public static final int intro10Addr = 0xF9B04;
    public static final int intro12Addr = 0xF9CAC;
    public static final int titleScreenAddr = 0x4171;
    public static final int damageRollCalculationAddr = 0x3E82A;
    
	public static final int wPlayTimeHours = 0xDA40;
	public static final int wPlayTimeMaxed = 0xDA41;
	public static final int wPlayTimeMinutes = 0xDA42;
	public static final int wPlayTimeSeconds = 0xDA43;
	public static final int wPlayTimeFrames = 0xDA44;
	public static final int wEnemyUsedMove = 0xCCF2;
	public static final int wMoveMissed = 0xD05E;
	public static final int wCriticalHitOrOHKO = 0xD05D;
	//; $00 = normal attack
	//; $01 = critical hit
	//; $02 = successful OHKO
	//; $ff = failed OHKO
	public static final int wEnemyMonStatus = 0xCFE4;
	public static final int wPlayerMonAccuracyMod = 0xCD1E;
	public static final int wPlayerHPBarColor = 0xCF1C;
	public static final int wPartyMon1HP = 0xD16C;
	public static final int wEnemyMonHP = 0xCFE6;
	public static final int wNumBagItems = 0xD31C;
}