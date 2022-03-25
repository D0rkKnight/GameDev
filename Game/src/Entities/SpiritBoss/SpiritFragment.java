package Entities.SpiritBoss;

import org.joml.Vector2f;
import org.joml.Vector3f;

import Collision.Collider.CODVertex;
import Collision.Hitbox;
import Collision.Hurtbox;
import Collision.Shapes.Shape;
import Entities.Framework.Combatant;
import Entities.Framework.Enemy;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class SpiritFragment extends Enemy {
	
	public Hitbox hitbox;
	public SpiritBoss owner;

	public SpiritFragment(String ID, Vector2f position, String name, Stats stats, SpiritBoss owner) {
		super(ID, position, name, stats);

		// Rend
		TextureAtlas texAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/spirit_boss.png"), 32, 32);
		
		rendDims = new Vector2f(96, 32);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, 
				Shape.ShapeEnum.SQUARE, new Color(), texAtlas.genSubTex(0, 6, 3, 1));
		rend.spr = texAtlas.tex;

		this.renderer = rend;

		// Hitbox
		dim = new Vector2f(96, 32);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, 0);
		addColl(hurtbox);
		
		hitbox = new Hitbox(this, new CODVertex(dim.x, dim.y));
		hitbox.offset.set(-dim.x/2, 0);
		addColl(hitbox);
		
		hitbox.cb = (Combatant c) -> {
			Vector2f ray = new Vector2f(getCenter()).sub(owner.getCenter()).normalize();
			c.knockback(ray);
			c.hit(5);
			c.invuln();
		};
		hitbox.isActive = false;

		pData.hasKnockback = false;

		this.renderer.getOrigin().x = rendDims.x / 2;
		offset.x = dim.x / 2;
		offset.y = -dim.y / 2;

		hasCollision = false;

		localTrans.scale.setTranslation(new Vector3f(0, -dim.y / 2, 0));
		
		// Fields
		this.owner = owner;
	}

	public SpiritFragment(Vector2f position, SpiritBoss owner) {
		this("SPIRIT_FRAG", position, "Spirit Fragment", new Stats(), owner);
	}
}
