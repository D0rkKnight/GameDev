package Entities.Framework;

import org.joml.Vector2f;

import Graphics.Rendering.GeneralRenderer;
import Utility.Timers.FlickerTimer;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;
import Wrappers.Stats;

public abstract class Combatant extends PhysicsEntity {
	public Stats stats;
	protected Timer hurtTimer;
	private Timer invulnTimer;
	protected boolean isInvuln = false;
	protected boolean baseInvulnState = false;
	protected int baseInvulnLength = 10;

	public Combatant(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name);
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

		GeneralRenderer sprRen = (GeneralRenderer) renderer;
		sprRen.updateColors(new Color(1, 0, 0));

		hurtTimer = new FlickerTimer(500, 50, new Color(1, 1, 1), new Color(1, 0, 0), this, new TimerCallback() {
			@Override
			public void invoke(Timer timer) {
				hurtTimer = null; // Color reset is handled
			}
		});
	}

	// Don't make these abstract
	// just sets stats.isDying to true
	public void die() {

	}

	public void checkForDeath() {
		if (stats.health <= 0)
			Destroy();
	}

	@Override
	public void calculate() {
		super.calculate();

		if (hurtTimer != null)
			hurtTimer.update();
		if (invulnTimer != null)
			invulnTimer.update();
		regen();
	}

	public void regen() {
		stats.health = Math.min(stats.health + stats.healthRegen, stats.maxHealth);
		stats.stamina = Math.min(stats.stamina + stats.staminaRegen, stats.maxStamina);
	}

	public void attack() {

	}

	public static Alignment getOpposingAlignment(Alignment align) {
		switch (align) {
		case PLAYER:
			return Alignment.ENEMY;
		case ENEMY:
			return Alignment.PLAYER;
		default:
			return null;
		}
	}

	public void invuln(int len) {
		isInvuln = true;
		invulnTimer = new Timer(len, new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				isInvuln = baseInvulnState;
				invulnTimer = null;
			}
		});
	}

	public void invuln() {
		invuln(baseInvulnLength);
	}

	public boolean isInvuln() {
		return isInvuln;
	}
}
