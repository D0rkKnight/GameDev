package Collision;

import Entities.CollidableEntity;

public class Hitbox {
	public float height;
	public float width;
	public CollidableEntity owner;
	
	public Hitbox(CollidableEntity owner, float height, float width) {
		this.height = height;
		this.width = width;
		this.owner = owner;
	}
	
	/**
	 * Special kinds of hitboxes may not propagate the hit to its owner
	 * @param hb
	 */
	public void hitBy(Hitbox hb) {
		owner.onHit(hb);
	}
}
