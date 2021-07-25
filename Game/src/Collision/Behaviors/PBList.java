package Collision.Behaviors;

import java.util.List;

public class PBList<T extends PhysicsBehavior> {
	public List<T> behaviors;

	public void remove(Class<?> clazz) {
		boolean removed = false;

		for (int i = 0; i < behaviors.size(); i++) {
			PhysicsBehavior b = behaviors.get(i);
			if (clazz.isInstance(b)) {
				behaviors.remove(i);
				removed = true;
				break;
			}
		}

		if (!removed) {
			new Exception("Behavior to be removed not found!").printStackTrace();
		}
	}

	public void add(T item) {
		behaviors.add(item);
	}
}
