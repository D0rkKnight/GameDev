package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.Hitbox;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Rendering.Transformation;
import Wrappers.Color;

public class Projectile extends PhysicsEntity{

	public Projectile(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		// TODO Auto-generated constructor stub
		
		//Configure renderer
		dim = new Vector2f(8f, 8f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer;
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 1, 0, 0));
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		hasGravity = false;
	}

	public void onHit(Hitbox otherHb) { //upon colliding with another hitbox
		Object e = otherHb.owner;
			
		//Hit an enemy
		int oppAlign = Combatant.getOpposingAlignment(alignment);
		
		if (e instanceof PhysicsEntity) {
			if (((PhysicsEntity) e).alignment == oppAlign) {
				
				//If it's a combatant, do damange and knockback
				if (e instanceof Combatant) {
					Combatant c = (Combatant) e;
					
					Vector2f kb = new Vector2f(pData.velo).mul(0.2f);
					c.knockback(kb, 0.5f, 1f);
					
					c.hit(10);
				}
				
				//DESTROY
				Destroy();
			}
		}
	}

	public void calculate() {
		gravity();
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

	@Override
	public Entity clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity clone(float xPos, float yPos) {
		// TODO Auto-generated method stub
		return null;
	}
}
