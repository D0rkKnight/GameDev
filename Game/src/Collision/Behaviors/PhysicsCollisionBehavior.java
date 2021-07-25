package Collision.Behaviors;

import org.joml.Vector2f;

import Entities.Framework.PhysicsEntity;
import Tiles.Tile;

public abstract class PhysicsCollisionBehavior extends PhysicsBehavior {

	public abstract void onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta);

}
