package Entities;

import Collision.Collider;
import GameController.Input;
import Rendering.RectRenderer;
import Rendering.Renderer;
import Wrappers.Rect;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class Player extends Combatant{
	
	public Input input;
	
	public Player(int ID, Vector2 position, Sprites sprites, Renderer renderer, Stats stats) {
		super(ID, position, sprites, renderer, stats);
		input = new Input();
		
		dim = new Rect(32f, 32f);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void hit(Collider collider1, Collider collider2) {
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
	public void move() {//TODO add collision
		if(input.moveX < 0 && true) {//moving left, check collision left
			if(xVelocity > -1) {
				xVelocity -= 0.2;
				if(xVelocity < -1) {
					xVelocity = -1;
				}
			}
			position.x += xVelocity;
		}
		else if(input.moveX > 0 && true) {//moving right, check collision right
			if(xVelocity < 1) {
				xVelocity += 0.2;
				if(xVelocity > 1) {
					xVelocity = 1;
				}
			}
			position.x += xVelocity;
		}
		else {
			if(xVelocity > 0) {
				xVelocity -= 0.2; //Rate that the player deaccelerates when not moving
				if(xVelocity < 0) {
					xVelocity = 0;
				}
			}
			else if(xVelocity < 0) {
				xVelocity += 0.2; //Rate that the player deaccelerates when not moving
				if(xVelocity > 0) {
					xVelocity = 0;
				}
			}
		}
		if(true && input.moveY != 0) { //if player is colliding with ground underneath and digital input detected (space pressed)
			yVelocity = 4f;
		}
		else if(false) { //player not colliding with ground
			yVelocity -= yAcceleration;
		}
		else {
			//player colliding with ground without vertical input detected
		}
		
		//Make velo modify pos
		position.x += xVelocity;
		position.y += yVelocity;
	}

	@Override
	public void render() {
		//Assume to be a rectRenderer
		RectRenderer rectRender = (RectRenderer) renderer;
		
		rectRender.rect = dim;
		rectRender.pos = position;
		rectRender.render();
	}

}
