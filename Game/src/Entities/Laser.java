package Entities;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import Collision.Collidable;
import Collision.Collider.CODVertex;
import Collision.Hitbox;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Aligned;
import Entities.Framework.Combatant;
import Entities.Framework.Entity;
import Entities.Framework.PhysicsEntity.Alignment;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Timers.Timer;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Laser extends Entity implements Collidable, Aligned {
	
	Alignment align;
	Hitbox hitbox;
	Timer killTimer;
	
	public Laser(String ID, Vector2f position, String name, Vector2f ray, Aligned owner, long lifeMilli) {
		super(ID, position, name);
		
		align = owner.getAlign();
		
		// Rend
		TextureAtlas atlas = new TextureAtlas(Texture.getTex("Assets/Sprites/laser.png"), 
				64, 64);
		
		rendDims = new Vector2f(ray.length(), 64);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, Shape.ShapeEnum.SQUARE, 
				new Color(), atlas.genSubTex(0, 0, (int) (rendDims.x/rendDims.y), 1));
		rend.spr = atlas.tex;
		rend.getOrigin().set(0, rendDims.y/2);

		this.renderer = rend;
		rendZ = -1; // Render before boss
		
		// Colliders
		dim = new Vector2f(rendDims);
		hitbox = new Hitbox(this, new CODVertex(dim.x, dim.y));
		hitbox.offset.set(0, -dim.y/2);
		addColl(hitbox);
		
		hitbox.cb = (Combatant c) -> {
			c.knockback(new Vector2f(0, 1));
			c.hit(5);
			c.invuln();
		};
		
		// Rotate the laser (right is the zero angle)
		float rad = new Vector2f(1, 0).angle(ray);
		localTrans.rot.rotate(rad, new Vector3f(0, 0, 1));
		
		killTimer = new Timer(lifeMilli, (Timer t) -> {
			Destroy();
		});
	}
	
	public Laser(Vector2f position, Vector2f ray, Aligned owner, long lifeMilli) {
		this ("LASER", position, "Generic Laser", ray, owner, lifeMilli);
	}
	
	public void calculate() {
		super.calculate();
		
		killTimer.update();
	}

	@Override
	public Alignment getAlign() {
		return align;
	}

	@Override
	public void setAlign(Alignment align) {
		this.align = align;
		
	}

}
