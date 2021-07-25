package Collision.Behaviors;

import org.joml.Math;

import Entities.Framework.Entity;
import Entities.Framework.PhysicsEntity;
import GameController.Time;

public class PGBGravity extends PhysicsGeneralBehavior {

	@Override
	public void invoke(PhysicsEntity pe) {
		// Gravity
		if (pe.hasGravity) {
			pe.pData.velo.y -= Entity.gravity * Time.deltaT() / 1300;
			pe.pData.velo.y = Math.max(pe.pData.velo.y, -2);
		}
	}
}
