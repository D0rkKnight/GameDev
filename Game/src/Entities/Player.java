package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.HammerShape;
import Debug.Debug;
import GameController.GameManager;
import GameController.Input;
import Math.Arithmetic;
import Rendering.Animation;
import Rendering.Animator;
import Rendering.SpriteRenderer;
import Rendering.Texture;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Timer;
import Wrappers.TimerCallback;

public class Player extends Combatant{
	
	private float jumpSpeed;
	private Timer gunTimer;
	float xCap = 0.5f; //todo add to contructor (and break everything)
	float accelConst = 2f / 300f;
	
	private float dashSpeed;
	private long dashDuration;
	private Timer dashTimer;
	private Vector2f dashDir;
	private boolean releasedJump = true; //for making sure the player can't hold down w to jump
	private boolean justDashed = false;
	
	private Animator anim;
	
	private boolean canJump;
	private long jumpGraceInterval = 100;
	private Timer jumpGraceTimer;
	
	public Player(int ID, Vector2f position, SpriteRenderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(15f, 60f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 0, 0, 0));
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
		
		SpriteRenderer sprRend = (SpriteRenderer) renderer;
		switch (movementMode) {
		case MOVEMENT_MODE_CONTROLLED: //walking
			sprRend.col = new Color(1, 0, 0);
			controlledMovement();
			break;
		case MOVEMENT_MODE_IS_DASHING: //dashing
			sprRend.col = new Color(1, 1, 1);
			dashingMovement();
			break;
		case MOVEMENT_MODE_KNOCKBACK:
			sprRend.col = new Color(0, 0, 1);
			knockbackMovement();
			break;
		default:
			System.err.println("Movement mode not set.");
		}

		//Gravity
		if (hasGravity) {
			pData.velo.y -= Entity.gravity * GameManager.deltaT() / 1300;
			pData.velo.y = Math.max(pData.velo.y, -2);
			if(justDashed) {
				pData.velo.y -= Entity.gravity * GameManager.deltaT() / 1300;
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
			if( false ) {//TODO colliding with wall
				//TODO velo.x += xCap;
			}
			
			canJump = false;
		}
		
		//Shoot a gun
		if (Input.primaryButtonDown) {
			gunTimer.update();
		}
		
		//Update dash timer
		if (dashTimer != null) {
			dashTimer.update();
		}
		
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
		
		
		float accelConst = 2f / 300f;
		
		float xAccel = 0;
		
		//Deceleration
		if (Input.moveX != 0) {
			xAccel = accelConst * Input.moveX;
		} else {
			//Reduce jitter (divide by deltaT to balance out equation)
			float decelConst = Math.min(accelConst, Math.abs(pData.velo.x) / GameManager.deltaT());
			
			xAccel = -decelConst * Arithmetic.sign(pData.velo.x);
		}
		xAccel *= GameManager.deltaT();
		if(Math.abs(pData.velo.x) <= xCap) {
			pData.velo.x += xAccel;
		
			//cap velo
			if (Input.moveX > 0) pData.velo.x = Math.min(pData.velo.x, xCap);
			if (Input.moveX < 0) pData.velo.x = Math.max(pData.velo.x, -xCap);
		}
		else {
			float decelConst = (Math.max(xCap, Math.abs(pData.velo.x) - 2 * accelConst) / GameManager.deltaT());
			pData.velo.x -= decelConst * Arithmetic.sign(pData.velo.x);
		}
		if(Input.moveY == 0) {
			releasedJump = true;
		}
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
		decelConst *= GameManager.deltaT();
		
		pData.velo.x += decelConst;
		hasGravity = true;
	}
	
	private void fireGun(Vector2f firePos) {
		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));
		
		Projectile proj = new Projectile(0, pos, GameManager.renderer, "Bullet"); //initializes bullet entity
		
		SpriteRenderer rend = (SpriteRenderer) proj.renderer;
		rend.spr = Debug.debugTex;
		
		Vector2f dir = new Vector2f(firePos).sub(pos).normalize();
		Vector2f velo = new Vector2f(dir).mul(3);
		
		proj.pData.velo = new Vector2f(velo);
		proj.alignment = alignment;
		
		GameManager.subscribeEntity(proj);
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
}
