package dabomstew.nidorta;

public class StartingExtraSteps {
	
	public static int getSteps(int map, int x, int y) {
		if(map == 1) {
			// Viridian
			int eSteps = 0;
			if(y < 17) {
				eSteps += (17-y);
			}
			if(y == 17 && x >= 18 && x <= 27) {
				// have to go down-up to escape this place
				eSteps++;
			}
			if(x > 29) {
				eSteps += (x-29);
			}
			if(y == 21) {
				eSteps++;
			}
			return eSteps*2;
		}
		else {
			// Route 22
			int eSteps = 0;
			if(y < 9) {
				eSteps += (9-y);
			}
			return eSteps*2;
		}
	}

}
