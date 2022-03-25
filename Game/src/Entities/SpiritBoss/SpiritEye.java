package Entities.SpiritBoss;

import org.joml.Vector2f;

import Collision.Hurtbox;
import Collision.Collider.CODVertex;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Enemy;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class SpiritEye extends Enemy {

	public SpiritEye(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
		// Rend
		rendDims = new Vector2f(96, 96);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, 
				Shape.ShapeEnum.SQUARE, new Color());
		rend.spr = Texture.getTex("Assets/Sprites/eye.png");
		rend.getOrigin().set(rendDims.x/2, rendDims.y/2);

		this.renderer = rend;
		rendZ = -1; // Render before boss
		
		// Colliders
		dim = new Vector2f(rendDims);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, -dim.y/2);
		addColl(hurtbox);
	}
	
	public void render() {
		super.render();
	}

}
