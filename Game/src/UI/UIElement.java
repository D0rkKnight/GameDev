package UI;

import org.joml.Vector2f;

import Rendering.Renderer;

public class UIElement {
	
	Vector2f pos;
	
	public UIElement(Vector2f pos) {
		this.pos = pos;
	}
	
	public void render() {
	}
	
	public void update() {}
}
