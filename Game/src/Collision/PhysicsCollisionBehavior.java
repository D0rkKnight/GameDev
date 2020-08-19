package Collision;

import java.util.ArrayList;

import org.joml.Vector2f;

import Entities.PhysicsEntity;
import Tiles.Tile;

public abstract class PhysicsCollisionBehavior {
	
	public String name;
	
	public abstract boolean onColl(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, 
			Tile[][] grid, Vector2f moveAxis, Vector2f[] axises, Vector2f moveDir, Vector2f tangent, Vector2f delta);
	
	public static void removeBehavior(ArrayList<PhysicsCollisionBehavior> behaviors, String nameStr) {
		boolean nameRemoved = false;
		
		for (int i=0; i<behaviors.size(); i++) {
			PhysicsCollisionBehavior b = behaviors.get(i);
			if (b.name.equals(nameStr)) {
				behaviors.remove(i);
				nameRemoved = true;
				break;
			}
		}
		
		if (!nameRemoved) {
			new Exception("Behavior to be removed not found!").printStackTrace();
		}
	}
}
