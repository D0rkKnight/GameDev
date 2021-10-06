package Entities.SpiritBoss;

import org.joml.Vector2f;

import Entities.Framework.Entity;

public class SpiritPulse extends Entity {

	public float r;
	public float w;

	public SpiritPulse(String ID, Vector2f position, String name) {
		super(ID, position, name);

		r = 100;
		w = 10;

	}

}
