package Entities;

import Collision.Collidable;
import GameController.Camera;
import GameController.GameManager;
import GameController.Input;
import Rendering.RectRenderer;
import Wrappers.Rect;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class Player extends Combatant {

	public Input input;
	
	public Player(int ID, Vector2 position, Sprites sprites, RectRenderer renderer, Stats stats) {

		super(ID, position, sprites, renderer, stats);
		input = new Input();

		dim = new Rect(32f, 32f);
		this.renderer.init(position, dim);
		this.renderer.linkPos(this.position);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void hit() {
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
		// TODO Auto-generated method stub
		move();
	}

	@Override
	public void setFrame(int framenum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void move() {// TODO add collision
		if (input.moveX < 0 && true) {// moving left, check collision left
			if (xVelocity > -1) {
				xVelocity -= 0.2;
				if (xVelocity < -1) {
					xVelocity = -1;
				}
			}
			position.x += xVelocity;
		} else if (input.moveX > 0 && true) {// moving right, check collision right
			if (xVelocity < 1) {
				xVelocity += 0.2;
				if (xVelocity > 1) {
					xVelocity = 1;
				}
			}
			position.x += xVelocity;
		} else {
			if (xVelocity > 0) {
				xVelocity -= 0.2; // Rate that the player deaccelerates when not moving
				if (xVelocity < 0) {
					xVelocity = 0;
				}
			} else if (xVelocity < 0) {
				xVelocity += 0.2; // Rate that the player deaccelerates when not moving
				if (xVelocity > 0) {
					xVelocity = 0;
				}
			}
		}
		if (true && input.moveY != 0) { // if player is colliding with ground underneath and digital input detected
										// (space pressed)
			yVelocity = 4f;
		}
		else if(true) { //player not colliding with ground
			//Set this to universal gravitational constant
			yAcceleration = Entity.gravity;
			
			yVelocity -= yAcceleration;
			yVelocity = Math.max(yVelocity, -3);
		}
		else {
			//player colliding with ground without vertical input detected
		}
		
		//Make velo modify pos
		position.x += xVelocity * GameManager.deltaT();
		position.y += yVelocity * GameManager.deltaT();
	}

	@Override
	public void render() {
		//TODO: This is like, broken rn because the renderer can't handle movement.
		renderer.render();
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
	/*
	@Override
	public void hit(int damage, float direction, float knockback) {
		stats.health -= damage;
		// These two ifs make sure degrees is within 0-360
		if (direction > 360) {
			direction -= 360;
		}
		if (direction < 0) {
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
	*/

}
