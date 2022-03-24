package Entities.SpiritBoss;

import org.joml.Vector2f;
import org.joml.Vector3f;

import Collision.Collider.CODVertex;
import Collision.Hurtbox;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Enemy;
import Entities.Framework.StateMachine.StateID;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class SpiritFragment extends Enemy {

	public SpiritFragment(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Rend
		rendDims = new Vector2f(96, 32);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, Shape.ShapeEnum.SQUARE, new Color());
		rend.spr = Debug.debugTex;

		this.renderer = rend;

		// Hitbox
		dim = new Vector2f(96, 32);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, 0);
		addColl(hurtbox);

		pData.hasKnockback = false;

		this.renderer.getOrigin().x = rendDims.x / 2;
		origin.x = dim.x / 2;

		setEntityFD(StateID.MOVE);

		hasCollision = false;

		localTrans.scale.setTranslation(new Vector3f(0, -dim.y / 2, 0));
	}

	public SpiritFragment(Vector2f position) {
		this("SPIRIT_FRAG", position, "Spirit Fragment", new Stats());
	}
}
