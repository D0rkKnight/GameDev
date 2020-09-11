package Collision.Behaviors;

import org.joml.Vector2f;

import Collision.Physics;
import Debugging.Debug;
import Debugging.DebugVector;
import Entities.Framework.PhysicsEntity;
import Tiles.Tile;
import Utility.Vector;

public class PhysicsCollisionBehaviorDeflect extends PhysicsCollisionBehavior {

	public PhysicsCollisionBehaviorDeflect() {
		this.name = "deflect";
	}

	@Override
	public void onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		if (!e.pData.grounded) {
			// TODO: Maybe this should just force perpendicular axises?
			// Project velocity onto tangent axis (Tangent points left)
			float tanSpeed = velo.dot(tangent);

			// This returns the relevant velocity in world space, but we must change it to
			// use an angled coordinate system.
			Vector2f tangentVector = new Vector2f(tangent.x * tanSpeed, tangent.y * tanSpeed);

			Debug.enqueueElement(new DebugVector(new Vector2f(rawPos).add(new Vector2f(0, 100)), tangentVector, 20));

			// First x, then y
			Vector2f axisA = axises[Physics.X];
			Vector2f axisB = axises[Physics.Y];

			// Somehow the two implementations are different.
			Vector2f compA = new Vector2f(0, 0);
			Vector2f compB = new Vector2f(0, 0);
			float[] magBuff = new float[2];
			Vector.breakIntoComponents(tangentVector, axisA, axisB, compA, compB, magBuff);

			velo.x = magBuff[Physics.X];
			velo.y = magBuff[Physics.Y]; // The indexes just needs to match the input axises.
		}
	}
}
