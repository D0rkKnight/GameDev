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
	
	public int shapeId;
	public Vector2f[] vertices;
	public Vector2f[] triangulatedVertices;
	
	public HammerShape(int shapeId) {
		vertices = null;
		triangulatedVertices = null;
		this.shapeId = shapeId;
	}
	
	protected static void pushRotations(int rotations, Vector2f[] points) {
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
