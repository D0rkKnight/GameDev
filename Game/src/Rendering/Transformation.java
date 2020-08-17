package Rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class Transformation {
	
	public Vector2f pos;
	
	public int matrixMode;
	
	public static final int MATRIX_MODE_WORLD = 0;
	public static final int MATRIX_MODE_SCREEN = 1;
	
	public Transformation(Vector2f pos) {
		this(pos, MATRIX_MODE_WORLD);
	}
	
	public Transformation(Vector2f pos, int matrixMode) {
		this.pos = pos;
		this.matrixMode = matrixMode;
	}
	
	public Matrix4f genMVP() {
		switch(matrixMode) {
		case MATRIX_MODE_WORLD:
			return genWorldMVP();
		case MATRIX_MODE_SCREEN:
			return genScreenMVP();
		default:
			new Exception("Matrix mode not recognized!").printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * For objects that exist in the world
	 * @return
	 */
	public Matrix4f genWorldMVP() {
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
		Matrix4f projectionMatrix = new Matrix4f().scale(2f/viewport.x, 2f/viewport.y, 1);
		
		//Getting MVP
		Matrix4f mvp = new Matrix4f(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
		
		return mvp;
	}
	
	/**
	 * For UI elements (These are anchored upper right btw)
	 */
	public Matrix4f genScreenMVP() {
		Camera cam = Camera.main;
		
		//Setting model space transformations
		Matrix4f translation = new Matrix4f();
		translation.setTranslation(pos.x, -pos.y, 0);
		Matrix4f rotation = new Matrix4f();
		Matrix4f scale = new Matrix4f();
		
		Matrix4f modelMatrix = new Matrix4f(scale).mul(rotation).mul(translation);
		
		//No view matrix, since the screen is anchored.
		Vector2f viewport = cam.viewport;
		Matrix4f viewMatrix = new Matrix4f().translate(-viewport.x/2, viewport.y/2, 0);
		
		//Setting the projection matrix (screw it we're just calculating this one ourselves)
		//Maps camera space coordinates to clip space

		
		Matrix4f projectionMatrix = new Matrix4f().scale(2f/viewport.x, 2f/viewport.y, 1);
		
		//Getting MVP
		Matrix4f mvp = new Matrix4f(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
		
		return mvp;
	}
}
