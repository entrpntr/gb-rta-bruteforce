package dabomstew.rta.nidobot;

import java.nio.ByteBuffer;

public class GrassGrindAction {
	
	public GrassGrindAction(ByteBuffer savedState, String preGrassAreaPath, String inGrassAreaPath, int input, int numSteps, int lhInput) {
		super();
		this.savedState = savedState;
		this.preGrassAreaPath = preGrassAreaPath;
		this.inGrassAreaPath = inGrassAreaPath;
		this.input = input;
		this.numSteps = numSteps;
		this.lastHorizontalInput = lhInput;
	}
	public ByteBuffer savedState;
	public String preGrassAreaPath;
	public String inGrassAreaPath;
	public int input;
	public int numSteps;
	public int lastHorizontalInput;
}
