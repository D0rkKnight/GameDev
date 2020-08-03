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
}
