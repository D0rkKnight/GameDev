package GameController;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Entities.Framework.Entity;
import Graphics.Rendering.Drawer;
import Utility.Arithmetic;

public class Camera {
	public static Camera main;
	private Entity target;
	public Vector2f pos; //Camera is centered on position
	public Vector2f viewport;
	public Matrix4f worldViewMatrix;
	public Matrix4f screenViewMatrix;
	public Matrix4f projectionMatrix;
	
	private float moveSpeed;
	
	public Camera() {
		if (main == null) main = this;
		
		pos = new Vector2f(0f, 0f);
		moveSpeed = 0.3f;
		
		//TODO: Resolve this with input's stuff
		viewport = Drawer.GetWindowSize();
		
		worldViewMatrix = new Matrix4f();
		screenViewMatrix = new Matrix4f();
		projectionMatrix = new Matrix4f();
		
		updateMatrices();
		
	}
	
	public void attach(Entity target) {
		this.target = target;
	}
	
	public void update() {
		//Change the viewport
		Vector2f tPos = target.getPosition();
		pos.x = Arithmetic.lerp(pos.x, tPos.x, moveSpeed);
		pos.y = Arithmetic.lerp(pos.y, tPos.y, moveSpeed);
		
		//Limit the viewport by its bounds
		Map map = World.currmap;
		float w = map.w;
		float h = map.h;
		
		Vector2f halfPort = new Vector2f(viewport).div(2);
		Vector2f bl = new Vector2f(0, 0).add(halfPort);
		Vector2f ur = new Vector2f(w, h).sub(halfPort);
		
		float x = Arithmetic.limit(pos.x, bl.x, ur.x);
		float y = Arithmetic.limit(pos.y, bl.y, ur.y);
		
		pos.x = x;
		pos.y = y;
		
		//Update matrices
		updateMatrices();
		
		//Update, since the mouse's world space has changed
		Input.updateCursor(Input.mouseScreenPos.x, Input.mouseScreenPos.y);
	}
	
	public void updateMatrices() {
		//Update the viewport/projection matrices
		worldViewMatrix.setTranslation(-pos.x, -pos.y, 0);
		screenViewMatrix.setTranslation(-viewport.x/2, viewport.y/2, 0);
		projectionMatrix.identity().scale(2f/viewport.x, 2f/viewport.y, 1);
	}
}
