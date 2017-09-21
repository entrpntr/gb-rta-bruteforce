package dabomstew.rta.generic;

public class DVs {

	private int attack;
	private int defense;
	private int speed;
	private int special;
	
	public DVs(String dvs) {
		if(!dvs.matches("-?\\d+(\\.\\d+)?/-?\\d+(\\.\\d+)?/-?\\d+(\\.\\d+)?/-?\\d+(\\.\\d+)?")) {
			throw new IllegalArgumentException("Could not parse dvs " + dvs);
		}
		String[] array = dvs.split("/");
		this.attack = Integer.valueOf(array[0]);
		this.defense = Integer.valueOf(array[1]);
		this.special = Integer.valueOf(array[2]);
		this.speed = Integer.valueOf(array[3]);
	}
	
	public DVs(int attack, int defense, int speed, int special) {
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.special = special;
	}

	public DVs(int dvs) {
		this.attack = (dvs >> 12) & 0xF;
		this.defense = (dvs >> 8) & 0xF;
		this.speed = (dvs >> 4) & 0xF;
		this.special = (dvs) & 0xF;
	}
	
	public int getAttack() {
		return attack;
	}

	public int getDefense() {
		return defense;
	}

	public int getSpeed() {
		return speed;
	}

	public int getSpecial() {
		return special;
	}
	
	public int getHp() {
		return ((attack & 1) << 3) | ((defense & 1) << 2) | ((speed & 1) << 1) | (special & 1);
	}
	
	public int getHexDVs() {
		return (attack << 12) | (defense << 8) | (speed << 4) | (special);
	}
	
	
	public boolean isAttackOdd() {
		return attack % 2 == 1;
	}
	
	public boolean isDefenseOdd() {
		return defense % 2 == 1;
	}
	
	public boolean isSpeedOdd() {
		return speed % 2 == 1;
	}
	
	public boolean isSpecialOdd() {
		return special % 2 == 1;
	}
	
	public boolean equals(Object other) {
		if(other instanceof DVs) {
			DVs r = (DVs) other;
			return attack == r.attack && defense == r.defense && speed == r.speed && special == r.special;
		}
		return super.equals(other);
	}
}