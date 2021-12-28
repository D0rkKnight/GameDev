package Entities.Framework;

import org.joml.Vector2f;

/**
 * Non abstract variant of the Entity class
 * 
 * @author shouh
 *
 */

public class SimpleEntity extends Entity {

	public SimpleEntity(String ID, Vector2f position, String name) {
		super(ID, position, name);
	}

	public SimpleEntity() {
		this("NO_ID", new Vector2f(), "UNNAMED");
	}

	public void render() {
		super.render();

		System.out.println("Rendering");
	}

}
