package Utility;

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

}
