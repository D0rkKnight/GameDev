package Collision;

import Entities.Framework.Aligned;
import Entities.Framework.Combatant;
import Entities.Framework.PhysicsEntity.Alignment;

public class Hurtbox extends Collider {

	public Hurtbox(Combatant owner, float width, float height) {
		super(owner, width, height);
	}

	@Override
	void hitBy(Collider coll) {
		// TODO Auto-generated method stub
		if (coll instanceof Hitbox) { // If being hit by a hitbox specifically
			Hitbox hb = (Hitbox) coll;

			Alignment thisAlign = ((Combatant) owner).getAlign();
			Alignment oppAlign = ((Aligned) hb.owner).getAlign();

			if (Combatant.getOpposingAlignment(thisAlign) == oppAlign) {
				System.out.println("Hitting");

				if (hb.cb != null) // Hit actions all occur in here.
					hb.cb.onHit((Combatant) owner);

				((Combatant) owner).hurtBy(hb);
			}
		}
	}

}
