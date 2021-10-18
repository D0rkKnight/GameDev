package Collision;

import Entities.Framework.Aligned;
import Entities.Framework.Entity;

public class Hitbox extends Collider {

	public HitboxCallback cb;

	public Hitbox(Entity owner, COD<?> cod) {
		super(owner, cod);

		// Alignment check
		if (!(owner instanceof Aligned)) {
			new Exception("Owner not aligned").printStackTrace();
			System.exit(1);
		}
	}

}
