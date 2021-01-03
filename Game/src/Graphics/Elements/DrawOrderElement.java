package Graphics.Elements;

/**
 * Used to order elements to be drawn.
 * 
 * @author Hanzen Shou
 *
 */

public abstract class DrawOrderElement {
	public int z;

	// Doesn't seem like a great solution TODO:
	public boolean destroyOnSceneChange = false;

	public DrawOrderElement(int z) {
		this.z = z;
	}
}
