package Entities;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Behaviors.PCBDeflect;
import Collision.Shapes.Shape;
import Entities.Framework.Enemy;
import GameController.Time;
import GameController.World;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import Utility.Arithmetic;
import Utility.Pathfinding;
import Utility.Vector;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.FrameSegment;
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

	protected FrameData aggroFD;

	public BouncingEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		pData.walksUpSlopes = false;

		ai = new Pathfinding();
		bounceReady = true;

		initFD();
		setEntityFD(aggroFD);
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();

		collBehaviorList.add(new PCBDeflect());
	}

	private void initFD() {
		ArrayList<FrameSegment> fs = new ArrayList<FrameSegment>();
		FrameSegment aFS = new FrameSegment(10, 0);
		aFS.cbs.add((e) -> {
			BouncingEnemy be = (BouncingEnemy) e;

			be.aggroLoop();
		});

		fs.add(aFS);

		aggroFD = new FrameData(fs, new ArrayList<>(), true);
	}

	protected void aggroLoop() {
		Tile[][] grid = World.currmap.grids.get("coll");

		if (target != null) {
			ai.calculatePath(position, target.getPosition(), grid);

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
	}

	@Override
	public void calculate() {
		super.calculate();

		hasGravity = true;
		// gravity();

		if (pData.wasGrounded == false && pData.grounded == true)
			onLanding();
	}

	public abstract void onBounce();

	public abstract void onLanding();
}
