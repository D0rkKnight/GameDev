package Collision.Behaviors;

import java.util.List;

public class PBList<T extends PhysicsBehavior> {
	public List<T> behaviors;

	public void removeBehavior(String nameStr) {
		boolean nameRemoved = false;

		for (int i = 0; i < behaviors.size(); i++) {
			PhysicsBehavior b = behaviors.get(i);
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

	public void add(T item) {
		behaviors.add(item);
	}
}
