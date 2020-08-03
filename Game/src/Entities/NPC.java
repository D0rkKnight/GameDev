package Entities;

import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Vector2;

public class NPC extends Entity {

	public NPC(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name) {
		super(ID, position, sprites, renderer, name);
		// TODO Auto-generated constructor stub
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

	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pushMovement() {
		// TODO Auto-generated method stub
		System.err.println("what");
	}

}
