package Utility.Transformations;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ModelTransform {
	public MatrixMode matrixMode;

	public static enum MatrixMode {
		//@formatter:off
		WORLD, 
		SCREEN, 
		STATIC // View matrix doesn't move
		//@formatter:on
	}

	// Do this to avoid reallocation every frame
	public Matrix4f trans; // Exists on a local level
	public Matrix4f rot;
	public Matrix4f scale;
	protected Matrix4f model;

	public ModelTransform() {
		this(new Vector2f());
	}

	public ModelTransform(Vector2f pos) {
		this(pos, MatrixMode.WORLD);
	}

	public ModelTransform(Vector2f pos, MatrixMode matrixMode) {
		trans = new Matrix4f();
		rot = new Matrix4f();
		scale = new Matrix4f();
		model = new Matrix4f();

		this.matrixMode = matrixMode;
		setTrans(pos);
	}

	public ModelTransform(ModelTransform transform) {
		this.matrixMode = transform.matrixMode;

		this.trans = new Matrix4f(transform.trans);
		this.rot = new Matrix4f(transform.rot);
		this.scale = new Matrix4f(transform.scale);
		this.model = new Matrix4f(transform.model);
	}

	public void setModel(ModelTransform transform) {
		trans.set(transform.trans);
		rot.set(transform.rot);
		scale.set(transform.scale);
	}

	public void setTrans(Vector2f pos) {

		Vector3f result = new Vector3f(pos.x, pos.y, 0);

		trans.identity().setTranslation(result);
	}

	public Matrix4f genModel() {
		model.identity().mul(trans).mul(rot).mul(scale);
		return model;
	}
}
