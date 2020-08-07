package GameController;

import org.joml.Vector2f;

import Entities.Entity;
import Wrappers.Arithmetic;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2f pos;
	public Vector2f viewport;
	
	private float moveSpeed;
	
	public Camera() {
		if (main == null) main = this;
		
		pos = new Vector2f(0f, 0f);
		moveSpeed = 1f;
		
		//TODO: Resolve this with input's stuff
		viewport = Drawer.GetWindowSize();
		viewport.x /= 2;
		viewport.y /= 2;
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
	
	/**
	 * Returns clipped vertex values
	 * TODO: Do this with matrices instead
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector2f mapVert(float x, float y) {
		return mapVert(new Vector2f(x, y));
	}
	
	/**
	 * Todo: use view matrix instead
	 * @param p
	 * @return
	 */
	
	public Vector2f mapVert(Vector2f p) {
		//View step of rendering
		Vector2f out = new Vector2f(p).sub(Camera.main.pos);
		//Vector2f out = new Vector2f(p);
		
		
		//Clip step of rendering (simple, since we're in an orthographic mode.
		out.x /= Camera.main.viewport.x;
		out.y /= Camera.main.viewport.y;
		
		return out;
		//return new Vector2f(p);
	}
}
