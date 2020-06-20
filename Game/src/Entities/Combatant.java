package Entities;
import java.awt.Image;

import Collision.Collider;
import Wrappers.Position;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends Entity{
	private Stats stats;
	public Combatant(int ID, Position position, Sprites sprites, Stats stats) {
		super(ID, position, sprites);
		this.stats = stats;
	}
	

	public abstract void hit(Collider collider1, Collider collider2);
	
	public abstract void attack();
	
	//just sets stats.isDying to true
	public abstract void die();

}
