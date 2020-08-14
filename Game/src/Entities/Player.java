package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.HammerShape;
import GameController.GameManager;
import GameController.Input;
import Rendering.Animation;
import Rendering.Animator;
import Rendering.SpriteRenderer;
import Rendering.Texture;
import Wrappers.Arithmetic;
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
	private Vector2f knockbackDir; //for debug purposes, only shows initial knockback 
	private boolean releasedJump = true; //for making sure the player can't hold down w to jump
	
	//knockback only dependent on velocity: reduces effect of movement keys (not completely disabling), reduced velocity every frame, changes to normal movement when at normal velocities
	private float movementMulti; //multiplier for movement when knocked back (suggest 0.5)
	private float decelMulti; //multiplier for decel when knocked back (suggest 1)
	private boolean knockback = true;
	
	private int movementMode;
	private boolean hasGravity;
	
	private static final int MOVEMENT_MODE_CONTROLLED = 1;
	private static final int MOVEMENT_MODE_IS_DASHING = 2;
	private static final int MOVEMENT_MODE_KNOCKBACK = 3;
	
	private Animator anim;
	
	public Player(int ID, Vector2f position, Sprites sprites, SpriteRenderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Vector2f(15f, 60f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer; //Renderer has been duplicated by now
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 0, 0));
		renderer = rendTemp;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		jumpSpeed = 1.5f;
		
		dashSpeed = 3f;
		dashDuration = 100;
		movementMode = MOVEMENT_MODE_CONTROLLED;
		
		//Configure firing
		gunTimer = new Timer(100, new TimerCallback() {

			@Override
			public void invoke() {
				fireGun(Input.mouseWorldPos);
			}
			
		});
		
		
		//Configure animation stuff
		Animation[] anims = new Animation[1];
		Texture[] animSheet = Texture.unpackSpritesheet("Assets/ChargingSlime.png", 32, 32);
		anims[0] = new Animation(animSheet);
		anim = new Animator(anims, 24, renderer);
	}

	public void onHit(Hitbox hb) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * applies knockback values. if currently in knockback state, chooses larger values.
	 * @param knockbackDir
	 * @param movementMulti
	 * @param decelMulti
	 */
	public void knockback(Vector2f knockbackDir, float movementMulti, float decelMulti) {
		if(movementMode == MOVEMENT_MODE_KNOCKBACK) {
			if(Math.abs(velo.x) < Math.abs(knockbackDir.x)) {
				velo.x = knockbackDir.x;
				this.knockbackDir.x = knockbackDir.x;
			}
			if(Math.abs(velo.y) < Math.abs(knockbackDir.y)) {
				velo.y = knockbackDir.y;
				this.knockbackDir.y = knockbackDir.y;
			}
			if(this.movementMulti < movementMulti) this.movementMulti = movementMulti;
			if(this.decelMulti < decelMulti) this.decelMulti = decelMulti;
		}
		else {
			velo.x = knockbackDir.x;
			velo.y = knockbackDir.y;
			this.knockbackDir = new Vector2f(knockbackDir);
			this.movementMulti = movementMulti;
			this.decelMulti = decelMulti;
			knockback = true;
		}
		
		
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
			velo.y -= Entity.gravity * GameManager.deltaT() / 1000;
			velo.y = Math.max(velo.y, -3);
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
		if(movementMode == MOVEMENT_MODE_KNOCKBACK && Math.abs(velo.x) < xCap) {
			movementMode = MOVEMENT_MODE_CONTROLLED;
			knockbackDir = null;
		}
		if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0) && movementMode != MOVEMENT_MODE_IS_DASHING) {
			movementMode = MOVEMENT_MODE_IS_DASHING;
			dashDir = new Vector2f(Input.moveX, Input.moveY).normalize();
			
			//Set velocity here
			velo = new Vector2f(dashDir).mul(dashSpeed);
			
			//Begin a timer
			dashTimer = new Timer(dashDuration, new TimerCallback() {

				@Override
				public void invoke() {
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
			float decelConst = Math.min(accelConst, Math.abs(velo.x) / GameManager.deltaT());
			
			xAccel = -decelConst * Arithmetic.sign(velo.x);
		}
		xAccel *= GameManager.deltaT();
		
		velo.x += xAccel;
		
		//cap velo
		if (Input.moveX > 0) velo.x = Math.min(velo.x, xCap);
		if (Input.moveX < 0) velo.x = Math.max(velo.x, -xCap);
		if(Input.moveY == 0) {
			releasedJump = true;
		}
		
		if (grounded && Input.moveY == 1 && releasedJump) { // if player is colliding with ground underneath and digital input detected
										// (space pressed)
			velo.y = jumpSpeed;
			
			//TODO: Rename this so its purpose is less vague.
			isJumping = true; //Signals to the physics system that some operations ought to be done
			releasedJump = false;
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
		float decelConst = Math.min(accelConst * decelMulti, Math.abs(velo.x) - xCap) * -Arithmetic.sign(velo.x);
		//effect of movement
		decelConst += accelConst * movementMulti * Input.moveX;
		decelConst *= GameManager.deltaT();
		
		velo.x += decelConst;
		hasGravity = true;
	}
	
	private void fireGun(Vector2f firePos) {
		Vector2f pos = new Vector2f(position).add(new Vector2f(8, 32));
		
		Projectile proj = new Projectile(0, pos, null, GameManager.renderer, "Bullet"); //initializes bullet entity
		Vector2f dir = new Vector2f(firePos).sub(position).normalize();
		
		Vector2f velo = new Vector2f(dir).mul(3);
		
		proj.velo = new Vector2f(velo);
		
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
