package Collision;

import Wrappers.Vector2;

public class HammerSquare extends HammerShape{
	
	public HammerSquare() {
		super(HAMMER_SHAPE_SQUARE);
		points = new Vector2[] {
			new Vector2(0, 0),
			new Vector2(1, 0),
			new Vector2(1, 1),
			new Vector2(0, 1)
		};
	}
}
