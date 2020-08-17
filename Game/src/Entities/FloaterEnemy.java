package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.PhysicsCollisionBehavior;
import Collision.PhysicsCollisionBehaviorDeflect;
import Collision.PhysicsCollisionBehaviorStepUp;
import Collision.PhysicsCollisionBehaviorWallCling;
import Debug.Debug;
import GameController.GameManager;
import Math.AI;
import Math.Vector;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Tiles.Tile;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Stats;

public class FloaterEnemy extends Enemy{
	
	public FloaterEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
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
	}
	
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();
		
		PhysicsCollisionBehavior.removeBehavior(groundedCollBehaviorList, "groundMove");
		groundedCollBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
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
		
		Tile[][] grid = GameManager.currmap.getGrid();
		
		if(target != null) {
			ai.calculatePath(position, target.position, grid); 
			
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

	@Override
	public Entity clone(float xPos, float yPos) {
		try {
			return new FloaterEnemy(ID, new Vector2f(xPos, yPos), renderer.clone(), name, stats.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Entity clone() {
		try {
			return new FloaterEnemy(ID, new Vector2f(position.x, position.y), renderer.clone(), name, stats.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
