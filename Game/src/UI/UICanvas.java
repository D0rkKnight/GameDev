package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

public class UICanvas extends UIElement {

	protected ArrayList<UIElement> children;

	public UICanvas(Vector2f pos) {
		super(pos);
		children = new ArrayList<>();
	}

	@Override
	public void render(Vector2f relativeTo) {
		super.render(relativeTo);

		Vector2f newRelative = new Vector2f(pos).add(relativeTo);

		for (UIElement c : children)
			c.render(newRelative);
	}

	@Override
	public void update() {
		super.update();
		for (UIElement c : children)
			c.update();
	}

	public void addElement(UIElement e) {
		children.add(e);
	}

	public void remElement(UIElement e) {
		children.remove(e);
	}
}
