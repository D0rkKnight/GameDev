package GameController;

import Entities.Entity;
import Wrappers.Rect;
import Wrappers.Vector2;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2 pos;
	public Rect viewport;
	
	public Camera(Entity target) {
		if (main == null) main = this;
		
		this.target = target;
		pos = target.getPosition();
		
		viewport = GameManager.GetWindowSize();
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
