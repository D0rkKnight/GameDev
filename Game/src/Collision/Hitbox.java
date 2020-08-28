package Collision;

public class Hitbox {
	public float height;
	public float width;
	public Object owner;
	
	public Hitbox(Object owner, float height, float width) {
		this.height = height;
		this.width = width;
		this.owner = owner;
		
		//Any class with a hitbox MUST implement Collidable
		if (!(owner instanceof Collidable)) {
			new Exception("Owner does not implement Collidable.").printStackTrace();
			System.exit(1);
		}
	}
	
	public Hitbox(Hitbox hb, Object owner) {
		this.height = hb.height;
		this.width = hb.width;
		this.owner = owner;
	}
	
	/**
	 * Special kinds of hitboxes may not propagate the hit to its owner
	 * @param hb
	 */
	public void hitBy(Hitbox hb) {
		((Collidable) owner).onHit(hb);
	}
}
