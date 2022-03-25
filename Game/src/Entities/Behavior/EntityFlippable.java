package Entities.Behavior;

import Entities.Framework.Entity;
import Utility.Timers.Timer;

public class EntityFlippable {
	public int sideFacing = 1;
	public int flipConst = 1;
	public Timer sideSwitchTimer;

	public EntityFlippable(int sideFacing, int flipConst) {
		this.sideFacing = sideFacing;
		this.flipConst = flipConst;
	}

	public void update(Entity e) {
		// Scale to the side facing
		if (sideFacing != 0) {
			e.localTrans.scale.identity().scaleAround(sideFacing * flipConst, 1, 1, e.offset.x, 0, 0);
		}
	}
}
