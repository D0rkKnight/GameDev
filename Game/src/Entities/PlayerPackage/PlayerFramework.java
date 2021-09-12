package Entities.PlayerPackage;

import java.util.HashMap;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.Hurtbox;
import Collision.Shapes.Shape;
import Entities.Behavior.EntityFlippable;
import Entities.Framework.Combatant;
import Entities.Framework.PhysicsEntity;
import Entities.Framework.Projectile;
import Entities.Framework.StateMachine.StateID;
import Entities.Framework.StateMachine.StateTag;
import GameController.GameManager;
import GameController.Input;
import GameController.Time;
import Graphics.Animation.Animation;
import Graphics.Animation.AnimationCallback;
import Graphics.Animation.Animator;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Graphics.particles.GhostParticleSystem;
import Utility.Arithmetic;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public abstract class PlayerFramework extends Combatant {

	private float jumpSpeed;
	Timer gunTimer;
	float xCap = 0.5f; // todo add to contructor (and break everything)
	float accelConst = 2f / 20f;
	float gunCost = 15f;
	float dashCost = 30f;

	private float dashSpeed;
	private Vector2f dashDir;
	private boolean releasedJump = true; // for making sure the player can't hold down w to jump

	int jumpsLeft;
	public int maxJumps = 1;
	private long jumpGraceInterval = 100;
	protected Timer jumpGraceTimer;

	EntityFlippable flip;

	public boolean canMove = true;

	GhostParticleSystem pSys;

	protected Hurtbox mainHurtbox;

	// Temp
	public Color baseCol;

	public PlayerFramework(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		// TODO: Seems like the renderer just wants to be defined within the entity and
		// not given from outside...
		rendDims = new Vector2f(96, 96);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(position), rendDims, Shape.ShapeEnum.SQUARE, new Color(1, 0, 0, 0));

		this.renderer = rend;

		baseCol = new Color(0, 0, 0, 1);

		// Configure hitbox
		dim = new Vector2f(15f, 60f);

		mainHurtbox = new Hurtbox(this, dim.x, dim.y);
		addColl(mainHurtbox);

		jumpSpeed = 1f;
		dashSpeed = 2f;

		initGraphics();
		rendOriginPos.set(rendDims.x / 2, 0);
		entOriginPos.x = dim.x / 2;

		// Alignment
		alignment = PhysicsEntity.Alignment.PLAYER;

		baseInvulnLength = 1000;

		setEntityFD(StateID.I);
	}

	@Override
	protected void initStructs() {
		flip = new EntityFlippable(1, 1);
	}

	private void initGraphics() {
		// Configure animation stuff
		TextureAtlas animSheet = new TextureAtlas(Texture.getTex("Assets/Sprites/Ilyia_idle-running_proto.png"), 96,
				96);
		HashMap<StateTag, Animation> anims = new HashMap<StateTag, Animation>();
		anims.put(StateTag.IDLE, new Animation(animSheet.genSubTexSet(0, 0, 3, 0)));
		anims.put(StateTag.ACCEL, new Animation(animSheet.genSubTexSet(0, 1, 11, 1)));
		anims.put(StateTag.MOVING, new Animation(animSheet.genSubTexSet(0, 2, 7, 2)));
		anims.put(StateTag.DASHING, new Animation(animSheet.genSubTexSet(0, 3, 0, 3)));

		anims.put(StateTag.DASH_ATK, new Animation(animSheet.genSubTexSet(1, 3, 1, 3)));
		anims.put(StateTag.JAB1, new Animation(animSheet.genSubTexSet(2, 3, 2, 3)));
		anims.put(StateTag.JAB2, new Animation(animSheet.genSubTexSet(3, 3, 3, 3)));
		anims.put(StateTag.LUNGE, new Animation(animSheet.genSubTexSet(4, 3, 4, 3)));

		anims.put(StateTag.TUMBLE, new Animation(animSheet.genSubTexSet(6, 3, 6, 3)));

		anims.get(StateTag.ACCEL).setCb(new AnimationCallback() {

			@Override
			public void onLoopEnd() {
				anim.switchAnim(StateTag.MOVING);
			}

		});

		anim = new Animator(anims, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		pSys = new GhostParticleSystem(animSheet.tex, 20, rendDims);
		pSys.init();
		pSys.pauseParticleGeneration();
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();

//		collBehaviorList.add(new PhysicsCollisionBehaviorStepUp());
//		collBehaviorList.add(new PhysicsCollisionBehaviorWallCling());
	}

	@Override
	public void calculate() {
		super.calculate();

		if (!GameManager.roomChanging) {
			if (Input.knockbackTest) {
				knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
			}
		}

		GeneralRenderer sprRend = (GeneralRenderer) renderer;
		if (hurtTimer == null && baseCol != null)
			sprRend.updateColors(baseCol);

		// Jump
		if (jumpGraceTimer != null)
			jumpGraceTimer.update();
		if (jumpsLeft > 0 && Input.moveY == 1 && releasedJump) {
			pData.velo.y = jumpSpeed;

			// TODO: Rename this so its purpose is less vague.
			pData.isJumping = true; // Signals to the physics system that some operations ought to be done
			releasedJump = false;

			jumpsLeft--;

			jumpGraceTimer = null; // Release timer so it doesn't decrement the jump count
		}
	}

	public void dash() {
		setEntityFD(StateID.DASH);

		if (stats.stamina < dashCost) {
			return;
		}

		stats.stamina -= dashCost;
		dashDir = new Vector2f(Input.moveX, Input.moveY).normalize();

		// Set velocity here
		pData.velo = new Vector2f(dashDir).mul(dashSpeed);
		hasGravity = false;

		// Update pSys
		pSys.resumeParticleGeneration();
	}

	@Override
	public void controlledMovement() {
		float xAccel = 0;

		float moveX = Input.moveX;
		float moveY = Input.moveY;
		if (!canMove) {
			moveX = 0;
			moveY = 0;
		}

		// Deceleration
		if (moveX != 0) {
			xAccel = accelConst * moveX;
		} else {
			float decelConst = Math.min(accelConst, Math.abs(pData.velo.x));

			xAccel = -decelConst * Arithmetic.sign(pData.velo.x);
		}
		if (Math.abs(pData.velo.x) <= xCap) {
			pData.velo.x += xAccel;

			// cap velo
			if (moveX > 0)
				pData.velo.x = Math.min(pData.velo.x, xCap);
			if (moveX < 0)
				pData.velo.x = Math.max(pData.velo.x, -xCap);
		} else {
			float decelConst = (Math.max(xCap, Math.abs(pData.velo.x) - 2 * accelConst));
			pData.velo.x -= decelConst * Arithmetic.sign(pData.velo.x);
		}
		if (moveY == 0) {
			releasedJump = true;
		}

		// Jump grace
		// TODO: Is it possible that only jumping for 1 frame can cause releasedJump to
		// be true despite having manually jumped?
		if (pData.wasGrounded && releasedJump == true) {
			// begin timer
			jumpGraceTimer = new Timer(jumpGraceInterval, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					jumpsLeft--;
					jumpGraceTimer = null;
				}
			});
		}

		int newSideFacing = Arithmetic.sign(Input.moveX);
		// Can only change sides if not attacking
		if (newSideFacing != 0)
			flip.sideFacing = newSideFacing;

		hasGravity = true;
	}

	/**
	 * For when melee options need to slow the pc down
	 */
	public void groundedSlowdown(float lerpConst) {
		Vector2f v = pData.velo;
		float scalar = Time.deltaT() * lerpConst / 1000;

		v.set(Arithmetic.lerp(v.x, 0, scalar), v.y);
	}

	/**
	 * I'm assuming that knockbackSpeed was set to not 0 upon being hit also
	 * assuming that hit function added velo already, this function just does
	 * deacceleration
	 */
	void decelMovement() {
		// automatic deacceleration
//		float decelConst = Math.min(accelConst * decelMulti, Math.abs(pData.velo.x) - xCap)
//				* -Arithmetic.sign(pData.velo.x);
		float decelConst = accelConst * decelMulti * -Arithmetic.sign(pData.velo.x);

		// effect of movement
		decelConst += accelConst * movementMulti * Input.moveX;

		if (pData.grounded) {
			decelConst *= 1.8;
		}
		pData.velo.x += decelConst * Time.deltaT() / 1000 * 10; // Lots of random tuning here
		hasGravity = true;

		// Escape knockback
		float escapeThreshMult = 0.5f;

		if (pData.velo.length() <= xCap * escapeThreshMult) {
			setEntityFD(StateID.I);
			knockbackDir = null;
		}
	}

	@Override
	public void knockback(Vector2f knockbackVector, float movementMulti, float decelMulti) {
		super.knockback(knockbackVector, movementMulti, decelMulti);

		setEntityFD(StateID.DECEL);
	}

	public void fireGun(Vector2f firePos) {
		if (stats.stamina < gunCost) {
			return;
		}

		stats.stamina -= gunCost;

		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));

		Projectile proj = new Projectile("PROJECTILE", pos, "Bullet", alignment); // initializes bullet
		// entity

		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f velo = new Vector2f(dir).mul(3);

		proj.pData.velo = new Vector2f(velo);

		GameManager.subscribeEntity(proj);
	}

	@Override
	public void calcFrame() {
		// Scale to the side facing
		flip.update(this);

		// Update trailing particle system
		pSys.activeSubTex = anim.currentAnim.getFrame();
		pSys.activeTransform = new ProjectedTransform(renderer.transform);
		pSys.update();

		super.calcFrame();
	}

	@Override
	public void render() {
		pSys.render();
		super.render();

	}

	@Override
	public void onTileCollision() {

	}
}
