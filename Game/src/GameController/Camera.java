package GameController;

import Entities.Entity;
import Wrappers.Vector2;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2 pos;
	private float viewportW;
	private float viewportH;
	
	public Camera(Entity target) {
		if (main == null) main = this;
		
		this.target = target;
		pos = target.getPosition();
	}
	
	public void update() {
		try {
			pos = target.getPosition().clone();
			pos.x += target.dim.w/2;
			pos.y += target.dim.h/2;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
}
