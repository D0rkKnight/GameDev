package Wrappers;

import Entities.PhysicsEntity;

public class Hitbox {
	public float height;
	public float width;
	public PhysicsEntity owner;
	
	public Hitbox(PhysicsEntity owner, float height, float width) {
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
