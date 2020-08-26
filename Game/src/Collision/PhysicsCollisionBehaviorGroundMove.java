package Collision;

import org.joml.Vector2f;

import Entities.PhysicsEntity;
import Tiles.Tile;

public class PhysicsCollisionBehaviorGroundMove extends PhysicsCollisionBehavior {
	
	public PhysicsCollisionBehaviorGroundMove() {
		this.name = "groundMove";
	}
	
	@Override
	public boolean onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		
		Vector2f tangentDir = new Vector2f(tangent).normalize();
		
		//TODO: Make this work along any gravitational pull
		if (Math.abs(tangentDir.y) < 0.8 && tangentDir.x < 0 && e.pData.canBeGrounded) {
			e.pData.grounded = true;
			
			//Dunno why this needs to be flipped but it does
			Vector2f newXDir = new Vector2f(-tangent.x, -tangent.y);
			
			if (e.pData.wasGrounded) {
				//Ground-ground transition
				//No need to queue whatever, just force the change.
				/**
				 * Redefining the x axis should only occur when moving along the x axis.
				 */
				if (moveAxis == e.pData.xDir) {
					e.forceDirectionalChange(newXDir, e.pData.yDir);
				}
			} else {
				//Aerial landing
				/**
				 * Doesn't matter which axis of approach, any ground tangent is preferential to the aerial axises.
				 */
				e.forceDirectionalChange(newXDir, e.pData.yDir);
			}
			
			//Make sure you don't continue falling
			velo.y = 0;
			//e.pData.grounded = true; This is already happening in the physics loop
			
			return true;
		}
		
		return false;
	}

}
