package Entities;
import Wrappers.Position;
import Wrappers.Sprites;

/**
 * superclass for all entities
 * @author Benjamin
 *
 */
public abstract class Entity {
	private int ID;
	private Position position;
	private Sprites sprites;
	
	public Entity(int ID, Position position, Sprites sprites) {
		this.ID = ID;
		this.position = position;
		this.sprites = sprites;
	}
	
	//AI of the object, calls animation frame changes, moves, and attacks
	public abstract void calculate();
	
	public abstract void setFrame(int framenum);
	
	public abstract void move();
	
	public abstract void getPosition();
	
	public abstract void render();
	
}
