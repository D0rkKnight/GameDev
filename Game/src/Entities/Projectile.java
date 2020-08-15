package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Sprites;

public class Projectile extends PhysicsEntity{

	public Projectile(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		// TODO Auto-generated constructor stub
		
		//Configure renderer
		dim = new Vector2f(8f, 8f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer;
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 1, 0));
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
	}

	public void onHit(Hitbox otherHb) { //upon colliding with another hitbox
		PhysicsEntity e = otherHb.owner;
		
		//Hit an enemy
		if (e.alignment == ALIGNMENT_ENEMY) {
			Vector2f kb = new Vector2f(velo).mul(0.2f);
			e.knockback(kb, 0.5f, 1f);
			
			//DESTROY
			Destroy();
		}
	}

	public void calculate() {
		// TODO Auto-generated method stub
	}

	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	public void controlledMovement() {
		// TODO Auto-generated method stub
		
	}

	public void onTileCollision() {
		Destroy();
	}
}
