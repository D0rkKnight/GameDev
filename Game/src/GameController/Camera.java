package GameController;

import org.joml.Vector2f;

import Entities.Entity;
import Wrappers.Arithmetic;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2f pos; //Camera is centered on position
	public Vector2f viewport;
	
	private float moveSpeed;
	
	public Camera() {
		if (main == null) main = this;
		
		pos = new Vector2f(0f, 0f);
		moveSpeed = 0.5f;
		
		//TODO: Resolve this with input's stuff
		viewport = Drawer.GetWindowSize();
		viewport.x /= 2;
		viewport.y /= 2; //??? Why?
		//TODO: Fix this stuff
	}
	
	public void attach(Entity target) {
		this.target = target;
	}
	
	public void update() {
		//Change the viewport
		Vector2f tPos = target.getPosition();
		pos.x = Arithmetic.lerp(pos.x, tPos.x, moveSpeed);
		pos.y = Arithmetic.lerp(pos.y, tPos.y, moveSpeed);
		
		//Update, since the mouse's world space has changed
		Input.updateCursor(Input.mouseScreenPos.x, Input.mouseScreenPos.y);
	}
}
