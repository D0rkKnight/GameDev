package Entities;
import Rendering.Shader;
import Wrappers.Position;
import Wrappers.Sprites;

/**
 * superclass for all entities
 * @author Benjamin
 *
 */
public abstract class Entity {
	protected int ID;
	protected Position position;
	protected Sprites sprites;
	protected Shader shader;
	
	public Entity(int ID, Position position, Sprites sprites, Shader shader) {
		this.ID = ID;
		this.position = position;
		this.sprites = sprites;
		this.shader = shader;
	}
	
	//AI of the object, calls animation frame changes, moves, and attacks
	public abstract void calculate();
	
	public abstract void setFrame(int framenum);
	
	public abstract void move();
	
	public abstract void getPosition();
	
	public abstract void render();
	
}
