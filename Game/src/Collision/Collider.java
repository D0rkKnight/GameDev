package Collision;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Collision.Collider.COD;
import Collision.Shapes.Shape;
import Entities.Framework.Centered;
import Entities.Framework.Entity;
import Entities.PlayerPackage.Player;
import Utility.Ellipse;
import Utility.Geometry;
import Utility.Rect;
import Utility.Transformations.ModelTransform;

// CODVertex for testing
public class Collider<T extends COD<?>> implements Centered {
	public Vector2f pos;
	public Vector2f offset;

	public Entity owner;
	public ModelTransform localTrans;

	public boolean isActive = true;
	private T cod;

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

//		public boolean hits(COD<?> oCOD, CrossCollisionCB test) {
//
//		}
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
			Matrix4f l2w = owner.genChildL2WMat();

			for (int i = 0; i < verts.length; i++) {
				Vector2f scaledVert = new Vector2f(shape.vertices[i]).mul(width, height);
				Vector4f wp = new Vector4f(scaledVert, 0, 1).mul(l2w);
				verts[i] = new Vector2f(wp.x, wp.y);
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
			Matrix4f model = owner.genChildL2WMat();
			Vector3f vp3 = new Vector3f();
			model.getTranslation(vp3);

			Vector2f pos = new Vector2f(vp3.x, vp3.y);

			Vector3f s = new Vector3f();
			model.getScale(s);

			return new Ellipse(pos, new Vector2f(s.x * width, s.y * height)); // Hack :/
		}

		public CODCircle clone() {
			return new CODCircle(width, height);
		}
	}

	public Collider(Entity owner, COD<?> cod) {
		this(new Vector2f(), owner, cod);
	}

	// Compound collider

	// Plan is to specify output format which in turn then determines how the
	// collider returns items.
	public Collider(Vector2f pos, Entity owner, COD<?> cod) {
		this.owner = owner;
		this.cod = (T) cod; // This is ok. Dunno why Eclipse can't resolve it.
		this.cod.owner = this;
		this.pos = pos;

		// Any class with a hitbox MUST implement Collidable
		if (!(owner instanceof Collidable)) {
			new Exception("Owner does not implement Collidable.").printStackTrace();
			System.exit(1);
		}

		this.owner = owner;

		// Local transformation, applied to vertices for collision.
		localTrans = new ModelTransform();
	}

	public static CrossCollisionCB[][] collMap = new CrossCollisionCB[2][2]; // Needs to be fully populated
	public static ArrayList<Class<?>> registeredCODs = new ArrayList<>();

	public static void buildCrossCollMap() {
		registeredCODs.add(CODVertex.class);
		registeredCODs.add(CODCircle.class);

		CrossCollisionCB vert2vert = (COD<?> c1, COD<?> c2) -> {
			Vector2f[] c1p = ((CODVertex) c1).getData();
			Vector2f[] c2p = ((CODVertex) c2).getData();

			return Geometry.separateAxisCheck(c1p, c2p, null, null, null);
		};

		CrossCollisionCB vert2circ = (COD<?> c1, COD<?> c2) -> {
			Vector2f[] c1p = ((CODVertex) c1).getData();
			Ellipse e = ((CODCircle) c2).getData();
			Vector2f[] c2p = e.genVerts(10);

			return Geometry.separateAxisCheck(c1p, c2p, null, null, null);
		};

		addCrossColl(CODVertex.class, CODVertex.class, vert2vert);
		addCrossColl(CODVertex.class, CODCircle.class, vert2circ);
	}

	// Build that map
	private static void addCrossColl(Class<?> c1, Class<?> c2, CrossCollisionCB cb) {
		int i1 = registeredCODs.indexOf(c1);
		int i2 = registeredCODs.indexOf(c2);

		collMap[i1][i2] = cb;
	}

	public Collider(Collider hb, Entity owner) {
		this(owner, hb.cod.clone());
	}

	public void update() {
		if (owner instanceof Player) {
			System.out.println("\n________________________\n");
			System.out.println("Loc: " + localTrans.genModel());
			System.out.println("L2W: " + genChildL2WMat());
		}
	}

	/**
	 * Special kinds of hitboxes may not propagate the hit to its owner
	 * 
	 * @param hb
	 */
	void hitBy(Collider hb) {
		((Collidable) owner).onColl(hb);
	}

	public Matrix4f genChildL2WMat() {
		// Generate local to parent space matrix
		ModelTransform lMat = new ModelTransform(localTrans);

		// Apply positional translation while still in local space (left side positional
		// translation)
		lMat.trans.translate(new Vector3f(pos.x, pos.y, 0));

		// Reify L2P (local to parent) space matrix
		Matrix4f l2p = lMat.genModel();

		// Assign output matrix
		Matrix4f o = l2p;

		// Left side L2W mult
		if (owner != null) {
			// Multiply recursively
			o = owner.genChildL2WMat().mul(l2p);
		}

		// Right side anchor shift multiplication
		Matrix4f anchorTrans = new Matrix4f().translate(offset.x, offset.y, 0);
		o.mul(anchorTrans);
		// No origin, just assume anchored at (0, 0)

		return o;
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

	@Override
	public Vector2f getCenter() {
		Rect r = new Rect(new Vector2f(cod.width, cod.height)); // Use dimensions as base
		Vector2f center = r.getTransformedCenter(genChildL2WMat());

		return center;
	}
}
