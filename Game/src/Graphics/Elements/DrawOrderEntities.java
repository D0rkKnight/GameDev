package Graphics.Elements;

import java.util.ArrayList;

import Entities.Framework.Entity;

public class DrawOrderEntities extends DrawOrderElement {

	public DrawOrderEntities(int z) {
		super(z);
	}

	public void tryRender(ArrayList<Entity> entities) {
		for (Entity ent : entities) {
			ent.render();
		}
	}
}
