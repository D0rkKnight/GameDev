package Entities;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Collider.CODVertex;
import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Entities.Behavior.EntityFlippable;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import Entities.Framework.Melee;
import Entities.Framework.StateMachine.StateID;
import Entities.Framework.StateMachine.StateTag;
import GameController.EntityData;
import GameController.GameManager;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Timers.Timer;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;
import Wrappers.Stats;

public class MeleeEnemy extends Enemy {

	// TODO: Same as player, remove boilerplate
	Timer windupTimer;
	EntityFlippable flip;

	public MeleeEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
		// Configure the renderer real quick
		rendDims = new Vector2f(64, 64);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		dim = new Vector2f(rendDims.x, rendDims.y * 1.5f);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, 0);
		addColl(hurtbox);
		this.renderer.getOrigin().y = -rendDims.y * 0.5f;
		this.renderer.getOrigin().x = rendDims.x / 2;

		offset.x = dim.x / 2;

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("assets/Sprites/bell_enemy.png"), 32, 32);
		Animation a1 = new Animation(tAtlas.genSubTexSet(0, 0, 5, 0));
		Animation a2 = new Animation(tAtlas.genSubTexSet(6, 0, 6, 0));
		Animation a3 = new Animation(tAtlas.genSubTexSet(7, 0, 7, 0));
		HashMap<StateTag, Animation> aMap = new HashMap<StateTag, Animation>();
		aMap.put(StateTag.IDLE, a1);
		aMap.put(StateTag.WINDUP, a2);
		aMap.put(StateTag.LUNGE, a3);
		anim = new Animator(aMap, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		hasGravity = true;

		// Generate and set states
		setEntityFD(StateID.MOVE);
	}

	@Override
	protected void initStructs() {
		flip = new EntityFlippable(1, -1);
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

		fd.getSeg(0).addCB(getTagCB(StateTag.CAN_MELEE));
		return fd;
	}

	private FrameData genATTACK() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(50, 0));
		segs.add(new FrameSegment(50, 50));

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(new Event(() -> {
			pData.velo.x = 1 * flip.sideFacing;

			anim.switchAnim(StateTag.LUNGE);

			// Attach a melee attack to self
			Melee me = new Melee(getCenter(), this, new Vector2f(flip.sideFacing, 0), 1, 150, dim);
			GameManager.subscribeEntity(me);
		}, 50));

		FrameData fd = new FrameData(segs, evs, false);

		fd.onEnd = () -> setEntityFD(StateID.MOVE);
		fd.onEntry = () -> anim.switchAnim(StateTag.WINDUP);

		return fd;
	}

	@Override
	protected void genTags() {
		super.genTags();

		addTag(StateTag.MOVEABLE, genMOVETag(flip));

		addTag(StateTag.CAN_MELEE, (() -> {
			if (target == null)
				return;

			Vector2f tVec = new Vector2f(target.getCenter()).sub(getCenter());

			int meleeRange = 100;
			if (tVec.x * flip.sideFacing > 0 && tVec.length() < meleeRange) {
				setEntityFD(StateID.ATTACK);
			}
		}));
	}

	@Override
	public void calcFrame() {
		flip.update(this);

		super.calcFrame();
	}
}
