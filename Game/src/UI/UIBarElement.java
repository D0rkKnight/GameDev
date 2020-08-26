package UI;

import org.joml.Vector2f;

import Collision.HammerShape;
import GameController.GameManager;
import Rendering.Renderer;
import Rendering.GeneralRenderer;
import Wrappers.Color;

public class UIBarElement extends UIBoxElement{
	
	public float fillRatio;
	
	public UIBarElement(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(rend, pos, dims, col);
		fillRatio = 1.0f;
		// TODO Auto-generated constructor stub
	}
	
	public void update() {
		
		//Buffer vertex changes to renderer
		if (rend instanceof GeneralRenderer) {
			Vector2f fillDims = new Vector2f(dims.x*fillRatio, dims.y);
			Vector2f[] verts = GameManager.hammerLookup.get(HammerShape.HAMMER_SHAPE_SQUARE).getRenderVertices(fillDims);
			
			((GeneralRenderer) rend).updateVertices(verts);

		} else {
			new Exception("It's time to fix this hack").printStackTrace();
		}
	}
}
