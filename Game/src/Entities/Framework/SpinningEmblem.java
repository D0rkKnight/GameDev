package Entities.Framework;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class SpinningEmblem extends Entity {

	public SpinningEmblem(String ID, Vector2f position, String name, Texture tex, SubTexture subtex) {
		super(ID, position, name);
		// TODO Auto-generated constructor stub

		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(position), new Vector2f(48, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				subtex);
		rend.spr = tex;
		this.renderer = rend;
	}
}
