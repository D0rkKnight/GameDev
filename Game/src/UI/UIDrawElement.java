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
	public void render() {
		rend.transform.pos = pos;
		rend.render();
	}

}
