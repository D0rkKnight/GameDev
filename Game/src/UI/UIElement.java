package UI;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Utility.Callback;
import Utility.Transformations.ModelTransform;

public class UIElement {

	ModelTransform localTrans;
	Vector2f pos;
	Vector2f dims;

	UIElement parent;

	Callback updateCb;

	private ArrayList<UIElement> children;

	protected Vector2f origin;
	public static final int ANCHOR_UL = 0;
	public static final int ANCHOR_BL = 1;
	public static final int ANCHOR_MID = 2;

	boolean wPosGenerated = false;

	public UIElement(Vector2f pos, Vector2f dims) {
		this.pos = pos;
		this.localTrans = new ModelTransform();
		this.dims = dims;

		children = new ArrayList<>();

		origin = new Vector2f(); // None by default
	}

	public void render() {
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
	}

	public Matrix4f genChildL2WMat() {
		ModelTransform lMat = new ModelTransform(localTrans);
		lMat.trans.translate(pos.x, -pos.y, 0);

		// Reify L2P (local to parent) space matrix
		Matrix4f l2p = lMat.genModel();

		// Assign output matrix
		Matrix4f o = l2p;

		// Left side L2W mult
		if (parent != null) {
			// Multiply recursively
			o = parent.genChildL2WMat().mul(l2p);
		}

		// Right side anchor shift mult
		Matrix4f anchorTrans = new Matrix4f().translate(origin.x, origin.y, 0);
		o.mul(anchorTrans);

		return o;
	}

	public void setUpdateCb(Callback newCb) {
		updateCb = newCb;
	}

	public void addElement(UIElement e) {
		children.add(e);

		e.onAttach(this);
	}

	public void setAnchor(int anchorId) {
		// This is all in the 4th quadrant
		// Remember that items in the space are expected to have negative y values

		switch (anchorId) {
		case ANCHOR_BL:
			origin.set(0, -dims.y);
			break;
		case ANCHOR_UL:
			origin.zero();
			break;
		case ANCHOR_MID:
			origin.set(dims).div(2, -2);
			break;
		default:
			new Exception("Anchor not recognized").printStackTrace();
			break;
		}
	}
}
