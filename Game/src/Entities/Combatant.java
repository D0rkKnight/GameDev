package Entities;
import Rendering.RectRenderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public abstract class Combatant extends Entity{
	protected Stats stats;
	protected float yVelocity;
	protected float xVelocity;
	protected float yAcceleration;
	
	public Combatant(int ID, Vector2 position, Sprites sprites, RectRenderer renderer, Stats stats) {
		super(ID, position, sprites, renderer);
		this.stats = stats;
	}
	

	public abstract void hit();
	
	public abstract void attack();
	
	//just sets stats.isDying to true
	public abstract void die();

}
