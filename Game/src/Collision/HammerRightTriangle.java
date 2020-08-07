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
			System.err.println("rotation type not recognized");
		}
		
		//Archetypal no rotation triangle
		points = new Vector2f[] {
				new Vector2f(0f, 1f),
				new Vector2f(0f, 0f),
				new Vector2f(1f, 0f)
		};
		pushRotations(rotations);
	}
}
