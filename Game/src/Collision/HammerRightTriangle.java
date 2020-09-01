package Collision;

import org.joml.Vector2f;

public class HammerRightTriangle extends HammerShape{
	
	public HammerRightTriangle(int triangleType) {
		super(triangleType);
		
		int rotations = 0;
		switch (triangleType) {
		case HAMMER_SHAPE_TRIANGLE_BL:
			rotations = 0;
			break;
		case HAMMER_SHAPE_TRIANGLE_BR:
			rotations = 1;
			break;
		case HAMMER_SHAPE_TRIANGLE_UR:
			rotations = 2;
			break;
		case HAMMER_SHAPE_TRIANGLE_UL:
			rotations = 3;
			break;
		default:
			new Exception("rotation type not recognized").printStackTrace();
		}
		
		//Archetypal no rotation triangle (Bottom left)
		vertices = new Vector2f[] {
				new Vector2f(0f, 1f),
				new Vector2f(0f, 0f),
				new Vector2f(1f, 0f)
		};
		pushRotations(rotations, vertices);
		genNormals();
		
		Vector2f bl = new Vector2f(0, 0);
		Vector2f br = new Vector2f(1, 0);
		Vector2f ul = new Vector2f(0, 1);
		Vector2f ur = new Vector2f(1, 1);
		triangulatedVertices = new Vector2f[] {
			ul,
			bl,
			br
		};
		pushRotations(rotations, triangulatedVertices);
	}
}
