package Entities;

import org.joml.Vector2f;
import Collision.Collider.CODVertex;

import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Combatant;
import Entities.Framework.Entity;
import GameController.EntityData;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class PunchingBag extends Combatant {

	public PunchingBag(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		rendDims = new Vector2f(96, 128);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(position), rendDims, Shape.ShapeEnum.SQUARE, new Color(1, 0, 0, 0));
		rend.spr = Debug.debugTex;

		this.renderer = rend;

		addColl(new Hurtbox(this, new CODVertex(rendDims.x, rendDims.y)));
		this.dim = rendDims;

		this.alignment = Combatant.Alignment.ENEMY;
		this.hasGravity = true;
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();

		generalBehaviorList.add(new PGBGroundFriction(20f));
	}

	@Override
	public void calculate() {
		super.calculate();
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new PunchingBag(vals.str("ID"), pos, vals.str("name"), Stats.fromED(vals));
	}
}
