package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

public class UICanvas extends UIElement {

	ArrayList<UIElement> children;

	public UICanvas(Vector2f pos) {
		super(pos);
		children = new ArrayList<>();
	}

	@Override
	public void render() {
		super.render();
		for (UIElement c : children)
			c.render();
	}

	@Override
	public void update() {
		super.update();
		for (UIElement c : children)
			c.update();
	}
}
