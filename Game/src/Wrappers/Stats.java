package Wrappers;

import GameController.EntityData;

public class Stats {
	public float health;
	public float maxHealth;
	public float healthRegen;
	public float stamina;
	public float maxStamina;
	public float staminaRegen;
	public int invulnerable;
	// for both enemies and the player, each of their attacks is designated with a
	// number
	public int[] attacks;
	public boolean isDying;

	public Stats(float maxHealth, float maxStamina, float healthRegen, float staminaRegen) {
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.stamina = maxStamina;
		this.maxStamina = maxStamina;
		this.staminaRegen = staminaRegen;
	}

	public Stats(Stats stats) {
		this(stats.maxHealth, stats.maxStamina, stats.healthRegen, stats.staminaRegen);
	}

	public static Stats fromED(EntityData vals) {
		float maxHealth = vals.d.containsKey("maxHealth") ? vals.fl("maxHealth") : 100;
		float maxStamina = vals.d.containsKey("maxStamina") ? vals.fl("maxStamina") : 100;
		float healthRegen = vals.d.containsKey("healthRegen") ? vals.fl("healthRegen") : 0;
		float staminaRegen = vals.d.containsKey("staminaRegen") ? vals.fl("staminaRegen") : 0;

		Stats s = new Stats(maxHealth, maxStamina, healthRegen, staminaRegen);

		return s;
	}
}
