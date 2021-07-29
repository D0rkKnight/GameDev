package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;

import Collision.Collidable;
import Collision.Collider;
import Entities.Framework.EntityFlag.FlagFactory;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.Animator;
import Graphics.Rendering.Renderer;
import Utility.Transformations.ModelTransform;

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
	public Vector2f rendOffset;
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

		rendOffset = new Vector2f();
		children = new ArrayList<Entity>();
	}

	public void calculate() {
		// Let's not rotate around a point yet
		if (renderer != null) {
			renderer.transform.trans.set(localTrans.trans);
			renderer.transform.rot.set(localTrans.rot);
			renderer.transform.scale.set(localTrans.scale);

			Vector2f totalOffset = new Vector2f(position).add(rendOffset);

			renderer.transform.trans.translate(new Vector3f(totalOffset.x, totalOffset.y, 0));
		}
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
			Collider hb = ((Collidable) this).getColl();
			if (hb != null)
				coll.remove(hb);
			else {
				new Exception("Collider of " + name + " not defined!").printStackTrace();
			}
		}

		for (Entity e : children) {
			e.unsubSelf(subList, coll);
		}
	}
}
