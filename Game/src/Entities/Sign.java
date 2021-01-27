package Entities;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import GameController.EntityData;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GrassRenderer;
import Graphics.Rendering.GrassShader;
import Utility.Transformation;
import Wrappers.Color;

public class Sign extends Entity {

	String text;

	public Sign(String ID, Vector2f position, String name, String text) {
		super(ID, position, name);

		this.text = text;

		// Configure the renderer real quick
		// TESTING: It's gonna look like grass for now ig
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 32, 48);
		GrassRenderer rend = new GrassRenderer(GrassShader.genShader("grassShader"));
		rend.init(new Transformation(position), new Vector2f(32, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				tAtlas.genSubTex(0, 0));
		rend.spr = tAtlas.tex;
		this.renderer = rend;
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub

	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		// TODO Auto-generated method stub
		return new Sign(vals.str("type"), pos, vals.str("name"), vals.str("text"));
	}

}
