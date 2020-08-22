package Entities;

import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.Hitbox;
import Debug.Debug;
import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Rendering.Transformation;
import Wrappers.Color;

/**
 * An attack with physical presence in the game world
 * @author Hanzen Shou
 *
 */
public class Melee extends CollidableEntity {
	
	Hitbox hitbox;
	Entity owner;

	public Melee(int ID, Vector2f position, Renderer renderer, String name, Entity owner) {
		super(ID, position, renderer, name);
		
		this.owner = owner;
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		
		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controlledMovement() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Entity clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity clone(float xPos, float yPos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}

}
