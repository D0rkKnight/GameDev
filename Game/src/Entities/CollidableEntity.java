package Entities;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Rendering.Renderer;

public abstract class CollidableEntity extends Entity implements Collidable{
	
	public Hitbox hitbox;
	
	public CollidableEntity(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		// TODO Auto-generated constructor stub
	}

}
