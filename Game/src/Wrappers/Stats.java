package Wrappers;

public class Stats {
	public int health;
	public int stamina;
	public int invulnerable;
	//for both enemies and the player, each of their attacks is designated with a number
	public int[] attacks;
	public boolean isDying;
	
	/**
	 * TODO: Fix this heap of spaghetti
	 */
	
	public Stats() {};
	
	public Stats(int health) {
		this.health = health;
	}
	
	public Stats clone() {
		Stats newStats = new Stats();
		newStats.health = health;
		newStats.stamina = stamina;
		newStats.invulnerable = invulnerable;
		if(attacks != null) {
			newStats.attacks = attacks.clone();
		}
		newStats.isDying = isDying;
		return newStats;
	}
}
