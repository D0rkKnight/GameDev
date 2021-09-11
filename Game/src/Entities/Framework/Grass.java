package Entities.Framework;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import GameController.EntityData;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.GrassShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Grass extends Entity {

	public Grass(String ID, Vector2f position, String name) {
		super(ID, position, name);

		// Configure the renderer real quick
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 32, 48);
		GeneralRenderer rend = new GeneralRenderer(GrassShader.genShader("grassShader"));
		rend.init(new ProjectedTransform(position), new Vector2f(32, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				tAtlas.genSubTex(0, 0));
		rend.spr = tAtlas.tex;
		this.renderer = rend;
	}

	@Override
	public void calculate() {

	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		// TODO Auto-generated method stub
		return new Grass(vals.str("type"), pos, vals.str("name"));
	}

}
