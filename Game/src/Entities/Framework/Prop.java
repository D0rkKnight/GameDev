package Entities.Framework;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Wrappers.Color;

public class Prop extends Entity {

	public Prop(String ID, Vector2f position, Renderer renderer, String name) {
		super(ID, position, renderer, name);

		// Configure the renderer real quick
		((GeneralRenderer) this.renderer).init(new Transformation(position), new Vector2f(32, 48),
				Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0));
		((GeneralRenderer) this.renderer).spr = Texture.getSprSheet("Assets/Sprites/props.png", 32, 48).texs[0];
	}

	@Override
	public void calculate() {
	}

	@Override
	public Entity createNew(float xPos, float yPos) {
		return new Prop(ID, new Vector2f(xPos, yPos), renderer, name);
	}

}
