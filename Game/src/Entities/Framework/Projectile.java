package Entities.Framework;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Shapes.Shape;
import GameController.EntityData;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Wrappers.Color;

public class Projectile extends PhysicsEntity {

	public Projectile(String ID, Vector2f position, String name) {
		super(ID, position, name);

		// Configure renderer (this is a hack)
		dim = new Vector2f(8f, 8f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE, new Color(1, 1, 0, 0));
		this.renderer = rend;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		hasGravity = false;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Projectile(vals.str("type"), pos, vals.str("name"));
	}

	@Override
	public void onHit(Hitbox otherHb) { // upon colliding with another hitbox
		Object e = otherHb.owner;

		// Hit an enemy
		Alignment oppAlign = Combatant.getOpposingAlignment(alignment);

		if (e instanceof PhysicsEntity) {
			if (((PhysicsEntity) e).alignment == oppAlign) {

				// If it's a combatant, do damange and knockback
				if (e instanceof Combatant) {
					Combatant c = (Combatant) e;

					if (!c.isInvuln()) {
						Vector2f kb = new Vector2f(pData.velo).mul(0.2f);
						c.knockback(kb, 0.5f, 1f);

						c.hit(10);
						c.invuln();
					}
				}

				// DESTROY
				Destroy();
			}
		}
	}

	@Override
	public void calculate() {
		super.calculate();

		// gravity();
	}

	@Override
	public void onTileCollision() {
		super.onTileCollision();

		Destroy();
	}
}
