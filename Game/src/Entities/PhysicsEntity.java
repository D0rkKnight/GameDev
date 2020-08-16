package Entities;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.Collidable;
import Collision.PhysicsCollisionBehavior;
import Collision.PhysicsCollisionBehaviorDeflect;
import Collision.PhysicsCollisionBehaviorGroundMove;
import Collision.PhysicsCollisionBehaviorStepUp;
import Collision.PhysicsCollisionBehaviorWallCling;
import GameController.GameManager;
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
	
	
	protected boolean hasGravity;
	
	protected Vector2f knockbackDir; //for debug purposes, only shows initial knockback 
	//knockback only dependent on velocity: reduces effect of movement keys (not completely disabling), reduced velocity every frame, changes to normal movement when at normal velocities
	protected float movementMulti; //multiplier for movement when knocked back (suggest 0.5)
	protected float decelMulti; //multiplier for decel when knocked back (suggest 1)
	protected boolean knockback = true;
	protected int movementMode;
	
	public static final int MOVEMENT_MODE_CONTROLLED = 1;
	public static final int MOVEMENT_MODE_IS_DASHING = 2;
	public static final int MOVEMENT_MODE_KNOCKBACK = 3;
	
	protected int alignment;
	public static final int ALIGNMENT_NEUTRAL = 0;
	public static final int ALIGNMENT_PLAYER = 1;
	public static final int ALIGNMENT_ENEMY = 2;
	
	//For now, presume that if one of these behaviors trigger, the following behavior are canceled.
	public ArrayList<PhysicsCollisionBehavior> groundedCollBehaviorList;
	public ArrayList<PhysicsCollisionBehavior> nonGroundedCollBehaviorList;
	
	public PhysicsEntity(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		
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
		
		groundedCollBehaviorList.add(new PhysicsCollisionBehaviorGroundMove());
		
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
	
	protected void gravity() {
		//Gravity
		if (hasGravity) {
			velo.y -= Entity.gravity * GameManager.deltaT() / 1300;
			velo.y = Math.max(velo.y, -2);
		}
	}
	
	/**
	 * applies knockback values. if currently in knockback state, chooses larger values.
	 * @param knockbackVector
	 * @param movementMulti
	 * @param decelMulti
	 */
	public void knockback(Vector2f knockbackVector, float movementMulti, float decelMulti) {
		if(movementMode == MOVEMENT_MODE_KNOCKBACK) {
			if(Math.abs(velo.x) < Math.abs(knockbackVector.x)) {
				velo.x = knockbackVector.x;
				this.knockbackDir.x = knockbackVector.x;
			}
			if(Math.abs(velo.y) < Math.abs(knockbackVector.y)) {
				velo.y = knockbackVector.y;
				this.knockbackDir.y = knockbackVector.y;
			}
			if(this.movementMulti < movementMulti) this.movementMulti = movementMulti;
			if(this.decelMulti < decelMulti) this.decelMulti = decelMulti;
		}
		else {
			velo.x = knockbackVector.x;
			velo.y = knockbackVector.y;
			this.knockbackDir = new Vector2f(knockbackVector);
			this.movementMulti = movementMulti;
			this.decelMulti = decelMulti;
			knockback = true;
		}
	}
}
