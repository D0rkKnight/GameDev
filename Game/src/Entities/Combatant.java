package Entities;

import org.joml.Vector2f;

import GameController.GameManager;
import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;

public abstract class Combatant extends PhysicsEntity {
	protected Stats stats;
	protected Player p;

	public Combatant(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name);
		this.stats = stats;
		
		p = GameManager.player;
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
		// These two whiles make sure degrees is within 0-360
		while (direction > 360) {
			direction -= 360;
		}
		while (direction < 0) {
			direction += 360;
		}
		// this if else accounts for the angle being to the right/left
		if (direction <= 180) {
			velo.x += knockback * Math.cos(Math.toRadians((90 - direction)));
		} else {
			velo.x -= knockback * Math.cos(Math.toRadians((270 - direction)));
		}
		// and top/bot
		if (direction < 90 || direction > 270) {
			velo.x += knockback * Math.cos(Math.toRadians(direction));
		} else {
			velo.x -= knockback * Math.cos(Math.toRadians(180 - direction));
		}
	}
	
	public abstract void attack();

	// just sets stats.isDying to true
	public abstract void die();
}
