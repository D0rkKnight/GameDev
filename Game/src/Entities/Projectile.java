package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Sprites;

public class Projectile extends PhysicsEntity{

	public Projectile(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name) {
		super(ID, position, sprites, renderer, name);
		// TODO Auto-generated constructor stub
		
		//Configure renderer
		dim = new Vector2f(8f, 8f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer;
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 1, 0));
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
	}

	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
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
