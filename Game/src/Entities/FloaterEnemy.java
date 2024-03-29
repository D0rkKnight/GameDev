package Entities;

import org.joml.Vector2f;

import Collision.Collider.CODVertex;
import Collision.Hitbox;
import Collision.Hurtbox;
import Collision.Behaviors.PCBDeflect;
import Collision.Behaviors.PCBGroundMove;
import Collision.Shapes.Shape;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import Entities.PlayerPackage.PlayerFramework;
import GameController.EntityData;
import GameController.Time;
import GameController.World;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import Utility.Pathfinding;
import Utility.Vector;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class FloaterEnemy extends Enemy {

	public FloaterEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		pData.walksUpSlopes = false;
		ai = new Pathfinding();

		// Configure hurtbox
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		addColl(hurtbox);

		// Configure hitbox
		Hitbox hitbox = new Hitbox(this, new CODVertex(dim.x, dim.y));
		hitbox.cb = (comb) -> {
			if (comb instanceof PlayerFramework) {
				PlayerFramework p = (PlayerFramework) comb;

				if (!p.getInvulnState()) {
					p.hit(10);
					p.knockback(Vector.dirTo(getPosition(), p.getPosition()), 0.5f, 1f);
					p.invuln();
				}
			}
		};

		addColl(hitbox);
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new FloaterEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();

		collBehaviorList.remove(PCBGroundMove.class);
		collBehaviorList.add(new PCBDeflect());
	}

	@Override
	public void calculate() {
		super.calculate();

		Tile[][] grid = World.currmap.grids.get("coll");

		if (target != null) {
			ai.calculatePath(position, target.getPosition(), grid);

			// Point towards the player and move
			Vector2f dir = Vector.dirTo(position, ai.nextNode());
			float movespeed = 0.01f;
			if (dir != null) {
				Vector2f target = new Vector2f(dir).mul(movespeed).mul(Time.deltaT());

				float ratio = 0.1f;
				pData.velo = Vector.lerp(pData.velo, target, ratio);
			}
		} else {
			findTarget();
		}
	}
}
