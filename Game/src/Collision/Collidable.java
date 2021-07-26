package Collision;

public interface Collidable {

	public void onColl(Collider otherHb);
	
	public Collider getHb();
	public void setHb(Collider hb);
}
