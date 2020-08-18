package Wrappers;

public class Stats implements Cloneable{
	public float health;
	public float maxHealth;
	public float healthRegen;
	public float stamina;
	public float maxStamina;
	public float staminaRegen;
	public int invulnerable;
	//for both enemies and the player, each of their attacks is designated with a number
	public int[] attacks;
	public boolean isDying;
	
	/**
	 * TODO: Fix this heap of spaghetti
	 */
	
	public Stats(float maxHealth, float maxStamina, float healthRegen, float staminaRegen) {
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.stamina = maxStamina;
		this.maxStamina = maxStamina;
		this.staminaRegen = staminaRegen;
	}
	
	public Stats clone() {
		Stats newStats = null;
		try {
			newStats = (Stats) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return newStats;
	}
}
