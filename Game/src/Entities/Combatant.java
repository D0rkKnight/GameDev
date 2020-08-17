package Entities;

import org.joml.Vector2f;

import Rendering.Renderer;
import Rendering.SpriteRenderer;
import Wrappers.Color;
import Wrappers.FlickerTimer;
import Wrappers.Stats;
import Wrappers.Timer;
import Wrappers.TimerCallback;

public abstract class Combatant extends PhysicsEntity {
	protected Stats stats;
	protected Timer hurtTimer;
	
	public Combatant(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name);
		this.stats = stats;
	}

	/**
	 * knockback in 360 degree dirction starting from top, 90 right, 180 bot hit ta
	 * 
	 * @param damage    damage dealth to player
	 * @param direction direction of knockback force (0 to 360, clockwise, starting
	 *                  from top)
	 * @param knockback force of knockback (in float, change of velocity)
	 * 
	 */
	public void hit(int damage) {
		stats.health -= damage;
		checkForDeath();
		
		SpriteRenderer sprRen = (SpriteRenderer) renderer;
		sprRen.col = new Color(1, 0, 0);
		
		hurtTimer = new FlickerTimer(500, 50, new Color(1,1,1), new Color(1, 0, 0), this, new TimerCallback() {
			@Override
			public void invoke(Timer timer) {
				// TODO Auto-generated method stub
				hurtTimer = null; //Color reset is handled
			}
		});
		
		/**
		 * This is old, right? -- Hanzen
		 */
//		// These two whiles make sure degrees is within 0-360
//		while (direction > 360) {
//			direction -= 360;
//		}
//		while (direction < 0) {
//			direction += 360;
//		}
//		// this if else accounts for the angle being to the right/left
//		if (direction <= 180) {
//			velo.x += knockback * Math.cos(Math.toRadians((90 - direction)));
//		} else {
//			velo.x -= knockback * Math.cos(Math.toRadians((270 - direction)));
//		}
//		// and top/bot
//		if (direction < 90 || direction > 270) {
//			velo.x += knockback * Math.cos(Math.toRadians(direction));
//		} else {
//			velo.x -= knockback * Math.cos(Math.toRadians(180 - direction));
//		}
	}
	
	//TODO: Figure out how to work this out
	// just sets stats.isDying to true
	public abstract void die();
	public void checkForDeath() {
		if (stats.health <= 0) Destroy();
	}
	
	public void calculate() {
		if (hurtTimer != null) hurtTimer.update();
	}
	
	public abstract void attack();
	
	public static int getOpposingAlignment(int align) {
		switch (align) {
		case ALIGNMENT_PLAYER:
			return ALIGNMENT_ENEMY;
		case ALIGNMENT_ENEMY:
			return ALIGNMENT_PLAYER;
		}
		
		return -1;
	}
}
