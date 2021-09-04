package Utility;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Rect {
	public Vector2f dims;

	public Rect(Vector2f dims) {
		this.dims = dims;
	}

	public float w() {
		return dims.x;
	}

	public float h() {
		return dims.y;
	}

	public Vector2f getUntransformedCenter() {
		return new Vector2f(dims).mul(0.5f);
	}

	public Vector2f getTransformedCenter(Matrix4f mat) {
		Vector4f v4c = new Vector4f(dims.x * 0.5f, dims.y * 0.5f, 0f, 1f).mul(mat);
		return new Vector2f(v4c.x, v4c.y);
	}

	public static Vector2f getDimsFromPointCollection(Collection<Vector2f> points) {
		Vector2f bl = new Vector2f();
		Vector2f ur = new Vector2f();

		for (Vector2f p : points) {
			if (p.x < bl.x)
				bl.x = p.x;
			if (p.y < bl.y)
				bl.y = p.y;
			if (p.x > ur.x)
				ur.x = p.x;
			if (p.y > ur.y)
				ur.y = p.y;
		}

		return new Vector2f(ur).sub(bl);
	}
}
