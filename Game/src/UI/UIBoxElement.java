package UI;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Wrappers.Color;

public class UIBoxElement extends UIDrawElement {

	public UIBoxElement(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(rend, pos, dims);

		if (rend instanceof GeneralRenderer) {
			GeneralRenderer sprRend = (GeneralRenderer) this.rend;
			sprRend.init(new Transformation(new Vector2f(), Transformation.MatrixMode.SCREEN), dims,
					Shape.ShapeEnum.SQUARE, col);
			sprRend.spr = Debug.debugTex;
		}
	}

}
