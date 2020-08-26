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
	protected Vector2f[] vertices;
	protected Vector2f[] triangulatedVertices;
	
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
	
	public Vector2f[] getRenderVertices(Vector2f dims) {
		Vector2f[] vs = triangulatedVertices.clone();

		//scale to dims
		for (int i=0; i<vs.length; i++) {
			vs[i] = new Vector2f(vs[i].x * dims.x, vs[i].y * dims.y);
		}
		return vs;
	}
	
	public Vector2f[] getRenderUVs() {
		Vector2f[] uvs = triangulatedVertices.clone();
		//Flip UVs vertically since opengl UVs are anchored upper left
		for (int i=0; i<uvs.length; i++) {
			uvs[i] = new Vector2f(uvs[i].x, 1-uvs[i].y);
		}
		return uvs;
	}
}
