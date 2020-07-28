package Entities;

import GameController.GameManager;
import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public abstract class Combatant extends Entity {
	protected Stats stats;
	
	//Velocity is handled as always relative to two axises. This is nice for its flexibility.
	public float yVelocity;
	public float xVelocity;
	public Vector2 yDir;
	public Vector2 xDir;
	private Vector2 newXDir;
	private Vector2 newYDir;
	public boolean veloChangePushed;
	protected float yAcceleration;
	
	protected Vector2 moveDelta;
	protected Player p;
	
	public boolean grounded;
	public boolean wasGrounded; //Used exclusively to update physics events, DO NOT TAMPER WITH

	public Combatant(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name);
		this.stats = stats;
		moveDelta = new Vector2(0, 0);
		yDir = new Vector2(0, 1);
		xDir = new Vector2(1, 0);
		p = GameManager.player;
	}

	/**
	 * knockback in 360 degree dirction starting from top, 90 right, 180 bot hit ta
	 * 
	 * @param damage    damage dealth to player
	 * @param direction direction of knockback force (0 to 360, clockwise, starting
	 *                  from top)
	 * @param knockback force of knockback (in float, change of velocity)
	 * 
	 */
	public void hit(int damage, float direction, float knockback) {
		stats.health -= damage;
		// These two whiles make sure degrees is within 0-360
		while (direction > 360) {
			direction -= 360;
		}
		while (direction < 0) {
			direction += 360;
		}
		// this if else accounts for the angle being to the right/left
		if (direction <= 180) {
			xVelocity += knockback * Math.cos(Math.toRadians((90 - direction)));
		} else {
			xVelocity -= knockback * Math.cos(Math.toRadians((270 - direction)));
		}
		// and top/bot
		if (direction < 90 || direction > 270) {
			yVelocity += knockback * Math.cos(Math.toRadians(direction));
		} else {
			yVelocity -= knockback * Math.cos(Math.toRadians(180 - direction));
		}
	}

	@Override
	public void calculate() {
	}
	
	@Override
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
	
	public abstract void attack();

	// just sets stats.isDying to true
	public abstract void die();
	
	
	//Notifies that axises of movement have changed, and that velocities need recalculating.
	public void recordVeloChange(Vector2 newXDir, Vector2 newYDir) {
		this.newXDir = new Vector2(newXDir.x, newXDir.y);
		this.newYDir = new Vector2(newYDir.x, newYDir.y);
		veloChangePushed = true;
		
		System.out.println("Velocity change queued");
	}
	
	//Recalculate velocity
	public void resolveVeloChange() {
		if (!veloChangePushed) {
			System.err.println("Velocity change pushed illegally!");
		}
		
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
		
		//TODO: figure out what is wrong
		
		xDir = new Vector2(newXDir.x, newXDir.y);
		yDir = new Vector2(newYDir.x, newYDir.y);
		
		System.out.println("World Velocity: "+worldVelo.toString());
		System.out.println("New X Direction: "+newXDir.toString());
		System.out.println("New Y Direction: "+newYDir.toString());
		System.out.println("New magnitudes (X, Y): "+xVelocity+", "+yVelocity);
		
		veloChangePushed = false;
		
		System.out.println("Velocity change resolved");
	}
}
