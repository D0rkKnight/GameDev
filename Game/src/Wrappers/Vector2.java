package Wrappers;

public class Vector2 implements Cloneable {
	public float x;
	public float y;
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Vector2 v) {
		x = v.x;
		y = v.y;
	}
	
	public Vector2 add(Vector2 v) {
		return new Vector2(x + v.x, y+v.y);
	}
	
	public Vector2 sub(Vector2 v) {
		return new Vector2(x - v.x, y-v.y);
	}
	
	public Vector2 mult(float f) {
		return new Vector2(x * f, y * f);
	}
	
	public void subtractFromThis(Vector2 pos) {
		this.x -= pos.x;
		this.y -= pos.y;
	}
	
	public void addToThis(Vector2 pos) {
		this.x += pos.x;
		this.y += pos.y;
	}
	
	public void addToThis(Rect r) {
		this.x += r.w;
		this.y += r.h;
	}
	
	public Vector2 clone() throws CloneNotSupportedException {
		return (Vector2) super.clone();
	}
	
	public Vector2 unit() {
		float mag = magnitude();
		Vector2 out = new Vector2(x/mag, y/mag);
		return out;
	}
	
	public float magnitude() {
		float out = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		return out;
	}
	
	/**
	 * Projects this vector onto another vector
	 * @param v : Vector to be projected onto
	 * @return
	 */
	public float dot(Vector2 v) {
		return x * v.x + y * v.y;
	}
	
	/**
	 * Takes 
	 * @param points
	 * @param normalizedAxis
	 */
	public static void projectPointSet(Vector2[] points, Vector2 normalizedAxis, float[] minMaxBuffer) {
		float min = 0;
		float max = 0;
		for (int j=0; j<points.length; j++) {
			Vector2 p = points[j];
			
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
	
	public String toString() {
		return "(X: "+x+", Y: "+y+")";
	}
	
	public void breakIntoComponents(Vector2 a, Vector2 b, Vector2 ca, Vector2 cb, float[] magBuff) {
		float cbMag = ((a.y * x) - (a.x * y)) / ((a.y * b.x) - (a.x * b.y));
		cb = b.mult(cbMag);
		
		float caMag = ((b.y * x) - (b.x * y)) / ((b.y + a.x) - (b.x * a.y));
		ca = a.mult(caMag);
		
		magBuff[0] = caMag;
		magBuff[1] = cbMag;
	}
	
	public static Vector2 lerp(Vector2 v1, Vector2 v2, float ratio) {
		float x = Arithmetic.lerp(v1.x, v2.x, ratio);
		float y = Arithmetic.lerp(v1.y, v2.y, ratio);
		Vector2 out = new Vector2(x, y);
		return out;
	}
	
	//Returns the vector to the right of this vector.
	public Vector2 rightVector() {
		Vector2 right = new Vector2(y, -x); //This is 90 degrees clockwise
		return right;
	}
}
