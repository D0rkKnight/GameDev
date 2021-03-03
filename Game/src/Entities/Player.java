package Entities;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Shapes.Shape;
import Entities.Framework.Combatant;
import Entities.Framework.Entity;
import Entities.Framework.Melee;
import Entities.Framework.PhysicsEntity;
import Entities.Framework.Projectile;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Animation.PlayerAnimator;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Graphics.particles.GhostParticleSystem;
import Utility.Arithmetic;
import Utility.Transformation;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;
import Wrappers.Stats;

public class Player extends Combatant {

	private float jumpSpeed;
	private Timer gunTimer;
	float xCap = 0.5f; // todo add to contructor (and break everything)
	float accelConst = 2f / 20f;
	float gunCost = 15f;
	float dashCost = 30f;

	private float dashSpeed;
	private long dashDuration;
	private Timer dashTimer;
	private Vector2f dashDir;
	private boolean releasedJump = true; // for making sure the player can't hold down w to jump
	private boolean justDashed = false;

	private Timer meleeTimer;
	private Melee meleeEntity;

	private boolean canJump;
	private long jumpGraceInterval = 100;
	private Timer jumpGraceTimer;

	private int sideFacing;

	public boolean canMove = true;

	private GhostParticleSystem pSys;

	public Player(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		// TODO: Seems like the renderer just wants to be defined within the entity and
		// not given from outside...
		rendDims = new Vector2f(96, 96);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new Transformation(position), rendDims, Shape.ShapeEnum.SQUARE, new Color(1, 0, 0, 0));

		this.renderer = rend;

		// Configure hitbox
		dim = new Vector2f(15f, 60f);
		hitbox = new Hitbox(this, dim.x, dim.y);

		jumpSpeed = 1f;

		dashSpeed = 2f;
		dashDuration = 80;
		movementMode = Movement.CONTROLLED;

		// Configure animation stuff
		TextureAtlas animSheet = new TextureAtlas(Texture.getTex("Assets/Sprites/Ilyia_idle-running_proto.png"), 96,
				96);
		Animation[] anims = new Animation[4];
		anims[Animator.ANIM_IDLE] = new Animation(animSheet.genSubTexSet(0, 0, 3, 0));
		anims[PlayerAnimator.ANIM_ACCEL] = new Animation(animSheet.genSubTexSet(0, 1, 11, 1));
		anims[PlayerAnimator.ANIM_MOVING] = new Animation(animSheet.genSubTexSet(0, 2, 7, 2));
		anims[PlayerAnimator.ANIM_DASHING] = new Animation(animSheet.genSubTexSet(0, 3, 0, 3));

		rendOffset.set(-35, 0);

		anim = new PlayerAnimator(anims, 12, (GeneralRenderer) this.renderer, this, Shape.ShapeEnum.SQUARE.v);

		// Alignment
		alignment = PhysicsEntity.Alignment.PLAYER;

		sideFacing = 1;

		baseInvulnLength = 1000;

		pSys = new GhostParticleSystem(animSheet.tex, 20, rendDims);
		pSys.init();
		pSys.pauseParticleGeneration();
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Player(vals.str("ID"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();

		// collBehaviorList.add(new PhysicsCollisionBehaviorStepUp());
		// collBehaviorList.add(new PhysicsCollisionBehaviorWallCling());
	}

	@Override
	public void calculate() {
		super.calculate();

		if (!GameManager.roomChanging) {
			determineMovementMode(); // determine what movement mode and execute it
		}

		Color baseCol = null;
		switch (movementMode) {
		case CONTROLLED: // walking
			baseCol = new Color(1, 0, 0, 1);
			controlledMovement();
			break;
		case DASHING: // dashing, a bit spaghetti here TODO
			baseCol = new Color(1, 1, 1, 1);
			dashingMovement();
			break;
		case DECEL:
			baseCol = new Color(0, 0, 1, 1);
			decelMovement();
			break;
		default:
			System.err.println("Movement mode not set.");
		}

		GeneralRenderer sprRend = (GeneralRenderer) renderer;
		if (hurtTimer == null)
			sprRend.updateColors(baseCol);

		// Gravity
		if (hasGravity) {
			pData.velo.y -= Entity.gravity / 100;
			pData.velo.y = Math.max(pData.velo.y, -2);
		}

		// Jump
		if (jumpGraceTimer != null)
			jumpGraceTimer.update();
		if (canJump && Input.moveY == 1 && releasedJump) {
			pData.velo.y = jumpSpeed;

			// TODO: Rename this so its purpose is less vague.
			pData.isJumping = true; // Signals to the physics system that some operations ought to be done
			releasedJump = false;

			canJump = false;
		}

		// Shoot a gun
		if (Input.primaryButtonDown) {
			if (gunTimer == null) {
				// Configure firing
				gunTimer = new Timer(100, new TimerCallback() {

					@Override
					public void invoke(Timer timer) {
						fireGun(Input.mouseWorldPos);
					}

				});
			}
			gunTimer.update();
		}

		// Melee
		if (Input.meleeAction && meleeTimer == null) {
			melee(Input.mouseWorldPos);
		}

		// Update timers
		if (meleeTimer != null) {
			meleeTimer.update();
		}
		if (dashTimer != null)
			dashTimer.update();
	}

	private void determineMovementMode() {
		if (Input.knockbackTest && movementMode == Movement.CONTROLLED) {
			knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
		}
		if (knockback) {
			movementMode = Movement.DECEL;
			knockback = false;
		}
		if (movementMode == Movement.DECEL && Math.abs(pData.velo.x) <= xCap) {
			movementMode = Movement.CONTROLLED;
			knockbackDir = null;
		}

		// Initiating dash
		if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0) && movementMode != Movement.DASHING) {
			if (stats.stamina < dashCost) {
				return;
			}

			stats.stamina -= dashCost;
			justDashed = true;
			movementMode = Movement.DASHING;
			dashDir = new Vector2f(Input.moveX, Input.moveY).normalize();

			// Set velocity here
			pData.velo = new Vector2f(dashDir).mul(dashSpeed);

			// Begin a timer
			dashTimer = new Timer(dashDuration, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					// First, dump this timer
					dashTimer = null;

					// Now stop dashing
					movementMode = Movement.CONTROLLED;
				}

			});

			// Update pSys
			pSys.resumeParticleGeneration();

			// Change animator
			// Does this even work?
			// Tbh all anim updates should be in Player, not PlayerAnimator TODO
			anim.switchAnim(PlayerAnimator.ANIM_DASHING);
		}

		// Bring entity back to normal
		if (movementMode == Movement.CONTROLLED && justDashed) {
			pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			pData.velo.x *= 0.8;
			justDashed = false;
			decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			// Update pSys
			pSys.pauseParticleGeneration();
			anim.switchAnim(PlayerAnimator.ANIM_MOVING);
		}

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
		if (pData.grounded && movementMode != Movement.DASHING)
			canJump = true;
		else if (pData.wasGrounded) {
			// begin timer
			jumpGraceTimer = new Timer(jumpGraceInterval, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					canJump = false;
					jumpGraceTimer = null;
				}
			});
		}

		int newSideFacing = Arithmetic.sign(Input.moveX);
		// Can only change sides if not attacking
		if (newSideFacing != 0 && meleeTimer == null)
			sideFacing = newSideFacing;

		hasGravity = true;
	}

	private void dashingMovement() {
		hasGravity = false;
	}

	/**
	 * I'm assuming that knockbackSpeed was set to not 0 upon being hit also
	 * assuming that hit function added velo already, this function just does
	 * deacceleration
	 */
	private void decelMovement() {
		// automatic deacceleration
		float decelConst = Math.min(accelConst * decelMulti, Math.abs(pData.velo.x) - xCap)
				* -Arithmetic.sign(pData.velo.x);
		// effect of movement
		decelConst += accelConst * movementMulti * Input.moveX;

		if (pData.grounded) {
			decelConst *= 1.4;
		}
		pData.velo.x += decelConst;
		hasGravity = true;
	}

	private void fireGun(Vector2f firePos) {
		if (stats.stamina < gunCost) {
			return;
		}

		stats.stamina -= gunCost;

		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));

		Projectile proj = new Projectile("PROJECTILE", pos, "Bullet"); // initializes bullet
																		// entity

		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f velo = new Vector2f(dir).mul(3);

		proj.pData.velo = new Vector2f(velo);
		proj.alignment = alignment;

		GameManager.subscribeEntity(proj);
	}

	private void melee(Vector2f firePos) {
		int meleedis = 50;// hardcode TODO
		Vector2f kbDir = new Vector2f(sideFacing, 0);

		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32)); // Hardcoded fun
		pos.sub(16, 16); // TODO: Fix hardcode

		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f dist = new Vector2f(dir).mul(meleedis);

		Vector2f mPos = new Vector2f(pos).add(dist);

		// TODO: Retrieve this from the lookup
		meleeEntity = new Melee("MELEE", mPos, "Melee", this, kbDir);
		GameManager.subscribeEntity(meleeEntity);

		float angle = Math.atan2(dir.y, dir.x);
		Matrix4f rot = meleeEntity.transform.rot;

		rot.translate(meleeEntity.dim.x / 2, meleeEntity.dim.y / 2, 0);
		rot.rotateZ(angle);
		rot.translate(-meleeEntity.dim.x / 2, -meleeEntity.dim.y / 2, 0);

		meleeTimer = new Timer(200, new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				// TODO: unsubscribing takes time while setting melee entity to null is instant,
				// so there is one frame where melee entity is not attached to the player.
				GameManager.unsubscribeEntity(meleeEntity);
				meleeEntity = null;
				meleeTimer = null;
			}
		});
	}

	@Override
	public void calcFrame() {
		super.calcFrame();

		// Scale to the side facing
		if (sideFacing != 0) {
			Matrix4f scale = transform.scale;

			// These are applied bottom row to top row
			scale.identity().translate(rendDims.x / 2, 0, 0);
			scale.scale(sideFacing, 1, 1);
			scale.translate(-rendDims.x / 2, 0, 0);
		}

		// Update trailing particle system
		pSys.activeSubTex = anim.currentAnim.getFrame();
		pSys.activeTransform = new Transformation(renderer.transform);
		pSys.update();
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
