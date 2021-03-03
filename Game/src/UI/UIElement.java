package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

import Utility.Callback;

public class UIElement {

	Vector2f relPos; // Relative position
	Vector2f sPos; // Screen position
	Vector2f dims;

	UIElement parent;

	Callback updateCb;

	private ArrayList<UIElement> children;

	protected Vector2f offset;
	public static final int ANCHOR_UL = 0;
	public static final int ANCHOR_BL = 1;
	public static final int ANCHOR_MID = 2;

	boolean wPosGenerated = false;

	public UIElement(Vector2f pos, Vector2f dims) {
		this.relPos = pos;
		this.dims = dims;
		this.sPos = new Vector2f();

		children = new ArrayList<>();

		offset = new Vector2f(); // None by default
	}

	public void render() {
		// Generate world position if not generated yet
		if (!wPosGenerated)
			genWPos();

		for (UIElement c : children)
			c.render();
	}

	public void update() {
		if (updateCb != null)
			updateCb.invoke();

		for (UIElement c : children)
			c.update();
	}

	protected void onAttach(UIElement newParent) {
		parent = newParent;
		genWPos();
	}

	protected void genWPos() {
		sPos.zero();

		if (parent != null) {
			parent.genWPos(); // Kindly request parent to regenerate their screen position

			sPos.add(parent.sPos);
		}

		sPos.add(relPos).add(offset);

		wPosGenerated = true;
	}

	public void setUpdateCb(Callback newCb) {
		updateCb = newCb;
	}

	public void addElement(UIElement e) {
		children.add(e);

		e.onAttach(this);
	}

	public void setAnchor(int anchorId) {
		// This is all in 4th quadrant coordinates

		switch (anchorId) {
		case ANCHOR_BL:
			offset.set(0, -dims.y);
			break;
		case ANCHOR_UL:
			offset.zero();
			break;
		case ANCHOR_MID:
			offset.set(dims).div(-2);
			break;
		default:
			new Exception("Anchor not recognized").printStackTrace();
			break;
		}

		genWPos();
	}
}
