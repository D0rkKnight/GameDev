package Entities.Framework;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Graphics.Rendering.Renderer;

public class Entrance extends Entity implements Collidable {

	private Hitbox hb;

	public Entrance(int ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onHit(Hitbox otherHb) {
		System.out.println("Zwoop");
	}

	@Override
	public Hitbox getHb() {
		return hb;
	}

	@Override
	public void setHb(Hitbox hb) {
		this.hb = hb;
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void controlledMovement() {

	}

	@Override
	public Entrance createNew(float xPos, float yPos) {
		return new Entrance(ID, new Vector2f(xPos, yPos), renderer, name);
	}

}
