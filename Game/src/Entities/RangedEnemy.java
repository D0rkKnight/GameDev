package Entities;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Entities.Behavior.EntityFlippable;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import Entities.Framework.Projectile;
import Entities.Framework.StateMachine.StateID;
import Entities.Framework.StateMachine.StateTag;
import GameController.EntityData;
import GameController.GameManager;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;
import Wrappers.Stats;

public class RangedEnemy extends Enemy {

	EntityFlippable flip;

	public RangedEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Rend
		rendDims = new Vector2f(96, 96);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), rendDims, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Hitbox
		dim = new Vector2f(16, 96);
		addColl(new Hurtbox(this, dim.x, dim.y));

		rendOriginPos.x = rendDims.x / 2;
		entOriginPos.x = dim.x / 2;

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("assets/Sprites/ranged_enemy.png"), 48, 48);
		Animation a1 = new Animation(tAtlas.genSubTexSet(0, 0));
		Animation a2 = new Animation(tAtlas.genSubTexSet(1, 0));
		Animation a3 = new Animation(tAtlas.genSubTexSet(0, 1));
		Animation a4 = new Animation(tAtlas.genSubTexSet(1, 1));
		HashMap<StateTag, Animation> aMap = new HashMap<StateTag, Animation>();
		aMap.put(StateTag.IDLE, a1);
		aMap.put(StateTag.MOVING, a2);
		aMap.put(StateTag.FIRE, a3);
		aMap.put(StateTag.WINDOWN, a4);
		anim = new Animator(aMap, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		hasGravity = true;

		setEntityFD(StateID.MOVE);
	}

	@Override
	protected void initStructs() {
		super.initStructs();

		flip = new EntityFlippable(1, -1);
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();

		generalBehaviorList.add(new PGBGroundFriction(8f));
	}

	@Override
	protected void genTags() {
		super.genTags();

		addTag(StateTag.MOVEABLE, genMOVETag(flip));
		addTag(StateTag.DASHING, () -> {
			if (Math.abs(pData.velo.x) > 0.1f)
				anim.switchAnimWithoutReset(StateTag.MOVING);
			else
				anim.switchAnimWithoutReset(StateTag.IDLE);
		});

		addTag(StateTag.CAN_FIRE, (() -> {
			if (target == null)
				return;

			Vector2f tVec = new Vector2f(target.getCenter()).sub(getCenter());

			int range = 300;
			if (tVec.x * flip.sideFacing > 0 && tVec.length() < range) {
				setEntityFD(StateID.ATTACK);
			}
		}));
	}

	@Override
	protected void assignFD() {
		super.assignFD();

		addFD(StateID.MOVE, genMOVE());
		addFD(StateID.ATTACK, genATTACK());
	}

	@Override
	protected FrameData genMOVE() {
		FrameData fd = super.genMOVE();

		fd.getSeg(0).addCB(getTagCB(StateTag.DASHING));
		fd.getSeg(0).addCB(getTagCB(StateTag.CAN_FIRE));
		return fd;
	}

	private FrameData genATTACK() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(50, 0));
		segs.add(new FrameSegment(50, 50));

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(new Event(() -> {
			System.out.println("Attack");

			pData.velo.x = -0.5f * flip.sideFacing;

			anim.switchAnim(StateTag.WINDOWN);

			// Fire a projectile
			Projectile proj = new Projectile("PROJ", getCenter(), "Ranged enemy projectile", alignment);
			proj.pData.velo.x = flip.sideFacing * 0.5f;
			GameManager.subscribeEntity(proj);
		}, 50));

		FrameData fd = new FrameData(segs, evs, false);

		fd.onEnd = () -> setEntityFD(StateID.MOVE);
		fd.onEntry = () -> anim.switchAnim(StateTag.FIRE);

		return fd;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new RangedEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	public void calcFrame() {
		super.calcFrame();

		flip.update(this);
	}

}
