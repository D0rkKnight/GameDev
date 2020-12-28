package Collision.HammerShapes;

import org.joml.Vector2f;

public class HammerSquare extends HammerShape {

	public HammerSquare() {
		super(HShapeEnum.SQUARE);
		vertices = new Vector2f[] { br, ur, ul, bl };
		genNormals();

		triangulatedVertices = new Vector2f[] { ul, bl, br, br, ur, ul };
	}
}
