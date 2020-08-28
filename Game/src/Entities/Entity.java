package Entities;

import org.joml.Vector2f;

import Collision.Hitbox;
import GameController.GameManager;
import Rendering.Renderer;
import Wrappers.SpriteSheetSection;
import Wrappers.Sprites;

/**
 * superclass for all entities
 * entities have to be initialized after construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity implements Cloneable {
	protected int ID;
	protected Vector2f position;
	protected Sprites sprites;
	static float gravity = 5f; //TODO: Don't forget to fix this
	
	public Renderer renderer;
	public String name;
	public Vector2f dim;
	protected int animationGroups;
	protected int currentGroup;
	protected int currentFrame;
	protected SpriteSheetSection[][] frames;
	
	//TODO: Move these within a physics affected class.

	public Entity(int ID, Vector2f position, Renderer renderer, String name) {
		this.ID = ID;
		if (position != null) this.position = new Vector2f(position);
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

	public Vector2f getPosition() {
		return position;
	}
	
	/**
	 * You can override this with something spicy I guess
	 */
	public void render() {
		System.out.println(name);
		
		renderer.transform.pos = position;
		renderer.render();
	}
	
	public void Destroy() {
		GameManager.unsubscribeEntity(this);
	}
	
	public Entity clone() {
		try {
			return (Entity) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public Entity clone(float xPos, float yPos) {
		Entity clonedE = clone();
		clonedE.position = new Vector2f(xPos, yPos);
		
		return clonedE;
	}
}
