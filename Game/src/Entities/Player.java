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
		
		dim = new Rect(0.2f, 0.2f);
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
	public void move() {
		// TODO Auto-generated method stub
		position.x += input.moveX * 0.01;
		position.y += input.moveY * 0.01;
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
