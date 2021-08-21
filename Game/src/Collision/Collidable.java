package Collision;

import java.util.ArrayList;

public interface Collidable {

	public void onColl(Collider otherHb);

	public ArrayList<Collider> getColl();

	public void addColl(Collider hb);

	public void remColl(Collider hb);
}
