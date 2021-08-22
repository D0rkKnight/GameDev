package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;

import Collision.Collidable;
import Collision.Collider;
import Debugging.Debug;
import Debugging.DebugVector;
import Entities.Framework.EntityFlag.FlagFactory;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.Animator;
import Graphics.Rendering.Renderer;
import Utility.Rect;
import Utility.Transformations.ModelTransform;
import Wrappers.Color;

/**
 * superclass for all entities entities have to be initialized after
 * construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity {
	protected String ID;
	protected final Vector2f position = new Vector2f();
	public static float gravity = 5f;

	public Renderer renderer; // Null by default
	public Vector2f rendOriginPos;
	public Vector2f entOriginPos;
	public Vector2f rendDims;

	public String name; // TODO: Let createNew specify the name of the entity
	public Vector2f dim;

	public Animator anim;

	public EntityFlag flag;

	// For local transformations. Position/translation is added later.
	public ModelTransform localTrans;

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

		rendOriginPos = new Vector2f();
		entOriginPos = new Vector2f();
		children = new ArrayList<Entity>();
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
			renderer.transform.trans.set(localTrans.trans);
			renderer.transform.rot.set(localTrans.rot);

			// Set scaling
			renderer.transform.scale.identity();

			Vector2f offset = new Vector2f(entOriginPos).sub(rendOriginPos); // Shifts rendered object so that shifted
																				// scaling is applied properly

			renderer.transform.scale.translate(new Vector3f(offset.x, offset.y, 0));
			renderer.transform.scale.mulLocal(localTrans.scale); // Left multiply so the origin offset
																	// is applied first

			Vector2f totalOffset = new Vector2f(position);

			renderer.transform.trans.translate(new Vector3f(totalOffset.x, totalOffset.y, 0));
		}

		if (anim != null)
			anim.update();
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
		// Debugging
		Rect r = new Rect(new Vector2f(dim)); // Use dimensions as base
		Vector2f center = new Vector2f(position).add(r.getTransformedCenter(localTrans.genModel()));

		Debug.enqueueElement(new DebugVector(center, new Vector2f(0, 1), 25, new Color(0, 0, 1, 1), 1));

		return center;
	}

	/**
	 * You can override this with something spicy I guess
	 */
	public void render() {
		if (renderer != null)
			renderer.render();
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
}
