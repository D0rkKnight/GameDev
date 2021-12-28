package Entities;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.EntityFlag;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class InteractableFlag extends EntityFlag {

	public InteractableFlag(Vector2f position) {
		super("I_FLAG", position, "Interactive Flag");

		// Configure the renderer real quick
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/icons.png"), 48, 48);

		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), dim, Shape.ShapeEnum.SQUARE, new Color(), tAtlas.genSubTex(0, 0));
		this.renderer = rend;

		rend.spr = tAtlas.tex;
	}
}
