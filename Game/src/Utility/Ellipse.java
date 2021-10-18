package Utility;

import org.joml.Vector2f;

public class Ellipse {
	public Vector2f dims;
	public Vector2f p;
	
	public Ellipse(Vector2f p, Vector2f dims) {
		this.p = p;
		this.dims = dims;
	}
	
	public Ellipse(Vector2f p, float r) {
		this(p, new Vector2f(r).mul(2));
	}
	
	public Vector2f[] genVerts(int segs) {
		Vector2f[] vs = new Vector2f[segs];
		
		for (int i=0; i<segs; i++) {
			float rad = (float) (((float) i)/segs * 2 * Math.PI);
			
			float dx = (float) Math.cos(rad) * dims.x / 2; // Not radius, div by 2
			float dy = (float) Math.sin(rad) * dims.y / 2;
			
			Vector2f v = new Vector2f(dx, dy).add(p);
			vs[i] = v;
		}
		
		return vs;
	}
}
