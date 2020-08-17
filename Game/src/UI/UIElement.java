package UI;

import Rendering.Renderer;

public class UIElement {
	
	Renderer rend;
	
	public UIElement(Renderer rend) {
		try {
			this.rend = rend.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render() {
		rend.render();
	}
	
	public void update() {}
}
