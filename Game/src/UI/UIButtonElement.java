package UI;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import org.joml.Vector2f;

import GameController.Input;
import Graphics.Rendering.GeneralRenderer;
import Utility.Callback;
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

		// If in between
		if (mPos.x > sPos.x && mPos.y > sPos.y) {
			if (mPos.x < sPos.x + dims.x && mPos.y < sPos.y + dims.y) {
				isHovered = true;

				if (Input.clickArr[GLFW_MOUSE_BUTTON_LEFT])
					onClick();
			}
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
