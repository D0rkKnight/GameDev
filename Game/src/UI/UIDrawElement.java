package UI;

import org.joml.Vector2f;

import Graphics.Rendering.Renderer;

public class UIDrawElement extends UIElement {

	Renderer rend;

	public UIDrawElement(Renderer rend, Vector2f pos) {
		super(pos);

		try {
			this.rend = rend.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(Vector2f relativeTo) {
		super.render(relativeTo);

		Vector2f newPos = new Vector2f(pos).add(offset).add(relativeTo);

		rend.transform.pos = newPos;
		rend.render();
	}

}
