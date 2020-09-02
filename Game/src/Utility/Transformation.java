package Utility;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class Transformation {
	
	public Vector2f pos; // A weird implementation
	
	public int matrixMode;
	
	public static final int MATRIX_MODE_WORLD = 0;
	public static final int MATRIX_MODE_SCREEN = 1;
	public static final int MATRIX_MODE_STATIC = 2; //The view matrix does not move
	
	//Do this to avoid reallocation every frame
	public Matrix4f trans;
	public Matrix4f rot;
	
	public Matrix4f scale;
	private Matrix4f model;
	
	public Matrix4f view;
	public Matrix4f proj;
	private Matrix4f mvp;
	
	public Transformation() {
		this(new Vector2f());
	}
	
	public Transformation(Vector2f pos) {
		this(pos, MATRIX_MODE_WORLD);
	}
	
	public Transformation(Vector2f pos, int matrixMode) {
		this.pos = pos;
		this.matrixMode = matrixMode;
		
		trans = new Matrix4f();
		rot = new Matrix4f();
		scale = new Matrix4f();
		model = new Matrix4f();
		
		view = new Matrix4f();
		proj = new Matrix4f();
		mvp = new Matrix4f();
	}
	
	/**
	 * Note that UI elements are anchored to the top left.
	 * @return
	 */
	public Matrix4f genMVP() {
		Camera cam = Camera.main;
		
		switch(matrixMode) {
		case MATRIX_MODE_WORLD:
			view = cam.worldViewMatrix;
			break;
		case MATRIX_MODE_SCREEN:
			view = cam.screenViewMatrix;
			break;
		case MATRIX_MODE_STATIC:
			//Don't set a view matrix
			break;
		default:
			new Exception("Matrix mode not recognized!").printStackTrace();
		}
		
		proj = cam.projectionMatrix;
		
		//Getting the MVP
		genModel();
		mvp.identity().mul(proj).mul(view).mul(model);
		
		return mvp;
	}
	
	public Matrix4f genModel() {
		if (matrixMode == MATRIX_MODE_SCREEN) trans.setTranslation(pos.x, -pos.y, 0);
		else trans.setTranslation(pos.x, pos.y, 0);
		
		model.identity().mul(trans).mul(rot).mul(scale);
		return model;
	}
}
