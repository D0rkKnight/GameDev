package UI;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Wrappers.Color;

public class UIBarElement extends UIBoxElement {

	public float fillRatio;

	public UIBarElement(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(rend, pos, dims, col);
		fillRatio = 1.0f;
	}

	@Override
	public void update() {
		super.update();

		// Buffer vertex changes to renderer
		if (rend instanceof GeneralRenderer) {
			Vector2f fillDims = new Vector2f(dims.x * fillRatio, dims.y);
			Vector2f[] verts = Shape.ShapeEnum.SQUARE.v.getRenderVertices(fillDims);

			((GeneralRenderer) rend).updateVertices(verts);

		} else {
			new Exception("It's time to fix this hack").printStackTrace();
		}
	}
}
