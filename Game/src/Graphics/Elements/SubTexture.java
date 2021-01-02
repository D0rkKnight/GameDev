package Graphics.Elements;

import org.joml.Vector2f;

import Collision.Shapes.Shape;

/**
 * Given a texture atlas, use this to find the right sub texture.
 * 
 * @author Hanzen Shou
 *
 */
public class SubTexture {
	public float x;
	public float y;
	public float w;
	public float h;

	public SubTexture(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	/**
	 * Create UVs to access the texture properly on the texture map
	 * 
	 * @param shape
	 * @return
	 */
	public Vector2f[] genSubUV(Shape shape) {
		Vector2f[] rawUV = shape.getRenderUVs();
		Vector2f[] out = new Vector2f[rawUV.length];

		for (int i = 0; i < rawUV.length; i++) {
			Vector2f newV = rawUV[i].mul(w, h).add(x, y);
			out[i] = newV;
		}

		return out;
	}
}
