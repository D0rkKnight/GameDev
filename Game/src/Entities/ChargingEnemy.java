package Entities;

import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class ChargingEnemy extends Enemy implements Gravity {
	protected boolean charging;// currently charging or no. can be set to
	protected boolean windup; // currently winding up for charge or no.
	protected int windupCycles; // animation cycles it should go through before charging
	protected int cooldownCycles; // animation cycles it should go through after charging
	protected int windupNum = 0; // counter
	protected int cooldownNum = 0; // counter

	public ChargingEnemy(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats,
			boolean charging, int windupCycles, int cooldownCycles) {
		super(ID, position, sprites, renderer, name, stats);
		this.charging = charging;
		windup = false; // enemies either charge upon entering room or are idle
		this.windupCycles = windupCycles;
		this.cooldownCycles = cooldownCycles;
	}

	// This enemy attacks only by charging towards the player. No attack function,
	// damage is strictly from collider.
	@Override
	public void attack() {
		// empty

	}

	@Override
	public void die() {
		// TODO Auto-generated method stub

	}

	@Override
	public void calculate() {
		calcFrame();
		if(currentGroup == 3) { //winddown
			
		}
		else if(charging) { //currently charging
			controlledMovement();
		}
		else if(/*player nearby*/true) { //player nearby
			windup = true; //start charge windup
		}
		// only charges at player if knocked back or moved. has a windup animation
		// before and a post charge animation.
		else {
			controlledMovement(); //idle move
		}
	}

	@Override
	public void controlledMovement() { //move mostly while charging. Otherwise idle movement.
		if(charging) {
		}

	}
	@Override
	public void gravity() {
		if(true) { //player not colliding with ground
			//Set this to universal gravitational constant
			yAcceleration = Entity.gravity;
			
			yVelocity -= yAcceleration;
			yVelocity = Math.max(yVelocity, -3);
		}
		else {
			//player colliding with ground without vertical input detected
		}
		
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	/**
	 * note that there should be 4 animation groups
	 * 
	 * 1: idle 2: windup 3: charging 4: cooldown
	 * 
	 * calcFrames also sets charging and windup to true/false;
	 */
	protected void calcFrame() {
		if (charging) {
			if (currentGroup == 2) { // charging
				currentFrame++;
				if (currentFrame >= frames[currentGroup].length - 1) { // loop animation
					currentFrame = 0;
				}
			}
		} else if (windup) {
			if (currentGroup == 0) { // idle
				currentGroup = 1; // change to windup
				currentFrame = 0;
			} else if (currentGroup == 1) { // windup
				currentFrame++;
				if (currentFrame >= frames[currentGroup].length - 1) { // loop animation
					windupNum++;
					currentFrame = 0;
				}
				if (windupNum >= windupCycles) { //completed required amount of loops
					charging = true;
					windup = false;
					currentGroup = 2; // start charging
					currentFrame = 0;
					windupNum = 0;
				}
			}
		} else {
			if (currentGroup == 0) { // idle
				currentFrame++;
				if (currentFrame >= frames[currentGroup].length - 1) { // loop idle animation
					currentFrame = 0;
				}
			} else if (currentGroup == 1) { // change from windup to idle animation
				currentGroup = 0;
				currentFrame = 0;
			} else if (currentGroup == 2) { // change from charge to post charge animation
				currentGroup = 3;
				currentFrame = 0;
			} else if (currentGroup == 3) { // if in post charge animation, continue animation
				currentFrame++;
				if (currentFrame >= frames[currentGroup].length - 1) { // if finished, change to idle
					cooldownNum++;
					currentFrame = 0;
				}
				if (cooldownNum >= cooldownCycles) {
					currentGroup = 0;
					currentFrame = 0;
					cooldownNum = 0;

				}
			}
		}

	}

	@Override
	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}
}
