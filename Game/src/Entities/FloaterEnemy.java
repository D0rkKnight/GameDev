package Entities;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Behaviors.PhysicsCollisionBehavior;
import Collision.Behaviors.PhysicsCollisionBehaviorDeflect;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Framework.Enemy;
import GameController.Time;
import GameController.World;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Tiles.Tile;
import Utility.Pathfinding;
import Utility.Transformation;
import Utility.Vector;
import Wrappers.Color;
import Wrappers.Stats;

public class FloaterEnemy extends Enemy {

	public FloaterEnemy(String ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HShapeEnum.SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		pData.walksUpSlopes = false;
		ai = new Pathfinding();
	}

	@Override
	public FloaterEnemy createNew(float xPos, float yPos, Stats stats) {
		return new FloaterEnemy(ID, new Vector2f(xPos, yPos), renderer, name, stats);
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
