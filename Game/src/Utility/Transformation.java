package Utility;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class Transformation {

	public Vector2f pos; // A weird implementation

	public MatrixMode matrixMode;

	public static enum MatrixMode {
		//@formatter:off
		WORLD, 
		SCREEN, 
		STATIC // View matrix doesn't move
		//@formatter:on
	}

	// Do this to avoid reallocation every frame
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
		this(pos, MatrixMode.WORLD);
	}

	public Transformation(Vector2f pos, MatrixMode matrixMode) {
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

	public Transformation(Transformation transform) {
		this.pos = new Vector2f(transform.pos);
		this.matrixMode = transform.matrixMode;

		this.trans = new Matrix4f(transform.trans);
		this.rot = new Matrix4f(transform.rot);
		this.scale = new Matrix4f(transform.scale);
		this.model = new Matrix4f(transform.model);

		this.view = new Matrix4f(transform.view);
		this.proj = new Matrix4f(transform.proj);
		this.mvp = new Matrix4f(transform.rot);
	}

	/**
	 * Note that UI elements are anchored to the top left.
	 * 
	 * @return
	 */
	public Matrix4f genMVP() {
		Camera cam = Camera.main;

		switch (matrixMode) {
		case WORLD:
			view = cam.worldViewMatrix;
			break;
		case SCREEN:
			view = cam.screenViewMatrix;
			break;
		case STATIC:
			// Don't set a view matrix
			break;
		default:
			new Exception("Matrix mode not recognized!").printStackTrace();
		}

		proj = cam.projectionMatrix;

		// Getting the MVP
		genModel();
		mvp.identity().mul(proj).mul(view).mul(model);

		return mvp;
	}

	public Matrix4f genModel() {
		if (matrixMode == MatrixMode.SCREEN)
			trans.setTranslation(pos.x, -pos.y, 0);
		else
			trans.setTranslation(pos.x, pos.y, 0);

		model.identity().mul(trans).mul(rot).mul(scale);
		return model;
	}
}
