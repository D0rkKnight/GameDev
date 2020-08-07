package Entities;

import org.joml.Vector2f;

import Collision.Collidable;
import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Stats;

public class WormHead extends Enemy implements Collidable{
	protected WormTail backSegment;
	public WormHead(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name, Stats stats, WormTail backSegment) {
		super(ID, position, sprites, renderer, name, stats);
		this.backSegment = backSegment;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onHit(Hitbox hb) {
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
		
	}

	@Override
	public void controlledMovement() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

}
