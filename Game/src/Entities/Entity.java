package Entities;

import org.joml.Vector2f;

import GameController.GameManager;
import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.SpriteSheetSection;
import Wrappers.Sprites;

/**
 * superclass for all entities
 * entities have to be initialized after construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity {
	protected int ID;
	protected Vector2f position;
	protected Sprites sprites;
	static float gravity = 5f; //TODO: Don't forget to fix this
	
	protected Renderer renderer;
	protected String name;
	public Vector2f dim;
	protected int animationGroups;
	protected int currentGroup;
	protected int currentFrame;
	protected SpriteSheetSection[][] frames;
	
	protected Hitbox hitbox;
	
	//TODO: Move these within a physics affected class.

	public Entity(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name) {
		this.ID = ID;
		this.position = position;
		this.sprites = sprites;
		this.renderer = renderer;
		this.name = name;
		try {
			this.renderer = renderer.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract void calculate();

	protected abstract void calcFrame();
	
	/**
	 * Applies AI / controls
	 */
	public abstract void controlledMovement();
	
	/**
	 * Applies deltas
	 */
	public abstract void pushMovement();

	public Vector2f getPosition() {
		return position;
	}
	
	/**
	 * You can override this with something spicy I guess
	 */
	public void render() {
		renderer.transform.setPos(position);
		renderer.render();
	}
	
	public Hitbox getHitbox() {
		return hitbox;
	}
	
	public void Destroy() {
		GameManager.unsubscribeEntity(this);
	}
}
