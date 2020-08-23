package Collision;

public interface Collidable {

	public void onHit(Hitbox otherHb);
	
	public Hitbox getHb();
	public void setHb(Hitbox hb);
}
