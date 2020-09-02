package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.Hitbox;
import Rendering.Renderer;
import Utility.Transformation;
import Rendering.GeneralRenderer;
import Wrappers.Color;

public class Projectile extends PhysicsEntity{

	public Projectile(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		// TODO Auto-generated constructor stub
		
		//Configure renderer (this is a hack)
		dim = new Vector2f(8f, 8f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 1, 0, 0));
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		hasGravity = false;
	}
	public Projectile createNew(float xPos, float yPos) {
		return new Projectile(ID, new Vector2f(xPos, yPos), renderer, name);
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
}
