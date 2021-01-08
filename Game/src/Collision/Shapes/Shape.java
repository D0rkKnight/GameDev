package Collision.Shapes;

import org.joml.Vector2f;

import Utility.Vector;

/**
 * Container class for collision and mesh data
 * 
 * @author Hanzen Shou
 *
 */
public abstract class Shape {

	// TODO: Rather than having these IDs refer to a hashmap, why don't we
	// enumerate the shapes within the enumerator?

	public static enum ShapeEnum {
		//@formatter:off
		SQUARE(new ShapeRect()), 
		TRIANGLE_BL(new ShapeRightTriangle(0)),
		TRIANGLE_BR(new ShapeRightTriangle(1)),
		TRIANGLE_UL(new ShapeRightTriangle(3)),
		TRIANGLE_UR(new ShapeRightTriangle(2)), 
		FINAL(null);
		//@formatter:on

		public Shape v;

		ShapeEnum(Shape v) {
			this.v = v;
		}
	}

	protected Vector2f bl;
	protected Vector2f br;
	protected Vector2f ul;
	protected Vector2f ur;
	public Vector2f[] vertices;
	public Vector2f[] normals; // First edge is between 1st and 2nd points
	protected Vector2f[] triangulatedVertices;

	public Shape() {
		vertices = null;
		triangulatedVertices = null;

		ul = new Vector2f(0, 1);
		ur = new Vector2f(1, 1);
		bl = new Vector2f(0, 0);
		br = new Vector2f(1, 0);
	}

	protected static void pushRotations(int rotations, Vector2f[] points) {
		// Start by populating points with new objects
		for (int i = 0; i < points.length; i++)
			points[i] = new Vector2f(points[i]);

		for (int i = 0; i < rotations; i++) {
			for (int j = 0; j < points.length; j++) {
				Vector2f v = points[j];
				float x = v.x;
				float y = v.y;

				float temp = x;
				x = 1 - y;
				y = temp;

				v.set(x, y);
			}
		}
	}

	public Vector2f[] getRenderVertices(Vector2f dims) {
		Vector2f[] vs = triangulatedVertices.clone();

		// scale to dims
		for (int i = 0; i < vs.length; i++) {
			vs[i] = new Vector2f(vs[i].x * dims.x, vs[i].y * dims.y);
		}
		return vs;
	}

	public Vector2f[] getRenderUVs() {
		Vector2f[] uvs = triangulatedVertices.clone();
		// Flip UVs vertically since opengl UVs are anchored upper left
		for (int i = 0; i < uvs.length; i++) {
			uvs[i] = new Vector2f(uvs[i].x, 1 - uvs[i].y);
		}
		return uvs;
	}

	public void genNormals() {
		normals = new Vector2f[vertices.length];

		for (int i = 0; i < vertices.length; i++) {
			Vector2f p1 = vertices[i];
			Vector2f p2 = vertices[(i + 1 + vertices.length) % vertices.length];

			// Knowing that these points are going counterclockwise, rotate the vector to
			// the right.
			Vector2f n = Vector.rightVector(new Vector2f(p2).sub(p1)).normalize();

			normals[i] = n;
		}
	}

	public int vertexCount() {
		return vertices.length;
	}

	public int renderVertexCount() {
		return triangulatedVertices.length;
	}
}
