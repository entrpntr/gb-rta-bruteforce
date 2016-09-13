package dabomstew.rta.nidobot;

import java.nio.ByteBuffer;

public class OverworldStateAction {
	
	
	public String statePos;
	public ByteBuffer savedState;
	public int nextInput;
	
	public OverworldStateAction(String statePos, ByteBuffer savedState, int nextInput) {
		super();
		this.statePos = statePos;
		this.savedState = savedState;
		this.nextInput = nextInput;
	}

}
