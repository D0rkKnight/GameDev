package Entities;
import Collision.Collider;
import Shaders.Shader;
import Wrappers.Position;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends Entity{
	private Stats stats;
	public Combatant(int ID, Position position, Sprites sprites, Shader shader, Stats stats) {
		super(ID, position, sprites, shader);
		this.stats = stats;
	}
	

	public abstract void hit(Collider collider1, Collider collider2);
	
	public abstract void attack();
	
	//just sets stats.isDying to true
	public abstract void die();

}
