package Collision.HammerShapes;

import org.joml.Vector2f;

public class HammerRightTriangle extends HammerShape {

	public HammerRightTriangle(HShapeEnum triangleType) {
		super(triangleType);

		int rotations = 0;
		switch (triangleType) {
		case TRIANGLE_BL:
			rotations = 0;
			break;
		case TRIANGLE_BR:
			rotations = 1;
			break;
		case TRIANGLE_UR:
			rotations = 2;
			break;
		case TRIANGLE_UL:
			rotations = 3;
			break;
		default:
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
