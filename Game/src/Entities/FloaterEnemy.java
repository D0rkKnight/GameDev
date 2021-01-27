package Entities;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Behaviors.PhysicsCollisionBehavior;
import Collision.Behaviors.PhysicsCollisionBehaviorDeflect;
import Collision.Shapes.Shape;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import GameController.EntityData;
import GameController.Time;
import GameController.World;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import Utility.Pathfinding;
import Utility.Transformation;
import Utility.Vector;
import Wrappers.Color;
import Wrappers.Stats;

public class FloaterEnemy extends Enemy {

	public FloaterEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		pData.walksUpSlopes = false;
		ai = new Pathfinding();
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new FloaterEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();

		PhysicsCollisionBehavior.removeBehavior(collBehaviorList, "groundMove");
		collBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
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
