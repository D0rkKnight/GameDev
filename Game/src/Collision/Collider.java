package Collision;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Collision.Collider.COD;
import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import Utility.Transformations.ModelTransform;

public class Collider<T extends COD> {
	public Vector2f position;
	public float height;
	public float width;
	public Entity owner;

	public Shape shape;
	public ModelTransform localTrans;
	private Vector2f[] verts; // Encapsulated in genVerts

	public boolean isActive = true;

	// Collision output data
	public abstract static class COD<T> {
		public abstract T getData(Matrix4f model, Matrix4f wTrans);
	}

	public static class CODVertex extends COD<Vector2f[]> {
		@Override
		public Vector2f[] getData(Matrix4f model, Matrix4f wTrans) {
			return null;
		}
	}

	public static class CODEllipse extends COD<Vector2f> {
		@Override
		public Vector2f getData(Matrix4f model, Matrix4f wTrans) {
			return null;
		}

	}

	// Plan is to specify output format which in turn then determines how the
	// collider returns items.

	public Collider(Entity owner, float width, float height) {
		this(owner, width, height, Shape.ShapeEnum.SQUARE.v);
	}

	public Collider(Entity owner, float width, float height, Shape shape) {
		this.height = height;
		this.width = width;
		this.owner = owner;
		this.shape = shape;

		// Local transformation, applied to vertices for collision.
		localTrans = new ModelTransform();
		verts = new Vector2f[shape.vertices.length];
		if (owner.getPosition() != null)
			this.position = owner.getPosition();

		// Any class with a hitbox MUST implement Collidable
		if (!(owner instanceof Collidable)) {
			new Exception("Owner does not implement Collidable.").printStackTrace();
			System.exit(1);
		}
	}

	public Collider(Collider hb, Entity owner) {
		this(owner, hb.width, hb.height);
	}

	public void update() {
		// Pull data
		position = new Vector2f(owner.getPosition());
		localTrans.setModel(owner.localTrans);
	}

	/**
	 * Special kinds of hitboxes may not propagate the hit to its owner
	 * 
	 * @param hb
	 */
	void hitBy(Collider hb) {
		((Collidable) owner).onColl(hb);
	}

	// Generates vertices in world space
	public Vector2f[] genWorldVerts() {
		// 2 steps: translate locally, then shift to world position.
		Matrix4f localModel = localTrans.genModel();
		Matrix4f worldTranslate = new Matrix4f().setTranslation(new Vector3f(position.x, position.y, 0));

		for (int i = 0; i < verts.length; i++) {
			Vector2f scaledVert = new Vector2f(shape.vertices[i]).mul(width, height);
			Vector4f transed = new Vector4f(scaledVert, 0, 1).mul(localModel);
			transed.mul(worldTranslate);
			verts[i] = new Vector2f(transed.x, transed.y);
		}

		return verts;
	}
}
