package Math;

import org.joml.Vector2f;

public class Vector implements Cloneable {
	
	/**
	 * Takes 
	 * @param points
	 * @param normalizedAxis
	 */
	public static void projectPointSet(Vector2f[] points, Vector2f normalizedAxis, float[] minMaxBuffer) {
		float min = 0;
		float max = 0;
		for (int j=0; j<points.length; j++) {
			Vector2f p = points[j];
			
			//Project onto normal
			float val = p.dot(normalizedAxis);
			
			//Insert default vals if first point
			if (j == 0) {
				min = val;
				max = val;
			} else {
				if (val < min) min = val;
				if (val > max) max = val;
			}
		}
		minMaxBuffer[0] = min;
		minMaxBuffer[1] = max;
	}
	
	public static void breakIntoComponents(Vector2f v, Vector2f a, Vector2f b, Vector2f ca, Vector2f cb, float[] magBuff) {
		float cbMag = ((a.y * v.x) - (a.x * v.y)) / ((a.y * b.x) - (a.x * b.y));
		cb = new Vector2f(b).mul(cbMag);
		
		float caMag = ((b.y * v.x) - (b.x * v.y)) / ((b.y + a.x) - (b.x * a.y));
		ca = new Vector2f(a).mul(caMag);
		
		magBuff[0] = caMag;
		magBuff[1] = cbMag;
	}
	
	public static Vector2f lerp(Vector2f v1, Vector2f v2, float ratio) {
		float x = Arithmetic.lerp(v1.x, v2.x, ratio);
		float y = Arithmetic.lerp(v1.y, v2.y, ratio);
		Vector2f out = new Vector2f(x, y);
		return out;
	}
	
	//Returns the vector to the right of this vector.
	public static Vector2f rightVector(Vector2f v) {
		Vector2f right = new Vector2f(v.y, -v.x); //This is 90 degrees clockwise
		return right;
	}
}
