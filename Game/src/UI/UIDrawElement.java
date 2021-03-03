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

		// Render yourself first
		rend.transform.pos.set(sPos);
		rend.transform.pos.add(0, dims.y); // 4th quadrant
		rend.render(); // Remember the view matrix flips the coordinates

		// Render children next
		super.render();
	}

}
