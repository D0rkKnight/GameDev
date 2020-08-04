package GameController;

import Entities.Entity;
import Wrappers.Arithmetic;
import Wrappers.Rect;
import Wrappers.Vector2;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2 pos;
	public Rect viewport;
	
	private float moveSpeed;
	
	public Camera() {
		if (main == null) main = this;
		
		pos = new Vector2(0f, 0f);
		moveSpeed = 1f;
		
		//TODO: Resolve this with input's stuff
		viewport = Drawer.GetWindowSize();
		viewport.w /= 2;
		viewport.h /= 2;
	}
	
	public void attach(Entity target) {
		this.target = target;
	}
	
	public void update() {
		//Change the viewport
		Vector2 tPos = target.getPosition();
		pos.x = Arithmetic.lerp(pos.x, tPos.x, moveSpeed);
		pos.y = Arithmetic.lerp(pos.y, tPos.y, moveSpeed);
		
		//Update, since the mouse's world space has changed
		Input.updateCursor(Input.mouseScreenPos.x, Input.mouseScreenPos.y);
	}
	
	/**
	 * Returns clipped vertex values
	 * TODO: Do this with matrices instead
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector2 mapVert(float x, float y) {
		return mapVert(new Vector2(x, y));
	}
	
	public Vector2 mapVert(Vector2 p) {
		//View step of rendering
		Vector2 out = p.sub(Camera.main.pos);
		
		
		//Clip step of rendering (simple, since we're in an orthographic mode.
		out.x /= Camera.main.viewport.w;
		out.y /= Camera.main.viewport.h;
		
		return out;
	}
}
