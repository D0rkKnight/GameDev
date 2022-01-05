package UI;

import org.joml.Vector2f;

import Graphics.Rendering.Renderer;

public class UIDrawElement extends UIElement {

	Renderer rend;

	public UIDrawElement(Vector2f pos, Vector2f dims) {
		super(pos, dims);
	}

	@Override
	public void render() {

		if (rend.parent != this)
			rend.parent = this;

		// Render yourself first
		// Vector2f rendPos = new Vector2f(sPos).add(0, dims.y);// 4th quadrant

		// rend.worldToScreen.scale.identity();
		// rend.worldToScreen.scale.scale(1f / Camera.main.viewport.x, 1f /
		// Camera.main.viewport.y, 1f);
		rend.render(); // Remember the view matrix flips the coordinates

		// Render children next
		super.render();
	}

}
