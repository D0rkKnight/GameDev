package Entities;
import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.PhysicsCollisionBehavior;
import Collision.PhysicsCollisionBehaviorDeflect;
import Debug.Debug;
import GameController.GameManager;
import Math.AI;
import Math.Arithmetic;
import Math.Vector;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Tiles.Tile;
import Wrappers.Color;
import Wrappers.Hitbox;
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

	public BouncingEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		
		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		pData.walksUpSlopes = false;
		
		ai = new AI();
		bounceReady = true;
	}
	
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();
		
		PhysicsCollisionBehavior.removeBehavior(groundedCollBehaviorList, "groundMove");
		groundedCollBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
	}
	
	public void calculate() {
		super.calculate();
		
		Tile[][] grid = GameManager.currmap.getGrid();
		
		if (target != null) {
			hasGravity = true;
			gravity();
			
			ai.calculatePath(position, target.position, grid); 
			
			// TODO Auto-generated method stub
			// Point towards the player and move
			Vector2f dir = Vector.dirTo(position, ai.nextNode());
			float movespeed = 0.03f;
			if (dir != null && pData.grounded && bounceReady) {
				int side = Arithmetic.sign(dir.x);
				
				pData.velo = new Vector2f(side * movespeed * GameManager.deltaT(), 0.03f * GameManager.deltaT());
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
				
				pData.velo.x = Arithmetic.lerp(pData.velo.x, 0, 0.5f);
			}
		} else {
			findTarget();
		}
		
		if (pData.wasGrounded == false && pData.grounded == true) onLanding();
	}
	
	public abstract void onBounce();
	public abstract void onLanding();
}
