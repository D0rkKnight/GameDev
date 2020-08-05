package Entities;

import Collision.HammerShape;
import GameController.GameManager;
import GameController.Input;
import Rendering.SpriteRenderer;
import Wrappers.Arithmetic;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Rect;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Timer;
import Wrappers.TimerCallback;
import Wrappers.Vector2;

public class Player extends Combatant{
	
	private float jumpSpeed;
	private Timer gunTimer;
	
	private float dashSpeed;
	private long dashDuration;
	private Timer dashTimer;
	private Vector2 dashDir;
	
	private int movementMode;
	private boolean hasGravity;
	
	private static final int MOVEMENT_MODE_CONTROLLED = 1;
	private static final int MOVEMENT_MODE_IS_DASHING = 2;
	
	public Player(int ID, Vector2 position, Sprites sprites, SpriteRenderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name, stats);
		
		//Configure the renderer real quick
		dim = new Rect(16f, 64f);
		SpriteRenderer rendTemp = (SpriteRenderer) this.renderer;
		rendTemp.init(position, dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color(1, 0, 0));
		renderer = rendTemp;
		
		this.renderer.linkPos(this.position);
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.w, dim.h);
		
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
		determineMovementMode();
		
		SpriteRenderer sprRend = (SpriteRenderer) renderer;
		switch (movementMode) {
		case MOVEMENT_MODE_CONTROLLED:
			sprRend.col = new Color(1, 0, 0);
			controlledMovement();
			break;
		case MOVEMENT_MODE_IS_DASHING:
			sprRend.col = new Color(1, 1, 1);
			dashingMovement();
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
	}
	
	private void determineMovementMode() {
		if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0) && movementMode != MOVEMENT_MODE_IS_DASHING) {
			movementMode = MOVEMENT_MODE_IS_DASHING;
			dashDir = new Vector2(Input.moveX, Input.moveY).unit();
			
			//Set velocity here
			velo = dashDir.mult(dashSpeed);
			
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
	public void controlledMovement() {// TODO add collision
		
		float xCap = 0.5f;
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
		
		if (grounded && Input.moveY == 1) { // if player is colliding with ground underneath and digital input detected
										// (space pressed)
			velo.y = jumpSpeed;
			
			//TODO: Rename this so its purpose is less vague.
			isJumping = true; //Signals to the physics system that some operations ought to be done
		}
		
		hasGravity = true;
	}
	
	private void dashingMovement()
	{
		hasGravity = false;
	}
	
	private void fireGun(Vector2 firePos) {
		System.out.println(firePos.toString());
		
		Vector2 pos = position.add(new Vector2(8, 32));
		
		Projectile proj = new Projectile(0, pos, null, GameManager.renderer, "Bullet");
		Vector2 dir = firePos.sub(position).unit();
		
		System.out.println(Input.mouseWorldPos.toString());
		
		Vector2 velo = dir.mult(3);
		
		proj.velo = new Vector2(velo);
		
		GameManager.subscribeEntity(proj);
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	public void onTileCollision() {
		// TODO Auto-generated method stub
		
	}
}
