package Entities;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Collision.Hitbox;
import Collision.Behaviors.PhysicsCollisionBehaviorStepUp;
import Collision.Behaviors.PhysicsCollisionBehaviorWallCling;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Framework.Combatant;
import Entities.Framework.Entity;
import Entities.Framework.Melee;
import Entities.Framework.Projectile;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Animation.PlayerAnimator;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteSheet;
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

	private Animator anim;

	private boolean canJump;
	private long jumpGraceInterval = 100;
	private Timer jumpGraceTimer;

	private int sideFacing;

	public Player(int ID, Vector2f position, GeneralRenderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);

		// Configure the renderer real quick
		((GeneralRenderer) this.renderer).init(new Transformation(position), new Vector2f(96, 96),
				HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 0, 0, 0));
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		dim = new Vector2f(15f, 60f);
		hitbox = new Hitbox(this, dim.x, dim.y);

		jumpSpeed = 1f;

		dashSpeed = 2f;
		dashDuration = 80;
		movementMode = MOVEMENT_MODE_CONTROLLED;

		// Configure animation stuff
		SpriteSheet animSheet = Texture.getSprSheet("Assets/Sprites/Ilyia_idle-running_proto.png", 96, 96);
		Animation[] anims = new Animation[3];
		anims[Animator.ANIM_IDLE] = new Animation(animSheet.getRow(0, 4));
		anims[PlayerAnimator.ANIM_ACCEL] = new Animation(animSheet.getRow(1, 12));
		anims[PlayerAnimator.ANIM_MOVING] = new Animation(animSheet.getRow(2, 8));
		rendOffset.set(-35, 0);

		anim = new PlayerAnimator(anims, 12, (GeneralRenderer) this.renderer, this);

		// Alignment
		alignment = ALIGNMENT_PLAYER;

		sideFacing = 1;
	}

	@Override
	public Combatant createNew(float xPos, float yPos, Stats stats) {
		return new Player(ID, new Vector2f(xPos, yPos), (GeneralRenderer) renderer, name, stats);
	}

	@Override
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();

		collBehaviorList.add(new PhysicsCollisionBehaviorStepUp());
		collBehaviorList.add(new PhysicsCollisionBehaviorWallCling());
	}

	@Override
	public void onHit(Hitbox hb) {
		// TODO Auto-generated method stub

	}

	@Override
	public void attack() {
		// TODO Auto-generated method stub

	}

	@Override
	public void die() {
		// TODO Auto-generated method stub

	}

	@Override
	public void calculate() {
		super.calculate();

		determineMovementMode(); // determine what movement mode and execute it

		GeneralRenderer sprRend = (GeneralRenderer) renderer;
		switch (movementMode) {
		case MOVEMENT_MODE_CONTROLLED: // walking
			sprRend.updateColors(new Color(1, 0, 0, 1));
			controlledMovement();
			break;
		case MOVEMENT_MODE_IS_DASHING: // dashing, a bit spaghetti here TODO
			sprRend.updateColors(new Color(1, 1, 1, 1));
			dashingMovement();
			break;
		case MOVEMENT_MODE_DECEL:
			sprRend.updateColors(new Color(0, 0, 1, 1));
			decelMovement();
			break;
		default:
			System.err.println("Movement mode not set.");
		}

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

		calcFrame();
	}

	private void determineMovementMode() {
		if (Input.knockbackTest && movementMode == MOVEMENT_MODE_CONTROLLED) {// TODO
			knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
		}
		if (knockback) {
			movementMode = MOVEMENT_MODE_DECEL;
			knockback = false;
		}
		if (movementMode == MOVEMENT_MODE_DECEL && Math.abs(pData.velo.x) <= xCap) {
			movementMode = MOVEMENT_MODE_CONTROLLED;
			knockbackDir = null;
		}
		if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0) && movementMode != MOVEMENT_MODE_IS_DASHING) {
			if (stats.stamina < dashCost) {
				return;
			}

			stats.stamina -= dashCost;
			justDashed = true;
			movementMode = MOVEMENT_MODE_IS_DASHING;
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
					movementMode = MOVEMENT_MODE_CONTROLLED;
				}

			});
		}
		// Bring entity back to normal
		if (movementMode == MOVEMENT_MODE_CONTROLLED && justDashed) {
			pData.velo.y *= 0.2; // TODO hardcode for dash deacc
			pData.velo.x *= 0.8;
			justDashed = false;
			decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
		}

	}

	@Override
	public void controlledMovement() {
		float xAccel = 0;

		// Deceleration
		if (Input.moveX != 0) {
			xAccel = accelConst * Input.moveX;
		} else {
			float decelConst = Math.min(accelConst, Math.abs(pData.velo.x));

			xAccel = -decelConst * Arithmetic.sign(pData.velo.x);
		}
		if (Math.abs(pData.velo.x) <= xCap) {
			pData.velo.x += xAccel;

			// cap velo
			if (Input.moveX > 0)
				pData.velo.x = Math.min(pData.velo.x, xCap);
			if (Input.moveX < 0)
				pData.velo.x = Math.max(pData.velo.x, -xCap);
		} else {
			float decelConst = (Math.max(xCap, Math.abs(pData.velo.x) - 2 * accelConst));
			pData.velo.x -= decelConst * Arithmetic.sign(pData.velo.x);
		}
		if (Input.moveY == 0) {
			releasedJump = true;
		}

		// Jump grace
		if (pData.grounded && movementMode != MOVEMENT_MODE_IS_DASHING)
			canJump = true;
		else if (pData.wasGrounded) {
			// begin timer
			jumpGraceTimer = new Timer(jumpGraceInterval, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					// TODO Auto-generated method stub
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

		Projectile proj = new Projectile(0, pos, GameManager.renderer, "Bullet"); // initializes bullet entity

		GeneralRenderer rend = (GeneralRenderer) proj.renderer;
		rend.spr = Debug.debugTex;

		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f velo = new Vector2f(dir).mul(3);

		proj.pData.velo = new Vector2f(velo);
		proj.alignment = alignment;

		GameManager.subscribeEntity(proj);
	}

	private void melee(Vector2f firePos) {
		int meleedis = 50;//hardcode TODO
		Vector2f kbDir = new Vector2f(sideFacing, 0);
		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));
		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f mPos = new Vector2f(dir.x * meleedis + position.x, dir.y * meleedis 
				+ position.y);
		meleeEntity = new Melee(1, mPos, GameManager.renderer, "Melee", this, kbDir);
		GameManager.subscribeEntity(meleeEntity);
		//float angle = Math.tan(new Vector2f(firePos).sub(pos).y / new Vector2f(firePos).sub(pos).x);
		//meleeEntity.transform.rot.rotateLocalY(angle);
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
	public void pushMovement() {
		super.pushMovement();

		// Dragging melee box along, after collision has been resolved.
		//if (meleeEntity != null) {
			// Update melee entity (drag it along with the character)
			//Vector2f centerD = new Vector2f(-dim.x / 2, 15);
			//Vector2f sideD = new Vector2f(30, 0).mul(sideFacing);

			//meleeEntity.getPosition().set(position).add(centerD).add(sideD);
			;
		//}
	}

	@Override
	protected void calcFrame() {
		// Just a simple animation update, nothing spicy.
		anim.update();

		// Scale to the side facing
		if (sideFacing != 0) {
			Matrix4f scale = transform.scale;

			// These are applied bottom row to top row
			scale.identity().translate(anim.currentAnim.w / 2, 0, 0);
			scale.scale(sideFacing, 1, 1);
			scale.translate(-anim.currentAnim.w / 2, 0, 0);
		}
	}

	@Override
	public void onTileCollision() {
		// TODO Auto-generated method stub

	}
}
