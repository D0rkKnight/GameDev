package Collision.Behaviors;

import org.joml.Vector2f;

import Collision.Physics;
import Entities.Framework.PhysicsEntity;
import GameController.GameManager;
import Tiles.Tile;
import Utility.Vector;

public class PCBStepUp extends PhysicsCollisionBehavior {

	public PCBStepUp() {
		this.name = "stepUp";
	}

	@Override
	public void onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		// TODO: Make this not janky...

		/**
		 * Moving up 1 tile tall steps
		 */
		boolean tileBumpSuccess = false;
		if (e.pData.wasGrounded && moveAxis == e.pData.xDir) {
			Vector2f rightAxis = Vector.rightVector(axises[1]);
			float dot = rightAxis.dot(tangent);

			if (Math.abs(dot) == 0) {
				float dy = GameManager.tileSize;

				// Simultaneously move in the x dir and up
				Vector2f deltaSnap = new Vector2f(0, dy);
				Vector2f moveSnapSum = new Vector2f(deltaSnap).add(deltaMove);
				tileBumpSuccess = !Physics.isColliding(rawPos, moveSnapSum, e, grid, moveDir, null, null);

				if (tileBumpSuccess) {
					delta.set(deltaSnap);
				}
			}
		}
	}

}
