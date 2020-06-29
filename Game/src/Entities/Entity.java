package Entities;
import Rendering.Renderer;
import Wrappers.Rect;
import Wrappers.Sprites;
import Wrappers.Vector2;

/**
 * superclass for all entities
 * @author Benjamin
 *
 */
public abstract class Entity {
	protected int ID;
	protected Vector2 position;
	protected Sprites sprites;
	protected Renderer renderer;
	
	public Rect dim;
	
	public Entity(int ID, Vector2 position, Sprites sprites, Renderer renderer) {
		this.ID = ID;
		this.position = position;
		this.sprites = sprites;
		this.renderer = renderer;
	}
	
	//AI of the object, calls animation frame changes, moves, and attacks
	public abstract void calculate();
	
	public abstract void setFrame(int framenum);
	
	public abstract void move();
	
	public Vector2 getPosition() {
		return position;
	}
	
	public abstract void render();
	
}
