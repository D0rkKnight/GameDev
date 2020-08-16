package Entities;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.PhysicsCollisionBehavior;
import Collision.PhysicsCollisionBehaviorDeflect;
import Collision.PhysicsCollisionBehaviorStepUp;
import Collision.PhysicsCollisionBehaviorWallCling;
import Math.Vector;
import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.PhysicsData;

public abstract class PhysicsEntity extends Entity implements Collidable{
	
	//Velocity is handled as always relative to two axises. This is nice for its flexibility.
	public PhysicsData pData;
	public Vector2f queuedTangent;
	//public boolean wasGrounded; //Used exclusively to update physics events, DO NOT TAMPER WITH
	//WAS TAMPERED WITH
	
	
	//For now, presume that if one of these behaviors trigger, the following behavior are canceled.
	public ArrayList<PhysicsCollisionBehavior> groundedCollBehaviorList;
	public ArrayList<PhysicsCollisionBehavior> nonGroundedCollBehaviorList;
	
	public PhysicsEntity(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name) {
		super(ID, position, sprites, renderer, name);
		
		pData = new PhysicsData();
		pData.moveDelta = new Vector2f(0, 0);
		pData.velo = new Vector2f(0, 0);
		pData.yDir = new Vector2f(0, 1);
		pData.xDir = new Vector2f(1, 0);
		pData.isJumping = false;
		pData.collidedWithTile = false;
		pData.height = dim.y;
		pData.width = dim.x;
		pData.canBeGrounded = true;
		
		initPhysicsCollBehavior();
	}
	
	protected void initPhysicsCollBehavior() {
		groundedCollBehaviorList = new ArrayList();
		nonGroundedCollBehaviorList = new ArrayList();
		
		//TODO: Array named poorly, distinction isn't that the character isn't grounded.
		nonGroundedCollBehaviorList.add(new PhysicsCollisionBehaviorStepUp());
		nonGroundedCollBehaviorList.add(new PhysicsCollisionBehaviorWallCling());
		nonGroundedCollBehaviorList.add(new PhysicsCollisionBehaviorDeflect());
	}
	
	/**
	 * Applies deltas
	 */
	public void pushMovement() {
		//Some self configuration
		//Make velo modify pos
		position.x += pData.moveDelta.x;
		position.y += pData.moveDelta.y;
		
		//Do a reset
		pData.moveDelta.x = 0f;
		pData.moveDelta.y = 0f;
	}

	public void setMoveDelta(Vector2f d) {
		pData.moveDelta = d;
	}
	
	//Notifies that axises of movement have changed, and that velocities need recalculating.
	public void recordVeloChange(Vector2f newXDir, Vector2f newYDir) {
		pData.newXDir = new Vector2f(newXDir);
		pData.newYDir = new Vector2f(newYDir);
		pData.veloChangeQueued = true;
	}
	
	//Recalculate velocity, retaining velocity.
	public void resolveVeloChange() {
		if (!pData.veloChangeQueued) {
			System.err.println("Velocity change pushed illegally!");
		}
		
		//Aerial collisions are resolved through retaining velocity
			
		//Begin by summing velocity components into world space.
		float worldX = pData.xDir.x * pData.velo.x + pData.yDir.x * pData.velo.y;
		float worldY = pData.xDir.y * pData.velo.x + pData.yDir.y * pData.velo.y;
		
		Vector2f worldVelo = new Vector2f(worldX, worldY);
		
		
		//Now break this vector into new components
		Vector2f newXVelo = new Vector2f(0, 0);
		Vector2f newYVelo = new Vector2f(0, 0);
		float[] magBuff = new float[2];
		Vector.breakIntoComponents(worldVelo, pData.newXDir, pData.newYDir, newXVelo, newYVelo, magBuff);
		
		pData.velo.x = magBuff[0];
		pData.velo.y = magBuff[1];
		
		pData.xDir = new Vector2f(pData.newXDir.x, pData.newXDir.y);
		pData.yDir = new Vector2f(pData.newYDir.x, pData.newYDir.y);
		
		pData.veloChangeQueued = false;
	}
	
	//Force a directional change, without changing velocity coordinates.
	public void forceDirectionalChange(Vector2f newX, Vector2f newY) {
		pData.xDir = new Vector2f(newX);
		pData.yDir = new Vector2f(newY);
	}
	
	//Collision events
	public void onTileCollision() {}
}
