package Rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class Transformation {
	
	private Vector2f pos;
	
	public Transformation(Vector2f pos) {
		this.pos = pos;
	}
	
	public void setPos(Vector2f pos) {
		this.pos = pos;
	}
	
	public Matrix4f genMVP() {
		Camera cam = Camera.main;
		
		//Setting model space transformations
		Matrix4f translation = new Matrix4f();
		translation.setTranslation(pos.x, pos.y, 0);
		Matrix4f rotation = new Matrix4f();
		Matrix4f scale = new Matrix4f();
		
		Matrix4f modelMatrix = new Matrix4f(scale).mul(rotation).mul(translation);
		
		//Setting the view matrix
		Vector2f camPos = cam.pos;
		Matrix4f viewMatrix = new Matrix4f().setTranslation(-camPos.x, -camPos.y, 0);
		
		//Setting the projection matrix (screw it we're just calculating this one ourselves)
		//Maps camera space coordinates to clip space

		Vector2f viewport = cam.viewport;
		Matrix4f projectionMatrix = new Matrix4f().scale(1f/viewport.x, 1f/viewport.y, 1);
		
		//Getting MVP
		Matrix4f mvp = new Matrix4f(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
		
		return mvp;
	}
}
