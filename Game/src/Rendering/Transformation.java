package Rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class Transformation {
	
	public Vector2f pos;
	
	public int matrixMode;
	
	public static final int MATRIX_MODE_WORLD = 0;
	public static final int MATRIX_MODE_SCREEN = 1;
	
	//Do this to avoid reallocation every frame
	private Matrix4f trans;
	private Matrix4f rot;
	private Matrix4f scale;
	private Matrix4f model;
	
	private Matrix4f view;
	private Matrix4f proj;
	private Matrix4f mvp;
	
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
	
	public Matrix4f genMVP() {
		switch(matrixMode) {
		case MATRIX_MODE_WORLD:
			genWorldMVP();
			break;
		case MATRIX_MODE_SCREEN:
			genScreenMVP();
			break;
		default:
			new Exception("Matrix mode not recognized!").printStackTrace();
		}
		
		//Getting MVP
		mvp.identity().mul(proj).mul(view).mul(model);
		
		return mvp;
	}
	
	/**
	 * For objects that exist in the world
	 * @return
	 */
	public void genWorldMVP() {
		Camera cam = Camera.main;
		
		//Setting model space transformations
		trans.setTranslation(pos.x, pos.y, 0);
		rot.identity();
		scale.identity();
		
		model.identity().mul(trans).mul(rot).mul(scale);
		
		//Setting the view/projection matrices
		view = cam.worldViewMatrix;
		proj = cam.projectionMatrix;
	}
	
	/**
	 * For UI elements (These are anchored upper right btw)
	 */
	public void genScreenMVP() {
		Camera cam = Camera.main;
		
		//Setting model space transformations
		trans.setTranslation(pos.x, -pos.y, 0);
		rot.identity();
		scale.identity();
		
		model.identity().mul(trans).mul(rot).mul(scale);
		
		//Use screen view matrix
		view = cam.screenViewMatrix;
		proj = cam.projectionMatrix;
	}
}
