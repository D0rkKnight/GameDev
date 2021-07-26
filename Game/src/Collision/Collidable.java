package Collision;

public interface Collidable {

	public void onColl(Collider otherHb);
	
	public Collider getColl();
	public void setColl(Collider hb);
}
