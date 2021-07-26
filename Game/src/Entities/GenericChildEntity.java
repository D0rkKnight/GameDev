package Entities;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Collider;
import Collision.Hitbox;
import Entities.Framework.Entity;

public class GenericChildEntity extends Entity implements Collidable {

	protected Hitbox hb;

	public GenericChildEntity(String ID, Vector2f position, String name, Entity parent) {
		super(ID, position, name);

		parent.setAsChild(this);
	}

	@Override
	public void calculate() {
		super.calculate();

		if (parent != null)
			position.set(parent.getPosition());
	}

	@Override
	public void onColl(Collider otherHb) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collider getColl() {
		// TODO Auto-generated method stub
		return hb;
	}

	@Override
	public void setColl(Collider hb) {
		this.hb = (Hitbox) hb;
	}

}
