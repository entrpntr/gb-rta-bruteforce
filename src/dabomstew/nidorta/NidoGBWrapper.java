package dabomstew.nidorta;

import mrwint.gbtasgen.Gb;

public class NidoGBWrapper {
	
	private GBMemory mem;
	private Gb gb;
	
	public NidoGBWrapper(Gb gb, GBMemory mem) {
		this.mem = mem;
		this.gb = gb;
	}
	
	public int advanceToAddress(int... addresses) {
		int result = 0;
		int runs = 0;
		while(result == 0) {
			if(addresses.length == 0) {
				result = gb.step(0);
			}
			else {
				result = gb.step(0, addresses);
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
		gb.writeMemory(address, value);
		mem.setStale();
	}

	public void advanceFrame() {
		gb.step(0);
		mem.setStale();
	}

}
