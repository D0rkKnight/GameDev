package Entities.Framework;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GrassRenderer;
import Graphics.Rendering.GrassShader;
import Utility.Transformation;
import Wrappers.Color;

public class Prop extends Entity {

	public Prop(String ID, Vector2f position, String name) {
		super(ID, position, name);

		// Configure the renderer real quick
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 32, 48);
		GrassRenderer rend = new GrassRenderer(GrassShader.genShader("grassShader"));
		rend.init(new Transformation(position), new Vector2f(32, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				tAtlas.genSubTex(0, 0));
		rend.spr = tAtlas.tex;
		this.renderer = rend;
	}

	@Override
	public void calculate() {
	}

	@Override
	public Entity createNew(float xPos, float yPos) {
		return new Prop(ID, new Vector2f(xPos, yPos), name);
	}

}
