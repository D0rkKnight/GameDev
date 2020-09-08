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
import Utility.Arithmetic;
import Utility.Pathfinding;
import Utility.Transformation;
import Utility.Vector;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;
import Wrappers.Stats;

/**
 * Hips and hops
 * 
 * @author Hanzen Shou
 *
 */
public abstract class BouncingEnemy extends Enemy {

	Timer bounceTimer;
	boolean bounceReady;
	int moveDir;

	public BouncingEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		pData.walksUpSlopes = false;

		ai = new Pathfinding();
		bounceReady = true;
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
			hasGravity = true;
			gravity();

			ai.calculatePath(position, target.getPosition(), grid);

			// TODO Auto-generated method stub
			// Point towards the player and move
			Vector2f dir = Vector.dirTo(position, ai.nextNode());
			float movespeed = 0.03f;
			float deltaX = 0f;
			if (dir != null) {
				deltaX = moveDir * movespeed * Time.deltaT();
			}
			if (dir != null && pData.grounded && bounceReady) {
				pData.velo = new Vector2f(deltaX, 0.03f * Time.deltaT());
				onBounce();

				bounceReady = false;

			} else if (pData.grounded) {
				if (bounceTimer == null)
					bounceTimer = new Timer(1000, new TimerCallback() {

						@Override
						public void invoke(Timer timer) {
							// TODO Auto-generated method stub
							bounceReady = true;
						}

					});

				bounceTimer.update();

				moveDir = Arithmetic.sign(dir.x);
				pData.velo.x = Arithmetic.lerp(pData.velo.x, 0, 0.5f);
			} else {
				// Aerial drift
				pData.velo.x = Arithmetic.lerp(pData.velo.x, deltaX, 0.1f);
			}
		} else {
			findTarget();
		}

		if (pData.wasGrounded == false && pData.grounded == true)
			onLanding();
	}

	public abstract void onBounce();

	public abstract void onLanding();
}
