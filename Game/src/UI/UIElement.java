package UI;

import org.joml.Vector2f;

import Utility.Callback;

public class UIElement {

	Vector2f pos;
	Callback updateCb;

	protected Vector2f offset;

	public UIElement(Vector2f pos) {
		this.pos = pos;

		offset = new Vector2f(); // None by default
	}

	public void render(Vector2f relativeTo) {
	}

	public void render() {
		render(new Vector2f());
	}

	public void update() {
		if (updateCb != null)
			updateCb.invoke();
	}

	public void setCb(Callback newCb) {
		updateCb = newCb;
	}
}
