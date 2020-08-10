package Entities;

import org.joml.Vector2f;

import Collision.Collidable;
import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Vector;

public abstract class PhysicsEntity extends Entity implements Collidable{
	
	//Velocity is handled as always relative to two axises. This is nice for its flexibility.
	public Vector2f velo;
	public Vector2f yDir;
	public Vector2f xDir;
	protected Vector2f newXDir;
	protected Vector2f newYDir;
	public boolean veloChangeQueued;
	public Vector2f queuedTangent;
	
	protected float yAcceleration;
	public boolean isJumping;
	
	protected Vector2f moveDelta;
	
	public boolean grounded;
	public boolean wasGrounded; //Used exclusively to update physics events, DO NOT TAMPER WITH
	public boolean collidedWithTile;
	
	public PhysicsEntity(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name) {
		super(ID, position, sprites, renderer, name);
		
		moveDelta = new Vector2f(0, 0);
		velo = new Vector2f(0, 0);
		yDir = new Vector2f(0, 1);
		xDir = new Vector2f(1, 0);
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

	public void setMoveDelta(Vector2f d) {
		moveDelta = d;
	}
	
	//Notifies that axises of movement have changed, and that velocities need recalculating.
	public void recordVeloChange(Vector2f newXDir, Vector2f newYDir) {
		this.newXDir = new Vector2f(newXDir);
		this.newYDir = new Vector2f(newYDir);
		veloChangeQueued = true;
	}
	
	//Recalculate velocity, retaining velocity.
	public void resolveVeloChange() {
		if (!veloChangeQueued) {
			System.err.println("Velocity change pushed illegally!");
		}
		
		//Aerial collisions are resolved through retaining velocity
			
		//Begin by summing velocity components into world space.
		float worldX = xDir.x * velo.x + yDir.x * velo.y;
		float worldY = xDir.y * velo.x + yDir.y * velo.y;
		
		Vector2f worldVelo = new Vector2f(worldX, worldY);
		
		
		//Now break this vector into new components
		Vector2f newXVelo = new Vector2f(0, 0);
		Vector2f newYVelo = new Vector2f(0, 0);
		float[] magBuff = new float[2];
		Vector.breakIntoComponents(worldVelo, newXDir, newYDir, newXVelo, newYVelo, magBuff);
		
		velo.x = magBuff[0];
		velo.y = magBuff[1];
		
		xDir = new Vector2f(newXDir.x, newXDir.y);
		yDir = new Vector2f(newYDir.x, newYDir.y);
		
		veloChangeQueued = false;
	}
	
	//Force a directional change, without changing velocity coordinates.
	public void forceDirectionalChange(Vector2f newX, Vector2f newY) {
		xDir = new Vector2f(newX);
		yDir = new Vector2f(newY);
	}
	
	//Collision events
	public void onTileCollision() {}
}
