package Entities;

import Rendering.Renderer;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Vector2;

public class LockedEntity extends Entity{

	public LockedEntity(int ID, Vector2 position, Renderer renderer, String name, Sprites sprites) {
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
		//Locked entities should never have to push their movement
	}

}
