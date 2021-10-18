package Collision;

import Collision.Collider.COD;

public interface CrossCollisionCB {
	public boolean test(COD<?> c1, COD<?> c2);
}
