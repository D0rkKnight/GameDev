package Wrappers;

public abstract class Arithmetic {
	public static boolean isIntersecting(float min1, float max1, float min2, float max2, float[] distBuffer) {
		if (min1 > max2 || min2 > max1) return false;
		else {
			//Get intersection
			float out = Math.abs(Math.max(min1, min2) - Math.min(max1, max2));
			distBuffer[0] = out;
			
			return true;
		}
	}
	
	public static int sign(float val) {
		if (val == 0) return 0;
		else if (val > 0) return 1;
		else return -1;
	}
}
