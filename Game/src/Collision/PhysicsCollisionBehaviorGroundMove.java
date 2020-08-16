package Collision;

import org.joml.Vector2f;

import Entities.PhysicsEntity;
import Tiles.Tile;

public class PhysicsCollisionBehaviorGroundMove extends PhysicsCollisionBehavior {

	@Override
	public boolean onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		//Dunno why this needs to be flipped but it does
		Vector2f newXDir = new Vector2f(-tangent.x, -tangent.y);
		
		if (e.wasGrounded) {
			//Ground-ground transition
			//No need to queue whatever, just force the change.
			/**
			 * Redefining the x axis should only occur when moving along the x axis.
			 */
			if (moveAxis == e.xDir) {
				e.forceDirectionalChange(newXDir, e.yDir);
			}
		} else {
			//Aerial landing
			/**
			 * Doesn't matter which axis of approach, any ground tangent is preferential to the aerial axises.
			 */
			e.forceDirectionalChange(newXDir, e.yDir);
		}
		
		//Make sure you don't continue falling
		velo.y = 0;
		e.grounded = true;
		
		return true;
	}

}
