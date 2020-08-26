package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.HammerShape;
import Collision.Hitbox;
import Collision.PhysicsCollisionBehaviorStepUp;
import Collision.PhysicsCollisionBehaviorWallCling;
import Debug.Debug;
import GameController.GameManager;
import GameController.Input;
import Math.Arithmetic;
import Rendering.Animation;
import Rendering.Animator;
import Rendering.GeneralRenderer;
import Rendering.Texture;
import Rendering.Transformation;
import Wrappers.Color;
import Wrappers.Stats;
import Wrappers.Timer;
import Wrappers.TimerCallback;

public class Player extends Combatant{
	
	private float jumpSpeed;
	private Timer gunTimer;
	float xCap = 0.5f; //todo add to contructor (and break everything)
	float accelConst = 2f / 20f;
	float gunCost = 15f;
	float dashCost = 30f;
	
	private float dashSpeed;
	private long dashDuration;
	private Timer dashTimer;
	private Vector2f dashDir;
	private boolean releasedJump = true; //for making sure the player can't hold down w to jump
	private boolean justDashed = false;
	
	private Timer meleeTimer;
	private Melee meleeEntity;
	
	private Animator anim;
	
	private boolean canJump;
	private long jumpGraceInterval = 100;
	private Timer jumpGraceTimer;
	private GeneralRenderer srenderer;
	
	private int sideFacing;
	
	public Player(int ID, Vector2f position, GeneralRenderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(15f, 60f);
		srenderer = renderer;
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 0, 0, 0));
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		jumpSpeed = 1f;
		
		dashSpeed = 2f;
		dashDuration = 50;
		movementMode = MOVEMENT_MODE_CONTROLLED;
		
		//Configure firing
		gunTimer = new Timer(100, new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				fireGun(Input.mouseWorldPos);
			}
			
		});
		
		
		//Configure animation stuff
		Animation[] anims = new Animation[1];
		Texture[] animSheet = Texture.unpackSpritesheet("Assets/ChargingSlime.png", 32, 32);
		anims[0] = new Animation(animSheet);
		anim = new Animator(anims, 24, renderer);
		
		//Alignment
		alignment = ALIGNMENT_PLAYER;
		
		sideFacing = 1;
	}
	
	protected void initPhysicsCollBehavior() {
		super.initPhysicsCollBehavior();
		
		collBehaviorList.add(new PhysicsCollisionBehaviorStepUp());
		collBehaviorList.add(new PhysicsCollisionBehaviorWallCling());
	}

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
	
	public void calculate() {
		super.calculate();
		
		determineMovementMode(); //determine what movement mode and execute it
		
		GeneralRenderer sprRend = (GeneralRenderer) renderer;
		switch (movementMode) {
		case MOVEMENT_MODE_CONTROLLED: //walking
			sprRend.updateColors(new Color(1, 0, 0));
			controlledMovement();
			break;
		case MOVEMENT_MODE_IS_DASHING: //dashing, a bit spaghetti here TODO
			sprRend.updateColors(new Color(1, 1, 1));
			dashingMovement();
			break;
		case MOVEMENT_MODE_KNOCKBACK:
			sprRend.updateColors(new Color(0, 0, 1));
			knockbackMovement();
			break;
		default:
			System.err.println("Movement mode not set.");
		}

		//Gravity
		if (hasGravity) {
			pData.velo.y -= Entity.gravity / 100;
			pData.velo.y = Math.max(pData.velo.y, -2);
			
			//Bring entity back to normal
			if(justDashed) {
				pData.velo.y -= Entity.gravity / 100;
				if(pData.velo.y < 0) justDashed = false;
			}
	    }
		
		//Jump
		if (jumpGraceTimer != null) jumpGraceTimer.update();
		if (canJump && Input.moveY == 1 && releasedJump) {
			pData.velo.y = jumpSpeed;
			
			//TODO: Rename this so its purpose is less vague.
			pData.isJumping = true; //Signals to the physics system that some operations ought to be done
			releasedJump = false;
			
			canJump = false;
		}
		
		//Shoot a gun
		if (Input.primaryButtonDown) {
			gunTimer.update();
		}
		
		//Melee
		if (Input.meleeAction && meleeTimer == null) {
			melee();
		}
		
		//Update timers
		if (meleeTimer != null) {
			meleeTimer.update();
		}
		if (dashTimer != null) dashTimer.update();
		
		calcFrame();
	}
	
	private void determineMovementMode() {
		if(Input.knockbackTest && movementMode == MOVEMENT_MODE_CONTROLLED ) {//TODO
			knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
		}
		if(knockback) { 
			movementMode = MOVEMENT_MODE_KNOCKBACK;
			knockback = false;
		}
		if(movementMode == MOVEMENT_MODE_KNOCKBACK && Math.abs(pData.velo.x) < xCap) {
			movementMode = MOVEMENT_MODE_CONTROLLED;
			knockbackDir = null;
		}
		if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0) && movementMode != MOVEMENT_MODE_IS_DASHING) {
			if(stats.stamina < dashCost) {
				System.out.println("out of stamina, can't dash");
				return;
			}
			
			stats.stamina -= dashCost;
			justDashed = true;
			movementMode = MOVEMENT_MODE_IS_DASHING;
			dashDir = new Vector2f(Input.moveX, Input.moveY).normalize();
			
			//Set velocity here
			pData.velo = new Vector2f(dashDir).mul(dashSpeed);
			
			//Begin a timer
			dashTimer = new Timer(dashDuration, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					//First, dump this timer
					dashTimer = null;
					
					//Now stop dashing
					movementMode = MOVEMENT_MODE_CONTROLLED;
				}
				
			});
		}
		
	}

	@Override
	public void controlledMovement() {
		float xAccel = 0;
		
		//Deceleration
		if (Input.moveX != 0) {
			xAccel = accelConst * Input.moveX;
		} else {
			float decelConst = Math.min(accelConst, Math.abs(pData.velo.x));
			
			xAccel = -decelConst * Arithmetic.sign(pData.velo.x);
		}
		if(Math.abs(pData.velo.x) <= xCap) {
			pData.velo.x += xAccel;
		
			//cap velo
			if (Input.moveX > 0) pData.velo.x = Math.min(pData.velo.x, xCap);
			if (Input.moveX < 0) pData.velo.x = Math.max(pData.velo.x, -xCap);
		}
		else {
			float decelConst = (Math.max(xCap, Math.abs(pData.velo.x) - 2 * accelConst));
			pData.velo.x -= decelConst * Arithmetic.sign(pData.velo.x);
		}
		if(Input.moveY == 0) {
			releasedJump = true;
		}
		
		//Jump grace
		if (pData.grounded && movementMode != MOVEMENT_MODE_IS_DASHING) canJump = true;
		else if (pData.wasGrounded) {
			//begin timer
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
		//Can only change sides if not attacking
		if (newSideFacing != 0 && meleeTimer == null) sideFacing = newSideFacing;
		
		hasGravity = true;
	}

	
	private void dashingMovement()
	{
		hasGravity = false;
	}
	/**
	 * I'm assuming that knockbackSpeed was set to not 0 upon being hit
	 * also assuming that hit function added velo already, this function just does deacceleration
	 */
	private void knockbackMovement() {
		//automatic deacceleration
		float decelConst = Math.min(accelConst * decelMulti, Math.abs(pData.velo.x) - xCap) * -Arithmetic.sign(pData.velo.x);
		//effect of movement
		decelConst += accelConst * movementMulti * Input.moveX;
		
		pData.velo.x += decelConst;
		hasGravity = true;
	}
	
	private void fireGun(Vector2f firePos) {
		if(stats.stamina < gunCost) {
			System.out.println("out of stamina, can't fire");
			return;
		}
		
		stats.stamina -= gunCost;
		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));
		
		Projectile proj = new Projectile(0, pos, GameManager.renderer, "Bullet"); //initializes bullet entity
		
		GeneralRenderer rend = (GeneralRenderer) proj.renderer;
		rend.spr = Debug.debugTex;
		
		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f velo = new Vector2f(dir).mul(3);
		
		proj.pData.velo = new Vector2f(velo);
		proj.alignment = alignment;
		
		GameManager.subscribeEntity(proj);
	}
	
	private void melee() {
		Vector2f kbDir = new Vector2f(sideFacing, 0);
		meleeEntity = new Melee(1, new Vector2f(position), GameManager.renderer, "Melee", this, kbDir);
		GameManager.subscribeEntity(meleeEntity);
		
		meleeTimer = new Timer(200, new TimerCallback() {
			
			@Override
			public void invoke(Timer timer) {
				// TODO: unsubscribing takes time while setting melee entity to null is instant, so there is one frame where melee entity is not attached to the player.
				GameManager.unsubscribeEntity(meleeEntity);
				meleeEntity = null;
				meleeTimer = null;
			}
		});
	}
	
	public void pushMovement() {
		super.pushMovement();
		
		//Dragging melee box along, after collision has been resolved.
		if (meleeEntity != null) {
			//Update melee entity (drag it along with the character)
			Vector2f centerD = new Vector2f(-dim.x/2, 15);
			Vector2f sideD = new Vector2f(30, 0).mul(sideFacing);
			
			meleeEntity.position.set(position).add(centerD).add(sideD);;
		}
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
		//Just a simple animation update, nothing spicy.
		anim.update();
	}

	public void onTileCollision() {
		// TODO Auto-generated method stub
		
	}
	public Entity clone() {
		try {
			return new Player(ID, new Vector2f(position.x, position.y), srenderer.clone(), name, stats.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Entity clone(float xPos, float yPos) {
		try {
			return new Player(ID, new Vector2f(xPos, yPos), srenderer.clone(), name, stats.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
