package Collision.Shapes;

import org.joml.Vector2f;

public class ShapeRightTriangle extends Shape {

	public ShapeRightTriangle(int rotations) {
		super();

		if (rotations < 0 || rotations > 3) {
			new Exception("rotation type not recognized").printStackTrace();
		}

		// Archetypal no rotation triangle (Bottom left)
		vertices = new Vector2f[] { ul, bl, br };
		pushRotations(rotations, vertices);
		genNormals();

		triangulatedVertices = new Vector2f[] { ul, bl, br };
		pushRotations(rotations, triangulatedVertices);
	}
}
