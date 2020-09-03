package Entities.Framework;

import org.joml.Vector2f;
import org.joml.Vector3f;

import GameController.GameManager;
import Graphics.Elements.SpriteSheetSection;
import Graphics.Elements.Sprites;
import Graphics.Rendering.Renderer;
import Utility.CanBeCloned;
import Utility.Transformation;

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
	protected static float gravity = 5f; //TODO: Don't forget to fix this
	
	public Renderer renderer;
	public Vector2f rendOffset;
	
	public String name;
	public Vector2f dim;
	protected int animationGroups;
	protected int currentGroup;
	protected int currentFrame;
	protected SpriteSheetSection[][] frames;
	
	protected boolean hasInit = false;
	
	//For local transformations. Position/translation is added later.
	public Transformation transform;

	public Entity(int ID, Vector2f position, Renderer renderer, String name) {
		this.ID = ID;
		if (position != null) {
			this.position = new Vector2f(position);
			transform = new Transformation(new Vector2f(position)); //View/Proj matrices are unimportant
		}
		this.name = name;
		
		rendOffset = new Vector2f();
		
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
	
	public void updateChildren() {
		renderer.transform.pos.set(position).add(rendOffset);
		
		//Let's not rotate around a point yet
		renderer.transform.rot.set(transform.rot);
		renderer.transform.scale.set(transform.scale);
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
