package Collision;

import org.joml.Vector2f;

import Entities.PhysicsEntity;
import Tiles.Tile;

public class PhysicsCollisionBehaviorWallCling extends PhysicsCollisionBehavior{

	@Override
	public boolean onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta) {
		if ((moveDir.equals(e.pData.xDir) && moveDir.dot(tangent) == 0)) {
			velo.x = 0;
			velo.y = 0;
			e.pData.velo.y = 0;
			e.pData.grounded = true;
			// TODO Auto-generated method stub
			return false;
		}
		return true;
	}

}
