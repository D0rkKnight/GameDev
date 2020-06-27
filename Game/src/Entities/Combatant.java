package Entities;
import Collision.Collider;
import Rendering.Renderer;
import Wrappers.Position;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends Entity{
	protected Stats stats;
	protected float yVelocity;
	protected float xVelocity;
	protected float yAcceleration;
	public Combatant(int ID, Position position, Sprites sprites, Renderer renderer, Stats stats) {
		super(ID, position, sprites, renderer);
		this.stats = stats;
	}
	

	public abstract void hit(Collider collider1, Collider collider2);
	
	public abstract void attack();
	
	//just sets stats.isDying to true
	public abstract void die();

}
