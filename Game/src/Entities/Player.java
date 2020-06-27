package Entities;

import Collision.Collider;
import GameController.Input;
import Rendering.RectRenderer;
import Rendering.Renderer;
import Wrappers.Position;
import Wrappers.Rect;
import Wrappers.Sprites;
import Wrappers.Stats;

public class Player extends Combatant{
	
	public Input input;
	
	public Player(int ID, Position position, Sprites sprites, Renderer renderer, Stats stats) {
		super(ID, position, sprites, renderer, stats);
		input = new Input();
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
			xVelocity = -5;
			position.x += input.moveX;
		}
		else if(input.moveX > 0 && true) {//moving right, check collision right
			xVelocity = 5;
			position.x += input.moveX;
		}
		else {
			xVelocity = 0;
		}
		if(true && input.moveY != 0) { //if player is colliding with ground underneath and digital input detected (space pressed)
			yVelocity = 10;
			position.y += yVelocity;
		}
		else if(false) { //player not colliding with ground
			yVelocity -= yAcceleration;
			position.y += yVelocity;
		}
		else {
			//player colliding with ground without vertical input detected
		}
		
	}

	@Override
	public void getPosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		float dim = 0.2f;
		
		//Assume to be a rectRenderer
		RectRenderer rectRender = (RectRenderer) renderer;
		
		rectRender.rect = new Rect(dim, dim);
		rectRender.pos = position;
		rectRender.render();
	}

}
