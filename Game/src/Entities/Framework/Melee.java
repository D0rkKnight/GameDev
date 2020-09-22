package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Wrappers.Color;

/**
 * An attack with physical presence in the game world
 * 
 * @author Hanzen Shou
 *
 */
public class Melee extends Entity implements Collidable {

	Entity owner;
	int alignment;
	Vector2f kbDir;
	Vector2f offset;

	ArrayList<Entity> hitEntities;

	protected Hitbox hitbox;

	public Melee(String ID, Vector2f position, Renderer renderer, String name, Entity owner, Vector2f kbDir) {
		super(ID, position, renderer, name);
		offset = new Vector2f(owner.position.x - position.x, owner.position.y - position.y);
		this.owner = owner;
		if (owner instanceof PhysicsEntity)
			alignment = ((PhysicsEntity) owner).alignment;
		else
			System.err.println("Attack owned by non physic entity?");

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		this.kbDir = kbDir;
		hitEntities = new ArrayList<>();
	}

	@Override
	public Entity createNew(float xPos, float yPos) {
		return new Melee(ID, new Vector2f(xPos, yPos), renderer, name, owner, new Vector2f(kbDir));
	}

	@Override
	public void calculate() {
		controlledMovement();

		transform.genModel();
	}

	@Override
	public void controlledMovement() {
		position.x = owner.position.x - offset.x;
		position.y = owner.position.y - offset.y;

	}

	@Override
	public void onHit(Hitbox otherHb) {
		// Copied straight over from Projectile. TODO: Generalize some sort of solution
		Entity e = otherHb.owner;

		// Hit an enemy
		int oppAlign = Combatant.getOpposingAlignment(alignment);

		// Can only hit each enemy once
		if (e instanceof PhysicsEntity && !hitEntities.contains(e)) {
			if (((PhysicsEntity) e).alignment == oppAlign) {

				// If it's a combatant, do damange and knockback
				if (e instanceof Combatant) {
					Combatant c = (Combatant) e;

					if (!c.isInvuln()) {
						Vector2f kb = new Vector2f(kbDir).mul(2);
						c.knockback(kb, 0.5f, 1f);

						c.hit(10);
					}
				}

				hitEntities.add(e);
			}
		}
	}

	@Override
	public Hitbox getHb() {
		return hitbox;
	}

	@Override
	public void setHb(Hitbox hb) {
		hitbox = hb;
	}

}
