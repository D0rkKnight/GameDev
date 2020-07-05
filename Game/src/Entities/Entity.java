package Entities;

import Rendering.Renderer;
import Wrappers.Rect;
import Wrappers.SpriteSheetSection;
import Wrappers.Sprites;
import Wrappers.Vector2;

/**
 * superclass for all entities
 * 
 * @author Benjamin
 *
 */
public abstract class Entity {
	protected int ID;
	protected Vector2 position;
	protected Sprites sprites;
	static float gravity = 0.1f;
	protected Renderer renderer;
	protected String name;
	public Rect dim;
	protected int animationGroups;
	protected int currentGroup;
	protected int currentFrame;
	protected SpriteSheetSection[][] frames;

	public Entity(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name) {
		this.ID = ID;
		this.position = position;
		this.sprites = sprites;
		this.renderer = renderer;
		this.name = name;
		try {
			this.renderer = renderer.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected abstract void calcFrame();

	public abstract void move();

	public Vector2 getPosition() {
		return position;
	}

	public abstract void render();

}
