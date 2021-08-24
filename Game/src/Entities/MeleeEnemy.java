package Entities;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.DebugBox;
import Entities.Framework.Aligned;
import Entities.Framework.Combatant;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import Entities.Framework.Melee;
import Entities.Framework.StateMachine.ECB;
import Entities.Framework.StateMachine.StateTag;
import GameController.EntityData;
import GameController.GameManager;
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
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;
import Wrappers.Stats;

public class MeleeEnemy extends Enemy {

	// TODO: Same as player, remove boilerplate
	int sideFacing = 1;
	int flip = -1;
	Timer sideSwitchTimer;
	Timer windupTimer;

	static HashMap<StateTag, ECB<MeleeEnemy>> tagCBs = new HashMap<>();

	public MeleeEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
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
		Animation a2 = new Animation(tAtlas.genSubTexSet(6, 0, 6, 0));
		Animation a3 = new Animation(tAtlas.genSubTexSet(7, 0, 7, 0));
		HashMap<ID, Animation> aMap = new HashMap<ID, Animation>();
		aMap.put(Animator.ID.IDLE, a1);
		aMap.put(Animator.ID.WINDUP, a2);
		aMap.put(Animator.ID.LUNGE, a3);
		anim = new Animator(aMap, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		hasGravity = true;

		// Generate and set states
		genTags();
		FD.assignFD();
		setEntityFD(FD.MOVE.fd);
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

	// TODO: Maybe make this just use a general list of tags and map to a hashmap?
	// Can always add a layer of abstraction if need be.
	private static enum FD {
		MOVE, ATTACK, STUNNED;

		public FrameData fd;

		public static void assignFD() {
			MOVE.fd = genMOVE();
			ATTACK.fd = genATTACK();
			STUNNED.fd = genSTUNNED();
		}
	}

	private static FrameData genMOVE() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(tagCBs.get(StateTag.MOVEABLE), tagCBs.get(StateTag.CAN_MELEE)));

		FrameData fd = new FrameData(segs, null, true);
		fd.onEntry = (ECB<MeleeEnemy>) (e) -> e.anim.switchAnim(Animator.ID.IDLE);

		return fd;
	}

	private static FrameData genATTACK() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(50, 0));
		segs.add(new FrameSegment(50, 50));

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(new Event((ECB<MeleeEnemy>) (e) -> {
			System.out.println("Attack");

			e.pData.velo.x = 1 * e.sideFacing;

			e.anim.switchAnim(Animator.ID.LUNGE);

			// Attach a melee attack to self
			Melee me = new Melee(e.getCenter(), e, new Vector2f(e.sideFacing, 0), 1, 150, e.dim);
			GameManager.subscribeEntity(me);
		}, 50));

		FrameData fd = new FrameData(segs, evs, false);

		fd.onEnd = (ECB<MeleeEnemy>) (e) -> e.setEntityFD(FD.MOVE.fd);
		fd.onEntry = (ECB<MeleeEnemy>) (e) -> e.anim.switchAnim(Animator.ID.WINDUP);

		return fd;
	}

	private static FrameData genSTUNNED() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(50, 0));

		FrameData fd = new FrameData(segs, null, false);
		fd.onEnd = (ECB<MeleeEnemy>) (e) -> e.setEntityFD(FD.MOVE.fd);
		fd.onEntry = (ECB<MeleeEnemy>) (e) -> e.anim.switchAnim(Animator.ID.IDLE); // No stunned animation yet

		fd.cb = (e) -> System.out.println("Stunned");

		return fd;
	}

	// TODO: This can definitely be inherited
	private static void genTags() {
		tagCBs.put(StateTag.MOVEABLE, (ECB<MeleeEnemy>) (e) -> {
			e.follow();
		});

		tagCBs.put(StateTag.CAN_MELEE, (ECB<MeleeEnemy>) (e) -> {
			if (e.target == null)
				return;

			Vector2f tVec = new Vector2f(e.target.getCenter()).sub(e.getCenter());

			int meleeRange = 100;
			if (tVec.x * e.sideFacing > 0 && tVec.length() < meleeRange) {
				e.setEntityFD(FD.ATTACK.fd);
			}
		});
	}

	public void follow() {
		// Pursue player
		if (target == null)
			findTarget();

		if (target != null) {
			Vector2f tVec = new Vector2f(target.getCenter()).sub(getCenter());

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

			int pursueThresh = 100;
			float maxVelo = 1;
			if (Math.abs(tVec.x) > pursueThresh && sideSwitchTimer == null) {
				Vector2f v = pData.velo;

				v.x = Arithmetic.lerp(v.x, maxVelo * sideFacing, 3f * Time.deltaT() / 1000f);
			} else if (Math.abs(tVec.x) <= pursueThresh && sideSwitchTimer == null) {
				// Attack
				// TODO: I should probably just write this as a state machine
			}
		}
	}

	@Override
	public void hurtBy(Hitbox other) {
		Aligned otherOwner = (Aligned) other.owner;
		if (Combatant.getOpposingAlignment(otherOwner.getAlign()) == alignment) {
			setEntityFD(FD.STUNNED.fd);
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
