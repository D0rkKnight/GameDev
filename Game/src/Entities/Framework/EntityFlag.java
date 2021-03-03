package Entities.Framework;

import org.joml.Vector2f;

public class EntityFlag extends Entity {

	public EntityFlag(String ID, Vector2f position, String name) {
		super(ID, position, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub

	}

	public static interface FlagFactory {
		public EntityFlag create(Vector2f pos);
	}
}
