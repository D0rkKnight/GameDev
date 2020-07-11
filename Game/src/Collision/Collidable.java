package Collision;

import Wrappers.Hitbox;

public interface Collidable {

	public void onHit(Hitbox otherHb);
	public Hitbox getHitbox();
}
