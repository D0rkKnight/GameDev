package Wrappers;

import Entities.Combatant;
import Entities.Entity;

public class Hitbox {
	public float height;
	public float width;
	public Combatant owner;
	
	public Hitbox(Combatant owner, float height, float width) {
		this.height = height;
		this.width = width;
		this.owner = owner;
	}
}
