package UI;

import org.joml.Vector2f;

import Graphics.Rendering.Renderer;
import Wrappers.Color;

public class UIBarCanvas extends UICanvas {

	public UIBoxElement bg;
	public UIBarElement bar;

	public UIBarCanvas(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(pos);

		bg = new UIBoxElement(rend, new Vector2f(), dims, new Color(0.1f, 0.1f, 0.1f, 1));
		bar = new UIBarElement(rend, new Vector2f(), dims, col);

		children.add(bg);
		children.add(bar);
	}

	public void setAnchor(int anchor) {
		bg.setAnchor(anchor);
		bar.setAnchor(anchor);
	}
}
