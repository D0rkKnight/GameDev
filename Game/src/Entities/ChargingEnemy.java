package Entities;

import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class ChargingEnemy extends Enemy {
	
	public ChargingEnemy(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name, stats);
		// TODO Auto-generated constructor stub
	}

	//This enemy attacks only by charging towards the player. No attack function, damage is strictly form collider.
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

	}

	@Override
	public void setFrame(int framenum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void move() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

}
