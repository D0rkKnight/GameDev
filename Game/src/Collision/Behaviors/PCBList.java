package Collision.Behaviors;

import java.util.ArrayList;

public class PCBList extends PBList<PhysicsCollisionBehavior> {
	public PCBList() {
		behaviors = new ArrayList<PhysicsCollisionBehavior>();
	}
}
