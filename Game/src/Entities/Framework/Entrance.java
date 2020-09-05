package Entities.Framework;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Player;
import GameController.GameManager;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Wrappers.Color;

public class Entrance extends Entity implements Collidable {

	private Hitbox hb;

	public Entrance(int ID, Vector2f position, Renderer renderer, String name, Vector2f dims) {
		super(ID, position, renderer, name);

		dim = dims;
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		hb = new Hitbox(this, dim.x, dim.y);
	}

	@Override
	public void onHit(Hitbox otherHb) {
		if (otherHb.owner instanceof Player) {
			GameManager.switchMap("assets/Maps/", "test2.tmx");
		}
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
		return createNew(xPos, yPos, 30, 30);
	}

	public Entrance createNew(float xPos, float yPos, float width, float height) {
		return new Entrance(ID, new Vector2f(xPos, yPos), renderer, name, new Vector2f(width, height));
	}

}
