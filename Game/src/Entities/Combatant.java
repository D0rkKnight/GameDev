package Entities;
import Collision.Collider;
import Rendering.Renderer;
import Wrappers.Vector2;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends Entity{
	private Stats stats;
	public Combatant(int ID, Vector2 position, Sprites sprites, Renderer renderer, Stats stats) {
		super(ID, position, sprites, renderer);
		this.stats = stats;
	}
	

	public abstract void hit(Collider collider1, Collider collider2);
	
	public abstract void attack();
	
	//just sets stats.isDying to true
	public abstract void die();

}
