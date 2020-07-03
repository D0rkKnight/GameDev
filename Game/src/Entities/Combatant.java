package Entities;

import Rendering.Renderer;
import Wrappers.Vector2;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends Entity {
	protected Stats stats;
	protected float yVelocity;
	protected float xVelocity;
	protected float yAcceleration;

	public Combatant(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name);
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
	public void hit(int damage, float direction, float knockback) {
		stats.health -= damage;
		// These two ifs make sure degrees is within 0-360
		if (direction > 360) {
			direction -= 360;
		}
		if (direction < 0) {
			direction += 360;
		}
		// this if else accounts for the angle being to the right/left
		if (direction <= 180) {
			xVelocity += knockback * Math.cos(Math.toRadians((90 - direction)));
		} else {
			xVelocity -= knockback * Math.cos(Math.toRadians((270 - direction)));
		}
		// and top/bot
		if (direction < 90 || direction > 270) {
			yVelocity += knockback * Math.cos(Math.toRadians(direction));
		} else {
			yVelocity -= knockback * Math.cos(Math.toRadians(180 - direction));
		}
	}

	public abstract void attack();

	// just sets stats.isDying to true
	public abstract void die();

}
