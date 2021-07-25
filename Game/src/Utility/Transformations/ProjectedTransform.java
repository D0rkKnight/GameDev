package Utility.Transformations;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import GameController.Camera;

public class ProjectedTransform extends ModelTransform {

	public Matrix4f view;
	public Matrix4f proj;
	private Matrix4f mvp;

	public ProjectedTransform() {
		this(new Vector2f());
	}

	public ProjectedTransform(Vector2f pos) {
		this(pos, MatrixMode.WORLD);
	}

	public ProjectedTransform(Vector2f pos, MatrixMode matrixMode) {
		super(pos, matrixMode);

		view = new Matrix4f();
		proj = new Matrix4f();
		mvp = new Matrix4f();

		setTrans(pos);
	}

	public ProjectedTransform(ProjectedTransform transform) {
		super(transform);

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
}
