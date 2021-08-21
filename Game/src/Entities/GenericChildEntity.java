package Entities;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Collider;
import Entities.Framework.Entity;

public class GenericChildEntity extends Entity implements Collidable {

	protected ArrayList<Collider> colls;

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
	public ArrayList<Collider> getColl() {
		return colls;
	}

	@Override
	public void addColl(Collider hb) {
		colls.add(hb);
	}

	@Override
	public void remColl(Collider hb) {
		colls.remove(hb);
	}

}
