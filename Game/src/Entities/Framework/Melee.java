package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.Shapes.Shape;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;

/**
 * An attack with physical presence in the game world
 * 
 * @author Hanzen Shou
 *
 */
public class Melee extends Entity implements Collidable {
	PhysicsEntity.Alignment alignment;
	Vector2f kbDir;
	Vector2f offset;

	ArrayList<Entity> hitEntities;

	protected Hitbox hitbox;

	public Timer lifeTimer;

	public Melee(String ID, Vector2f position, String name, Entity owner, Vector2f kbDir, long life) {
		super(ID, position, name);
		offset = new Vector2f(owner.position.x - position.x, owner.position.y - position.y);
		owner.setAsChild(this);
		if (owner instanceof PhysicsEntity)
			alignment = ((PhysicsEntity) owner).alignment;
		else
			System.err.println("Attack owned by non physic entity?");

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		System.out.println(hitbox.owner);

		this.kbDir = kbDir;
		hitEntities = new ArrayList<>();

		lifeTimer = new Timer(life, new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				Destroy();
				lifeTimer = null;
			}
		});

		// Set parentage
	}

	@Override
	public void calculate() {
		super.calculate();

		if (lifeTimer != null)
			lifeTimer.update();

		controlledMovement();

		transform.genModel();
	}

	@Override
	public void updateChildren() {
		super.updateChildren();

		hitbox.update();
	}

	@Override
	public void controlledMovement() {
		position.x = parent.position.x - offset.x;
		position.y = parent.position.y - offset.y;

	}

	@Override
	public void onHit(Hitbox otherHb) {
		// Copied straight over from Projectile. TODO: Generalize some sort of solution
		Entity e = otherHb.owner;

		// Hit an enemy
		PhysicsEntity.Alignment oppAlign = Combatant.getOpposingAlignment(alignment);

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
