package GameController;

import Entities.Entity;
import Wrappers.Rect;
import Wrappers.Vector2;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2 pos;
	public Rect viewport;
	
	public Camera() {
		if (main == null) main = this;
		
		pos = new Vector2(0f, 0f);
		
		viewport = GameManager.GetWindowSize();
		viewport.w /= 2;
		viewport.h /= 2;
	}
	
	public void attach(Entity target) {
		this.target = target;
		pos = target.getPosition();
	}
	
	public void update() {
		//Change the viewport
		
		
	}
}
