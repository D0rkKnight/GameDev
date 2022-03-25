package UI;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import GameController.Camera;
import GameController.Input;
import Graphics.Rendering.GeneralRenderer;
import Utility.Callback;
import Utility.Rect;
import Wrappers.Color;

public class UIButtonElement extends UIBoxElement {

	private boolean isHovered;
	private boolean wasHovered;

	private Color baseCol;
	private Callback clickCb;

	public UIButtonElement(Vector2f pos, Vector2f dims, Color col) {
		super(pos, dims, col);

		baseCol = new Color(col);
	}

	@Override
	public void update() {
		super.update();

		Vector2f mPos = Input.mouseScreenPos;
		isHovered = false;

		// Convert local vertices to screen space
		Matrix4f lSpace2World = genChildL2WMat();

		Vector2f[] verts = Rect.getPointCollectionFromDims(dims);
		Vector2f[] ssv = new Vector2f[verts.length];

		for (int i = 0; i < verts.length; i++) {
			// Drop vertex position by height into 4th quadrant (expected quadrant for UI
			// origin manipulations)
			verts[i].y -= dims.y;

			Vector4f v4 = new Vector4f(verts[i].x, verts[i].y, 0, 1);
			v4.mul(lSpace2World, v4); // Transforms to UI world space (which mouse position is in too)

			ssv[i] = new Vector2f(v4.x, v4.y);
			ssv[i].y += Camera.main.viewport.y; // UI positions are 4th quadrant
		}

		// Convert from UI screenspace to Graphical screenspace
		Vector2f mp = new Vector2f(Input.mouseScreenPos.x, Camera.main.viewport.y - Input.mouseScreenPos.y);

		Vector2f ur = ssv[1];
		Vector2f bl = ssv[3];

		if (mp.x > bl.x && mp.x < ur.x && mp.y > bl.y && mp.y < ur.y) {
			isHovered = true;

			if (Input.clickArr[GLFW_MOUSE_BUTTON_LEFT])
				onClick();
		}

		if (isHovered && !wasHovered)
			onHoverEnter();
		if (!isHovered && wasHovered)
			onHoverLeave();

		wasHovered = isHovered;
	}

	public void onHoverEnter() {
		((GeneralRenderer) rend).updateColors(new Color(1, 1, 1, 1));
	}

	public void onHoverLeave() {
		((GeneralRenderer) rend).updateColors(baseCol);
	}

	public void setClickCb(Callback cb) {
		clickCb = cb;
	}

	public void onClick() {
		if (clickCb != null)
			clickCb.invoke();
	}
}
