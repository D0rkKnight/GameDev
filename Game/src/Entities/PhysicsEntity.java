package Entities;

import Collision.Collidable;
import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Vector2;

public abstract class PhysicsEntity extends Entity implements Collidable{
	
	//Velocity is handled as always relative to two axises. This is nice for its flexibility.
	public float yVelocity;
	public float xVelocity;
	public Vector2 yDir;
	public Vector2 xDir;
	protected Vector2 newXDir;
	protected Vector2 newYDir;
	public boolean veloChangeQueued;
	public Vector2 queuedTangent;
	
	protected float yAcceleration;
	public boolean isJumping;
	
	protected Vector2 moveDelta;
	
	public boolean grounded;
	public boolean wasGrounded; //Used exclusively to update physics events, DO NOT TAMPER WITH
	public boolean collidedWithTile;
	
	public PhysicsEntity(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name) {
		super(ID, position, sprites, renderer, name);
		
		moveDelta = new Vector2(0, 0);
		yDir = new Vector2(0, 1);
		xDir = new Vector2(1, 0);
		isJumping = false;
		collidedWithTile = false;
	}
	
	/**
	 * Applies deltas
	 */
	public void pushMovement() {
		//Some self configuration
		//Make velo modify pos
		position.x += moveDelta.x;
		position.y += moveDelta.y;
		
		//Do a reset
		moveDelta.x = 0f;
		moveDelta.y = 0f;
	}

	public void setMoveDelta(Vector2 d) {
		moveDelta = d;
	}
	
	//Notifies that axises of movement have changed, and that velocities need recalculating.
	public void recordVeloChange(Vector2 newXDir, Vector2 newYDir) {
		this.newXDir = new Vector2(newXDir.x, newXDir.y);
		this.newYDir = new Vector2(newYDir.x, newYDir.y);
		veloChangeQueued = true;
	}
	
	//Recalculate velocity, retaining velocity.
	public void resolveVeloChange() {
		if (!veloChangeQueued) {
			System.err.println("Velocity change pushed illegally!");
		}
		
		//Aerial collisions are resolved through retaining velocity
			
		//Begin by summing velocity components into world space.
		float worldX = xDir.x * xVelocity + yDir.x * yVelocity;
		float worldY = xDir.y * xVelocity + yDir.y * yVelocity;
		
		Vector2 worldVelo = new Vector2(worldX, worldY);
		
		
		//Now break this vector into new components
		Vector2 newXVelo = new Vector2(0, 0);
		Vector2 newYVelo = new Vector2(0, 0);
		float[] magBuff = new float[2];
		worldVelo.breakIntoComponents(newXDir, newYDir, newXVelo, newYVelo, magBuff);
		
		xVelocity = magBuff[1];
		yVelocity = magBuff[0];
		
		xDir = new Vector2(newXDir.x, newXDir.y);
		yDir = new Vector2(newYDir.x, newYDir.y);
		
		veloChangeQueued = false;
	}
	
	//Force a directional change, without changing velocity coordinates.
	public void forceDirectionalChange(Vector2 newX, Vector2 newY) {
		xDir = new Vector2(newX);
		yDir = new Vector2(newY);
	}
	
	//Collision events
	public void onTileCollision() {}
}
