package Graphics.Elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import Entities.Framework.Entity;

public class DrawOrderEntities extends DrawOrderElement {

	public DrawOrderEntities(int z) {
		super(z);
	}

	public void tryRender(ArrayList<Entity> entities) {
		
		// Add everything to a priority queue
		Comparator<Entity> comp = (Entity o1, Entity o2) -> {
			return o2.rendZ - o1.rendZ;
		};
		
		// Farthest back elements are rendered first
		PriorityQueue<Entity> queue = new PriorityQueue<>(entities.size(), comp);
		for (Entity e : entities) queue.add(e);
		
		while (!queue.isEmpty()) {
			Entity e = queue.remove();
			e.render();
		}
	}
}
