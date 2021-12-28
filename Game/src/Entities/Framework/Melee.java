package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Collider;
import Collision.Collider.CODVertex;
import Collision.Hitbox;
import Collision.Shapes.Shape;
import Entities.Framework.PhysicsEntity.Alignment;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

/**
 * An attack with physical presence in the game world
 * 
 * @author Hanzen Shou
 *
 */
public class Melee extends Entity implements Collidable, Aligned {
	Vector2f kbDir;
	Vector2f offset;
	float kbStrength;

	ArrayList<Entity> hitEntities;

	protected ArrayList<Collider> hb;

	public Timer lifeTimer;

	protected Alignment alignment;

	// TODO: For entities, please just use relative positioning.

	public Melee(String ID, Vector2f position, String name, Entity owner, Vector2f kbDir, float kbStrength, long life,
			Vector2f dim) {
		super(ID, position, name);
		offset = new Vector2f(owner.position.x - position.x, owner.position.y - position.y);

		owner.setAsChild(this);
		if (owner instanceof PhysicsEntity)
			alignment = ((PhysicsEntity) owner).alignment;
		else
			System.err.println("Attack owned by non physic entity?");

		// Configure the renderer real quick
		this.dim = dim;
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), dim, Shape.ShapeEnum.SQUARE, new Color());
		this.renderer = rend;

		// Configure hitbox
		hb = new ArrayList<>();

		localTrans.trans.setTranslation(-dim.x / 2, -dim.y / 2, 0);

		this.kbDir = kbDir;
		this.kbStrength = kbStrength;
		hitEntities = new ArrayList<>();

		lifeTimer = new Timer(life, new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				Destroy();
				lifeTimer = null;
			}
		});

		// Configure hitbox
		Hitbox tempHB = new Hitbox(this, new CODVertex(dim.x, dim.y));
		tempHB.cb = (comb) -> {

			// Copied straight over from Projectile. TODO: Generalize some sort of solution
			// Hit an enemy
			PhysicsEntity.Alignment oppAlign = Combatant.getOpposingAlignment(alignment);

			// Can only hit each enemy once
			if (!hitEntities.contains(comb)) {
				if (comb.alignment == oppAlign) {
					if (!comb.getInvulnState()) {
						Vector2f kb = new Vector2f(kbDir).mul(kbStrength);
						comb.knockback(kb, 0.5f, 1f);

						comb.hit(10);
					}

					hitEntities.add(comb);
				}
			}
		};
		addColl(tempHB);
	}

	public Melee(Vector2f position, Entity owner, Vector2f kbDir, float kbStrength, long life, Vector2f dim) {
		this("MELEE", position, "Melee", owner, kbDir, kbStrength, life, dim);
	}

	@Override
	public void calculate() {
		super.calculate();

		if (lifeTimer != null)
			lifeTimer.update();

		controlledMovement();

		localTrans.genModel();
	}

	@Override
	public void updateChildren() {
		super.updateChildren();

		for (Collider c : hb)
			c.update();
	}

	@Override
	public void controlledMovement() {
		position.x = parent.position.x - offset.x;
		position.y = parent.position.y - offset.y;

	}

	@Override
	public ArrayList<Collider> getColl() {
		return hb;
	}

	@Override
	public void addColl(Collider c) {
		hb.add(c);
	}

	@Override
	public void remColl(Collider c) {
		hb.remove(c);
	}

	@Override
	public Alignment getAlign() {
		return alignment;
	}

	@Override
	public void setAlign(Alignment align) {
		this.alignment = align;
	}

	@Override
	public void onColl(Collider otherHb) {
		// TODO Auto-generated method stub

	}
}
