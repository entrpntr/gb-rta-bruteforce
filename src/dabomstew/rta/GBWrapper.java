package dabomstew.rta;

import java.nio.ByteBuffer;

import mrwint.gbtasgen.Gb;

public class GBWrapper {
	
	private GBMemory mem;
	private Gb gb;
	
	public GBWrapper(Gb gb, GBMemory mem) {
		this.mem = mem;
		this.gb = gb;
	}
	
	public int advanceToAddress(int... addresses) {
	    //System.out.println("advancing to "+printAddressList(addresses));
		int result = 0;
		//int runs = 0;
		while(result == 0) {
			if(addresses.length == 0) {
				result = gb.step(0);
			}
			else {
				result = gb.step(0, addresses);
			}
			//runs++;
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//			}
		}
		mem.setStale();
		//System.out.printf("returned after %d runs with result %04X\n", runs, result);
		return result;
	}
	
	public int advanceWithJoypadToAddress(int joypad, int... addresses) {
        int result = 0;
        while(result == 0) {
            if(addresses.length == 0) {
                result = gb.step(joypad);
            }
            else {
                result = gb.step(joypad, addresses);
            }
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//            }
        }
        mem.setStale();
        return result;
    }
	
	public void testSpeed() {
	    long start = System.currentTimeMillis();
	    for(int i=0;i<1000000;i++) {
	        gb.step(0);
	    }
	    System.out.println("executed 1000000 frames in "+(System.currentTimeMillis()-start)+"ms");
	}
	
	public String printAddressList(int[] addresses) {
	    StringBuilder sb = new StringBuilder("[");
	    for(int i=0;i<addresses.length;i++) {
	        sb.append(String.format("%X, ", addresses[i]));
	    }
	    sb.append("]");
	    return sb.toString();
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
	
	public void advanceFrame(int inputs) {
        gb.step(inputs);
        mem.setStale();
    }
	
	public void loadState(ByteBuffer state) {
	    gb.loadState(state);
	    mem.setStale();
	}

}
