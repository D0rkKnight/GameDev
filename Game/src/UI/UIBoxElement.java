package UI;

import org.joml.Vector2f;

import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Wrappers.Color;

public class UIBoxElement extends UIDrawElement {

	Vector2f dims;

	public static final int ANCHOR_UL = 0;
	public static final int ANCHOR_BL = 1;
	public static final int ANCHOR_MID = 2;

	public UIBoxElement(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(rend, pos);

		this.dims = dims;
		if (rend instanceof GeneralRenderer) {
			GeneralRenderer sprRend = (GeneralRenderer) this.rend;
			sprRend.init(new Transformation(pos, Transformation.MATRIX_MODE_SCREEN), dims,
					HammerShape.HAMMER_SHAPE_SQUARE, col);
			sprRend.spr = Debug.debugTex;
		}
	}

	public void setAnchor(int anchorId) {
		switch (anchorId) {
		case ANCHOR_BL:
			offset.zero();
			break;
		case ANCHOR_UL:
			offset.set(0, dims.y);
			break;
		case ANCHOR_MID:
			offset.set(-dims.x / 2, dims.y / 2);
			break;
		default:
			new Exception("Anchor not recognized").printStackTrace();
			break;
		}
	}
}
