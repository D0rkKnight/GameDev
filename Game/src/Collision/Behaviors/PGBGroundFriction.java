package Collision.Behaviors;

import org.joml.Vector2f;

import Entities.Framework.PhysicsEntity;
import GameController.Time;
import Utility.Arithmetic;

public class PGBGroundFriction extends PhysicsGeneralBehavior {

	public float frictionMult;

	public PGBGroundFriction(float frictionMult) {
		this.frictionMult = frictionMult;
	}

	@Override
	public void invoke(PhysicsEntity pe) {
		if (pe.pData.grounded) {
			Vector2f v = pe.pData.velo;
			v.x = Arithmetic.lerp(v.x, 0, Time.deltaT() * frictionMult / 1000);
		}
	}

}
