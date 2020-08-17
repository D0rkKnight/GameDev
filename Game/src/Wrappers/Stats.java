package Wrappers;

public class Stats implements Cloneable{
	public int health;
	public int maxHealth;
	public int stamina;
	public int invulnerable;
	//for both enemies and the player, each of their attacks is designated with a number
	public int[] attacks;
	public boolean isDying;
	
	/**
	 * TODO: Fix this heap of spaghetti
	 */
	
	public Stats(int maxHealth) {
		this.maxHealth = maxHealth;
		this.health = maxHealth;
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
