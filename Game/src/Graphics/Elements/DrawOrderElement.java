package Graphics.Elements;

/**
 * Used to order elements to be drawn.
 * 
 * @author Hanzen Shou
 *
 */

public abstract class DrawOrderElement {
	public int z;

	public DrawOrderElement(int z) {
		this.z = z;
	}
}
