package Utility;

import org.joml.Vector2f;
import org.joml.Vector4f;

import Utility.Transformations.Transformed;

public class Center {

	private Vector2f p;
	private Transformed parent;

	public Center(Transformed parent) {
		this.parent = parent;

		p = new Vector2f();
	}

	public void set(Vector2f p) {
		this.p.set(p);
	}

	public void set(float x, float y) {
		this.p.set(x, y);
	}

	// Gets the raw positional value of the center
	public Vector2f raw() {
		return p;
	}

	// Gets the positional value after local transformations are applied
	public Vector2f local() {
		Vector4f v4 = new Vector4f(p.x, p.y, 0, 1).mul(parent.getLocalTrans().genModel());

		return new Vector2f(v4.x, v4.y);
	}
}
