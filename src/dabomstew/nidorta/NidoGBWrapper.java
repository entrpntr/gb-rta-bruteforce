package dabomstew.nidorta;

public class NidoGBWrapper {
	
	private GBMemory mem;
	
	public NidoGBWrapper(GBMemory mem) {
		this.mem = mem;
	}
	
	public int advanceToAddress(int... addresses) {
		int result = 0;
		int runs = 0;
		while(result == 0) {
			if(addresses.length == 0) {
				result = NidoBot.gb.step(0);
			}
			else {
				result = NidoBot.gb.step(0, addresses);
			}
			runs++;
//			try {
//				Thread.sleep(16);
//			} catch (InterruptedException e) {
//			}
		}
		mem.setStale();
		//System.out.println("returned after "+runs+" runs with result "+result);
		return result;
	}

	public void injectInput(int input) {
		writeMemory(0xFFF8, input);
	}

	public void writeMemory(int address, int value) {
		NidoBot.gb.writeMemory(address, value);
		mem.setStale();
	}

	public void advanceFrame() {
		NidoBot.gb.step(0);
		mem.setStale();
	}

}
