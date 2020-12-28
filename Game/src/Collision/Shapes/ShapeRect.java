package Collision.Shapes;

import org.joml.Vector2f;

public class ShapeRect extends Shape {

	public ShapeRect() {
		super();
		vertices = new Vector2f[] { br, ur, ul, bl };
		genNormals();

		triangulatedVertices = new Vector2f[] { ul, bl, br, br, ur, ul };
	}
}
