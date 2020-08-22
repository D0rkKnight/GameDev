package Math;

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
	
	//Interpolates between the two values. Ratio of 0 is v1, Ratio of 1.0 is v2
	public static float lerp(float v1, float v2, float ratio) {
		float comp1 = (float) (v1 * (1.0-ratio));
		float comp2 = v2 * ratio;
		return comp1 + comp2;
	}
	
	public static float limit(float v, float min, float max) {
		v = Math.max(v, min);
		v = Math.min(v, max);
		
		return v;
	}
}
