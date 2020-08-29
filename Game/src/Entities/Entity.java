package Entities;

import org.joml.Vector2f;

import GameController.GameManager;
import Rendering.Renderer;
import Utility.CanBeCloned;
import Wrappers.SpriteSheetSection;
import Wrappers.Sprites;

/**
 * superclass for all entities
 * entities have to be initialized after construction
 * 
 * @author Benjamin
 *
 */
public abstract class Entity implements CanBeCloned {
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
	
	protected boolean hasInit = false;

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

	public void calculate() {
		if (!hasInit) {
			new Exception("Entity not initialized.").printStackTrace();
		}
	}

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
	
	public Entity createNew() {
		return createNew(position.x, position.y);
	}
	
	public abstract Entity createNew(float xPos, float yPos);
}
