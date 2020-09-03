package Entities;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Behaviors.PhysicsCollisionBehavior;
import Collision.Behaviors.PhysicsCollisionBehaviorDeflect;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Framework.Enemy;
import GameController.GameManager;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Tiles.Tile;
import Utility.Pathfinding;
import Utility.Transformation;
import Utility.Vector;
import Wrappers.Color;
import Wrappers.Stats;

public class FloaterEnemy extends Enemy {
	
	public FloaterEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		pData.walksUpSlopes = false;
		ai = new Pathfinding();
	}
	
	public FloaterEnemy createNew(float xPos, float yPos, Stats stats) {
		return new FloaterEnemy(ID, new Vector2f(xPos, yPos), renderer, name, stats);
	}
	
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();
		
		PhysicsCollisionBehavior.removeBehavior(collBehaviorList, "groundMove");
		collBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
	}

	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void die() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculate() {
		super.calculate();
		
		Tile[][] grid = GameManager.currmap.grids.get("coll");
		
		if(target != null) {
			ai.calculatePath(position, target.getPosition(), grid); 
			
			// TODO Auto-generated method stub
			// Point towards the player and move
			Vector2f dir = Vector.dirTo(position, ai.nextNode());
			float movespeed = 0.01f;
			if (dir != null) {
				Vector2f target = new Vector2f(dir).mul(movespeed).mul(GameManager.deltaT());
				
				float ratio = 0.1f;
				pData.velo = Vector.lerp(pData.velo, target, ratio);
			}
		}
		else {
			findTarget();
		}
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controlledMovement() {
		// TODO Auto-generated method stub
		
	}









}
