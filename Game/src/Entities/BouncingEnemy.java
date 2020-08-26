package Entities;
import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.Hitbox;
import Collision.PhysicsCollisionBehavior;
import Collision.PhysicsCollisionBehaviorDeflect;
import Debug.Debug;
import GameController.GameManager;
import Math.Pathfinding;
import Math.Arithmetic;
import Math.Vector;
import Rendering.Renderer;
import Rendering.GeneralRenderer;
import Rendering.Transformation;
import Tiles.Tile;
import Wrappers.Color;
import Wrappers.Stats;
import Wrappers.Timer;
import Wrappers.TimerCallback;

/**
 * Hips and hops
 * @author Hanzen Shou
 *
 */
public abstract class BouncingEnemy extends Enemy{
	
	Timer bounceTimer;
	boolean bounceReady;
	int moveDir;

	public BouncingEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		
		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		pData.walksUpSlopes = false;
		
		ai = new Pathfinding();
		bounceReady = true;
	}
	
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();
		
		PhysicsCollisionBehavior.removeBehavior(collBehaviorList, "groundMove");
		collBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
	}
	
	public void calculate() {
		super.calculate();
		
		Tile[][] grid = GameManager.currmap.grids.get("coll");
		
		if (target != null) {
			hasGravity = true;
			gravity();
			
			ai.calculatePath(position, target.position, grid); 
			
			// TODO Auto-generated method stub
			// Point towards the player and move
			Vector2f dir = Vector.dirTo(position, ai.nextNode());
			float movespeed = 0.03f;
			float deltaX = 0f;
			if (dir != null) {
				deltaX = moveDir * movespeed * GameManager.deltaT();
			}
			if (dir != null && pData.grounded && bounceReady) {
				int side = Arithmetic.sign(dir.x);
				
				pData.velo = new Vector2f(deltaX, 0.03f * GameManager.deltaT());
				onBounce();
				
				bounceReady = false;
				
			} else if (pData.grounded) {
				if (bounceTimer == null) bounceTimer = new Timer(1000, new TimerCallback() {

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
				//Aerial drift
				pData.velo.x = Arithmetic.lerp(pData.velo.x, deltaX, 0.1f);
			}
		} else {
			findTarget();
		}
		
		if (pData.wasGrounded == false && pData.grounded == true) onLanding();
	}
	
	public abstract void onBounce();
	public abstract void onLanding();
}
