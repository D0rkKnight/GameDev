package Entities;

import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.DebugBox;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import GameController.EntityData;
import GameController.Time;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Animation.Animator.ID;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Arithmetic;
import Utility.Timers.Timer;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class MeleeEnemy extends Enemy {

	// TODO: Same as player, remove boilerplate
	int sideFacing = 1;
	int flip = -1;
	Timer sideSwitchTimer;

	public MeleeEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
		// TODO Auto-generated constructor stub

		// Configure the renderer real quick
		rendDims = new Vector2f(64, 64);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), rendDims, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		dim = new Vector2f(rendDims.x, rendDims.y * 1.5f);
		addColl(new Hurtbox(this, dim.x, dim.y));
		rendOriginPos.y = -rendDims.y * 0.5f;
		rendOriginPos.x = rendDims.x / 2;

		entOriginPos.x = dim.x / 2;

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("assets/Sprites/bell_enemy.png"), 32, 32);
		Animation a1 = new Animation(tAtlas.genSubTexSet(0, 0, 5, 0));
		HashMap<ID, Animation> aMap = new HashMap<ID, Animation>();
		aMap.put(Animator.ID.IDLE, a1);
		anim = new Animator(aMap, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		hasGravity = true;
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();
		generalBehaviorList.add(new PGBGroundFriction(10f));
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new MeleeEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	public void calculate() {
		super.calculate();

		// Pursue player
		if (target == null)
			findTarget();

		if (target != null) {
			Vector2f tVec = new Vector2f(target.getCenter()).sub(getCenter());
			// System.out.println(target.getCenter());

			// Handle pauses when switching side faced
			int newSideFacing = Arithmetic.sign(tVec.x);
			if (newSideFacing != sideFacing && sideSwitchTimer == null) {
				sideSwitchTimer = new Timer(700, (t) -> {
					sideSwitchTimer = null;
					sideFacing = newSideFacing;
				});
			} else if (newSideFacing == sideFacing)
				sideSwitchTimer = null; // Short circuit if facing the right side

			if (sideSwitchTimer != null)
				sideSwitchTimer.update();

			int pursueThresh = 10;
			float maxVelo = 1;
			if (Math.abs(tVec.x) > pursueThresh && sideSwitchTimer == null) {
				Vector2f v = pData.velo;

				v.x = Arithmetic.lerp(v.x, maxVelo * sideFacing, 3f * Time.deltaT() / 1000f);
			}
		}
	}

	@Override
	public void calcFrame() {
		// Log origin
		Debug.enqueueElement(new DebugBox(position, new Vector2f(10, 10), new Color(1, 0, 0, 1), 1));

		// Scale to the side facing
		if (sideFacing != 0) {
			localTrans.scale.identity().scaleAround(sideFacing * flip, 1, 1, entOriginPos.x, 0, 0);
		}

		super.calcFrame();
	}
}
