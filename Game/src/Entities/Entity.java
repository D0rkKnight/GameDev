package Entities;

import Collision.Collidable;
import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Rect;
import Wrappers.SpriteSheetSection;
import Wrappers.Sprites;
import Wrappers.Vector2;

/**
 * superclass for all entities
 * entities have to be initialized after construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity implements Collidable{
	protected int ID;
	protected Vector2 position;
	protected Sprites sprites;
	static float gravity = 0.1f; //TODO: Don't forget to fix this
	
	protected Renderer renderer;
	protected String name;
	public Rect dim;
	protected int animationGroups;
	protected int currentGroup;
	protected int currentFrame;
	protected SpriteSheetSection[][] frames;
	
	protected Hitbox hitbox;
	
	//TODO: Move these within a physics affected class.

	public Entity(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name) {
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
	public abstract void move();
	
	/**
	 * Applies deltas
	 */
	public abstract void pushMovement();

	public Vector2 getPosition() {
		return position;
	}

	public abstract void render();
	
	public Hitbox getHitbox() {
		return hitbox;
	}

}
