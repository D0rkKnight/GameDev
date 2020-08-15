package Collision;

import org.joml.Vector2f;

import Entities.PhysicsEntity;
import GameController.GameManager;
import Math.Vector;
import Tiles.Tile;

public class PhysicsCollisionBehaviorStepUp extends PhysicsCollisionBehavior{

	@Override
	public boolean onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		/**
		 * Moving up 1 tile tall steps
		 */
		boolean tileBumpSuccess = false;
		if (e.wasGrounded && moveAxis == e.xDir) {
			Vector2f rightAxis = Vector.rightVector(axises[1]);
			float dot = rightAxis.dot(tangent);
			
			if (Math.abs(dot) == 0) {
				float dy = GameManager.tileSize;
				
				//Simultaneously move in the x dir and up
				Vector2f deltaSnap = new Vector2f(0, dy);
				Vector2f moveSnapSum = new Vector2f(deltaSnap).add(deltaMove);
				tileBumpSuccess = !Physics.isColliding(rawPos, moveSnapSum, e, grid, moveDir, null, null);
				
				if (tileBumpSuccess) {
					delta.set(deltaSnap);
					//Slow you down
					//velo.x *= 0.3; this accomplished nothinggg
				}
			}
		}
		
		// TODO Auto-generated method stub
		return !tileBumpSuccess;
	}

}
