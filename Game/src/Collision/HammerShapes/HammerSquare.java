package Collision.HammerShapes;

import org.joml.Vector2f;

public class HammerSquare extends HammerShape {

	public HammerSquare() {
		super(HAMMER_SHAPE_SQUARE);
		vertices = new Vector2f[] { br, ur, ul, bl };
		genNormals();

		// TODO: Polygon triangulation algorithm? Probably unecessary.
		triangulatedVertices = new Vector2f[] { ul, bl, br, br, ur, ul };
	}
}