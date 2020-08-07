package Collision;

import org.joml.Vector2f;

public class HammerSquare extends HammerShape{
	
	public HammerSquare() {
		super(HAMMER_SHAPE_SQUARE);
		points = new Vector2f[] {
			new Vector2f(0, 0),
			new Vector2f(1, 0),
			new Vector2f(1, 1),
			new Vector2f(0, 1)
		};
	}
}
