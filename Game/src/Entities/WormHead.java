package Entities;

import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class WormHead extends Enemy{
	protected WormTail backSegment;
	public WormHead(int ID, Vector2 position, Sprites sprites, Renderer renderer, Stats stats, WormTail backSegment) {
		super(ID, position, sprites, renderer, stats);
		this.backSegment = backSegment;
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
