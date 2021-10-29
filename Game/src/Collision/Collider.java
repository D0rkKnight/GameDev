package Collision;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Collision.Collider.COD;
import Collision.Shapes.Shape;
import Entities.Framework.Centered;
import Entities.Framework.Entity;
import Utility.Center;
import Utility.Ellipse;
import Utility.Transformations.ModelTransform;
import Utility.Transformations.Transformed;

// CODVertex for testing
public class Collider<T extends COD<?>> implements Centered, Transformed {
	public Vector2f position;
	public Entity owner;
	public ModelTransform localTrans;

	public boolean isActive = true;
	private T cod;

	protected Center center;

	// Collision output data
	public abstract static class COD<T> implements Cloneable {
		public float height;
		public float width;
		public Collider owner;

		public COD(float width, float height) {
			this.width = width;
			this.height = height;
		}

		public abstract T getData();

		public abstract COD<?> clone();
	}

	public static class CODVertex extends COD<Vector2f[]> {
		public Shape shape;
		private Vector2f[] verts; // Encapsulated in genVerts

		public CODVertex(float width, float height, Shape shape) {
			super(width, height);
			this.shape = shape;

			verts = new Vector2f[shape.vertices.length];
		}

		public CODVertex(float width, float height) {
			this(width, height, Shape.ShapeEnum.SQUARE.v);
		}

		@Override
		public Vector2f[] getData() {
			// 2 steps: translate locally, then shift to world position.
			Matrix4f model = owner.localTrans.genModel();
			Vector2f pos = owner.position;

			Matrix4f worldTranslate = new Matrix4f().setTranslation(new Vector3f(pos.x, pos.y, 0));

			for (int i = 0; i < verts.length; i++) {
				Vector2f scaledVert = new Vector2f(shape.vertices[i]).mul(width, height);
				Vector4f transed = new Vector4f(scaledVert, 0, 1).mul(model);
				transed.mul(worldTranslate);
				verts[i] = new Vector2f(transed.x, transed.y);
			}

			return verts;
		}

		public CODVertex clone() {
			return new CODVertex(width, height, shape);
		}
	}

	public static class CODCircle extends COD<Ellipse> {
		public CODCircle(float width, float height) {
			super(width, height);
		}

		public CODCircle(float r) {
			this(r, r);
		}

		// Returns radius and position
		@Override
		public Ellipse getData() {
			Matrix4f model = owner.localTrans.genModel();
			Vector2f pos = owner.position;

			Vector3f s = new Vector3f();
			model.getScale(s);

			return new Ellipse(pos, new Vector2f(s.x * width, s.y * height));
		}

		public CODCircle clone() {
			return new CODCircle(width, height);
		}
	}

	// Plan is to specify output format which in turn then determines how the
	// collider returns items.
	public Collider(Entity owner, COD<?> cod) {
		this.owner = owner;
		this.cod = (T) cod; // This is ok. Dunno why Eclipse can't resolve it.
		this.cod.owner = this;

		// Any class with a hitbox MUST implement Collidable
		if (!(owner instanceof Collidable)) {
			new Exception("Owner does not implement Collidable.").printStackTrace();
			System.exit(1);
		}

		this.owner = owner;

		// Local transformation, applied to vertices for collision.
		localTrans = new ModelTransform();
		if (owner.getPosition() != null)
			this.position = owner.getPosition();

		center = new Center(this);
	}

	public Collider(Collider hb, Entity owner) {
		this(owner, hb.cod.clone());
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
		// Hack

		if (cod instanceof CODVertex)
			return (Vector2f[]) cod.getData();
		if (cod instanceof CODCircle)
			return ((Ellipse) cod.getData()).genVerts(10);

		return null;
	}

	public T getCOD() {
		return cod;
	}

	public Center center() {
		return center;
	}

	@Override
	public Vector2f globalCenter() {
		return new Vector2f(position).add(center.local());
	}

	@Override
	public ModelTransform getLocalTrans() {
		return localTrans;
	}
}
