package UI;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Wrappers.Color;

public class UIBoxElement extends UIDrawElement {

	public UIBoxElement(Vector2f pos, Vector2f dims, Color col) {
		super(pos, dims);

		GeneralRenderer sprRend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		sprRend.init(new Transformation(new Vector2f(), Transformation.MatrixMode.SCREEN), dims, Shape.ShapeEnum.SQUARE,
				col);
		this.rend = sprRend;
	}

}
