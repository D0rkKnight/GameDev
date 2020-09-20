package Wrappers;

public class Stats implements Cloneable {
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
}
