package dabomstew.rta.ffef;

public class TargetStat {

	public static final int LESS_OR_EQUAL = 1;
	public static final int EQUAL = 2;
	public static final int GREATER_OR_EQUAL = 3;
	
	private int minDv;
	private int comparisonMethod;
	
	public TargetStat(int minDv) {
		this(minDv, GREATER_OR_EQUAL);
	}
	
	public TargetStat(int minDv, int comparisonMethod) {
		this.minDv = minDv;
		this.comparisonMethod = comparisonMethod;
	}
	
	public boolean compare(int inDv) {
		switch(comparisonMethod) {
		case LESS_OR_EQUAL:
			return inDv <= minDv;
		case EQUAL:
			return inDv == minDv;
		case GREATER_OR_EQUAL:
			return inDv >= minDv;
		}
		return false;
	}
}