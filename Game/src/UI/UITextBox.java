package UI;

import org.joml.Vector2f;

import Wrappers.Color;

public class UITextBox extends UICanvas {

	public UIBoxElement box;
	public UITextElement text;

	public UITextBox(Vector2f pos, Vector2f dims, String text) {
		super(pos, dims);

		// Instantiated in local space
		box = new UIBoxElement(new Vector2f(), dims, Color.DARK_GRAY);
		this.text = new UITextElement(text, new Vector2f(), dims);

		addElement(box);
		addElement(this.text);
	}

	@Override
	public void setAnchor(int anchor) {
		box.setAnchor(anchor);
		text.setAnchor(anchor);
	}
}
