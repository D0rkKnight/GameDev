package Entities;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.HammerShape;
import Collision.Hitbox;
import Debug.Debug;
import Rendering.Renderer;
import Rendering.GeneralRenderer;
import Rendering.Transformation;
import Wrappers.Color;

/**
 * An attack with physical presence in the game world
 * @author Hanzen Shou
 *
 */
public class Melee extends Entity implements Collidable {
	
	Entity owner;
	int alignment;
	Vector2f kbDir;
	
	ArrayList<Entity> hitEntities;
	
	protected Hitbox hitbox;

	public Melee(int ID, Vector2f position, Renderer renderer, String name, Entity owner, Vector2f kbDir) {
		super(ID, position, renderer, name);
		
		this.owner = owner;
		if (owner instanceof PhysicsEntity) alignment = ((PhysicsEntity) owner).alignment;
		else System.err.println("Attack owned by non physic entity?");
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		
		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		this.kbDir = kbDir;
		hitEntities = new ArrayList<>();
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controlledMovement() {
		// TODO Auto-generated method stub
		
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

	@Override
	public void onHit(Hitbox otherHb) {
		//Copied straight over from Projectile. TODO: Generalize some sort of solution
		Entity e = (Entity) otherHb.owner;
		
		//Hit an enemy
		int oppAlign = Combatant.getOpposingAlignment(alignment);
		
		//Can only hit each enemy once
		if (e instanceof PhysicsEntity && !hitEntities.contains(e)) {
			if (((PhysicsEntity) e).alignment == oppAlign) {
				
				//If it's a combatant, do damange and knockback
				if (e instanceof Combatant) {
					Combatant c = (Combatant) e;
					
					Vector2f kb = new Vector2f(kbDir).mul(2);
					c.knockback(kb, 0.5f, 1f);
					
					c.hit(10);
				}
				
				hitEntities.add(e);
			}
		}
	}

	@Override
	public Hitbox getHb() {
		// TODO Auto-generated method stub
		return hitbox;
	}

	@Override
	public void setHb(Hitbox hb) {
		// TODO Auto-generated method stub
		hitbox = hb;
	}

}
