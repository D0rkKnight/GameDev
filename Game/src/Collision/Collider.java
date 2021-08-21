package Collision;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.DebugVector;
import Entities.Framework.Entity;
import Utility.Rect;
import Utility.Transformations.ModelTransform;

public class Collider {
	public Vector2f position;
	public float height;
	public float width;
	public Entity owner;

	public Shape shape;
	public ModelTransform localTrans;
	private Vector2f[] verts; // Encapsulated in genVerts

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

		// Override scaling because it's a bit different
		// Pretty janky but I think it works
		Vector3f ownerDiag = new Vector3f(owner.localTrans.scale.m00(), owner.localTrans.scale.m11(),
				owner.localTrans.scale.m22());

		localTrans.scale.set(new Matrix4f().scaleAround(ownerDiag.x, ownerDiag.y, 1, width / 2, height / 2, 0));
		localTrans.scale.scale(width, height, 1);

		// Debugging
		Rect r = new Rect(new Vector2f(1, 1)); // Dimensions of 1, 1 necessary because base shape has those dimensions.
												// Height and width are actual scaling this base shape, so we want to
												// avoid double applying height and width to the values.
		Vector2f center = r.getTransformedCenter(localTrans.genModel());
		Debug.enqueueElement(new DebugVector(new Vector2f(position).add(center), new Vector2f(0, 1), 10, 1));
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
			Vector4f transed = new Vector4f(shape.vertices[i], 0, 1).mul(localModel);
			transed.mul(worldTranslate);
			verts[i] = new Vector2f(transed.x, transed.y);
		}

		return verts;
	}
}
