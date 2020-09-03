package Collision.HammerShapes;

import org.joml.Vector2f;

public class HammerSquare extends HammerShape{
	
	public HammerSquare() {
		super(HAMMER_SHAPE_SQUARE);
		vertices = new Vector2f[] {
			new Vector2f(0, 0),
			new Vector2f(1, 0),
			new Vector2f(1, 1),
			new Vector2f(0, 1)
		};
		genNormals();
		
		//TODO: Polygon triangulation algorithm? Probably unecessary.
		Vector2f ul = new Vector2f(0, 0);
		Vector2f ur = new Vector2f(1, 0);
		Vector2f bl = new Vector2f(0, 1);
		Vector2f br = new Vector2f(1, 1);
		triangulatedVertices = new Vector2f[] {
			ul,
			bl,
			br,
			br,
			ur,
			ul
		};
	}
}
