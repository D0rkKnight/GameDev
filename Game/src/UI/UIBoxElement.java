package UI;

import org.joml.Vector2f;

import Collision.HammerShape;
import Debugging.Debug;
import Rendering.Renderer;
import Utility.Transformation;
import Rendering.GeneralRenderer;
import Wrappers.Color;

public class UIBoxElement extends UIDrawElement{
	
	Vector2f dims;
	
	public UIBoxElement(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(rend, pos);
		// TODO Auto-generated constructor stub
		
		this.dims = dims;
		if (rend instanceof GeneralRenderer) {
			GeneralRenderer sprRend = (GeneralRenderer) this.rend;
			sprRend.init(new Transformation(pos, Transformation.MATRIX_MODE_SCREEN), dims, HammerShape.HAMMER_SHAPE_SQUARE, col);
			sprRend.spr = Debug.debugTex;
		}
	}

	
}
