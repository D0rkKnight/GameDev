package Entities.Framework;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import Collision.Collidable;
import Collision.Collider;
import Entities.Framework.EntityFlag.FlagFactory;
import GameController.GameManager;
import GameController.Input;
import Graphics.Drawer;
import Graphics.Animation.Animator;
import Graphics.Rendering.Renderer;
import Utility.Rect;
import Utility.Transformations.ModelTransform;

/**
 * superclass for all entities entities have to be initialized after
 * construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity implements Collidable, Centered {
	protected String ID;
	protected final Vector2f position = new Vector2f();
	public static float gravity = 5f;

	public Renderer renderer; // Null by default
	public Vector2f entOriginPos;
	public Vector2f rendDims;

	public String name;
	public Vector2f dim;

	public Animator anim;

	public EntityFlag flag;

	// For local transformations. Position/translation is added later.
	public ModelTransform localTrans;
	public ArrayList<Collider> colls;

	public ArrayList<Entity> children;
	private ArrayList<Entity> unsubscribeList = new ArrayList<>();
	public Entity parent;

	public Entity(String ID, Vector2f position, String name) {
		this.ID = ID;
		if (position != null) {
			this.position.set(position);
			localTrans = new ModelTransform(); // View/Proj matrices are unimportant
		}
		this.name = name;

		entOriginPos = new Vector2f();
		children = new ArrayList<Entity>();

		colls = new ArrayList<Collider>();
	}

	public void calculate() {

	}

	public void updateChildren() {
		// TODO: Update children?
		// Where are children even being set...
		for (Entity e : unsubscribeList) {
			children.remove(e);
		}
		unsubscribeList.clear();

		for (Entity e : children) {
			e.calculate();
			e.updateChildren();
		}
	}

	public void removeChild(Entity e) {
		unsubscribeList.add(e);
	}

	public void calcFrame() {
		// Let's not rotate around a point yet
		if (renderer != null) {
			// TODO: Remove hack
			renderer.parent = this;

//			if (this instanceof Player) {
//				System.out.println("\n________________________\n");
//				System.out.println("Loc: " + renderer.localTrans.genModel());
//				System.out.println("L2W: " + renderer.genL2WMat());
//				System.out.println("W2S: " + renderer.worldToScreen.genModel());
//			}
		}

		if (anim != null)
			anim.update();
	}

	/**
	 * Produces a matrix that converts children coordinates into their world space
	 * counterparts
	 * 
	 * Top level entity
	 * 
	 * @return
	 */
	public Matrix4f genChildL2WMat() {

		// Generate local to parent space matrix
		ModelTransform lMat = new ModelTransform(localTrans);

		// Apply positional translation while still in local space (left side positional
		// translation)
		lMat.trans.translate(new Vector3f(position.x, position.y, 0));

		// Reify L2P (local to parent) space matrix
		Matrix4f l2p = lMat.genModel();

		// Assign output matrix
		Matrix4f o = l2p;

		// Left side L2W mult
		if (parent != null) {
			// Multiply recursively
			o = parent.genChildL2WMat().mul(l2p);
		}

		// Right side anchor shift mult
		Matrix4f anchorTrans = new Matrix4f().translate(entOriginPos.x, entOriginPos.y, 0);
		o.mul(anchorTrans);

		return o;
	}

	/**
	 * Applies AI / controls
	 */
	public void controlledMovement() {

	}

	public Vector2f getPosition() {
		return position;
	}

	public Vector2f getCenter() {
		Rect r = new Rect(new Vector2f(dim)); // Use dimensions as base
		Vector2f center = new Vector2f(position).add(r.getTransformedCenter(localTrans.genModel()));

		return center;
	}

	/**
	 * You can override this with something spicy I guess
	 */
	public void render() {
		if (renderer != null) {
			Drawer.setCurrBuff(Drawer.DBEnum.MAIN);
			renderer.render();
		}
	}

	public void Destroy() {
		GameManager.unsubscribeEntity(this);

		if (parent != null)
			parent.removeChild(this);
	}

	public void onGameLoad() {

	}

	public boolean mouseHovered() {
		try {
			if (Input.mouseWorldPos.x > position.x && Input.mouseWorldPos.x < position.x + dim.x
					&& Input.mouseWorldPos.y > position.y && Input.mouseWorldPos.y < position.y + dim.y) {
				return true;
			}
			return false;
		} catch (NullPointerException E) {
			System.err.println("Nullpointer Error in Button, First cycle?");
			return false;
		}
	}

	public void flagEntity(FlagFactory iflagfactory) {
		this.flag = iflagfactory.create(new Vector2f(position).add(0, 50f));
		GameManager.subscribeEntity(this.flag);
	}

	public void deflagEntity() {
		flag.Destroy();
		flag = null;
	}

	public void setAsChild(Entity c) {
		c.parent = this;
		children.add(c);
	}

	public void unsubSelf(ArrayList<Entity> subList, ArrayList<Collider> coll) {
		subList.add(this);

		if (this instanceof Collidable) {
			Collidable thisC = (Collidable) this;
			for (Collider c : thisC.getColl()) {
				coll.remove(c);
			}
		}

		for (Entity e : children) {
			e.unsubSelf(subList, coll);
		}
	}

	@Override
	public ArrayList<Collider> getColl() {
		return colls;
	}

	@Override
	public void addColl(Collider hb) {
		if (!colls.contains(hb))
			colls.add(hb);
		else {
			System.err.println("Duplicate collider assignment");
		}
	}

	@Override
	public void remColl(Collider hb) {
		colls.remove(hb);
	}

	@Override
	public void onColl(Collider otherHb) {

	}
}
