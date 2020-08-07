package Collision;

import org.joml.Vector2f;

/**
 * Container class for collision and mesh data
 * @author Hanzen Shou
 *
 */
public abstract class HammerShape {
	public static final int HAMMER_SHAPE_SQUARE = 0;
	public static final int HAMMER_SHAPE_TRIANGLE_BL = 1;
	public static final int HAMMER_SHAPE_TRIANGLE_BR = 2;
	public static final int HAMMER_SHAPE_TRIANGLE_UL = 3;
	public static final int HAMMER_SHAPE_TRIANGLE_UR = 4;
	public static final int HAMMER_SHAPE_FINAL = 5;
	
	public final static int BORDER_L = 0;
	public final static int BORDER_R = 1;
	public final static int BORDER_T = 2;
	public final static int BORDER_B = 3;
	
	public int shapeId;
	public Vector2f[] points;
	
	public HammerShape(int shapeId) {
		points = null;
		this.shapeId = shapeId;
	}
	
	protected void pushRotations(int rotations) {
		for (int i=0; i<rotations; i++) {
			for (int j=0; j<points.length; j++) {
				Vector2f v = points[j];
				float x = v.x;
				float y = v.y;
				
				float temp = x;
				x = 1-y;
				y = temp;
				
				v.x = x;
				v.y = y;
			}
		}
	}
}
