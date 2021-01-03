package Entities.Framework;

import org.joml.Vector2f;

import GameController.GameManager;
import Graphics.Animation.Animator;
import Graphics.Rendering.Renderer;
import Utility.CanBeCloned;
import Utility.Transformation;

/**
 * superclass for all entities entities have to be initialized after
 * construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity implements CanBeCloned {
	protected String ID;
	protected Vector2f position;
	protected static float gravity = 5f;

	public Renderer renderer;
	public Vector2f rendOffset;
	public Vector2f rendDims;

	public String name; // TODO: Let createNew specify the name of the entity
	public Vector2f dim;

	protected Animator anim;

	// For local transformations. Position/translation is added later.
	public Transformation transform;

	public Entity(String ID, Vector2f position, Renderer renderer, String name) {
		this.ID = ID;
		if (position != null) {
			this.position = new Vector2f(position);
			transform = new Transformation(new Vector2f(position)); // View/Proj matrices are unimportant
		}
		this.name = name;

		rendOffset = new Vector2f();

		try {
			this.renderer = renderer.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public abstract void calculate();

	public void updateChildren() {
		renderer.transform.pos.set(position).add(rendOffset);

		// Let's not rotate around a point yet
		renderer.transform.rot.set(transform.rot);
		renderer.transform.scale.set(transform.scale);
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
		renderer.render();
	}

	public void Destroy() {
		GameManager.unsubscribeEntity(this);
	}

	@Override
	public Entity createNew() {
		return createNew(position.x, position.y);
	}

	public abstract Entity createNew(float xPos, float yPos);
}
