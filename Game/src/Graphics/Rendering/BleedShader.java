package Graphics.Rendering;

import java.util.Arrays;

import org.joml.Vector2f;
import org.joml.Vector4f;

import GameController.Camera;
import Utility.Transformations.ModelTransform.MatrixMode;
import Utility.Transformations.ProjectedTransform;

public class BleedShader extends SpriteShader {

	public Vector2f center;
	public ProjectedTransform trans;
	public float[] pulses;

	public BleedShader(String filename) {
		super(filename);
		center = new Vector2f();
		trans = new ProjectedTransform(new Vector2f(0, 0), MatrixMode.WORLD);

		pulses = new float[64];
		Arrays.fill(pulses, -1000);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("center");
		createUniform("pulseRadii");
		createUniform("viewport");
	}

	@Override
	public void renderStart(Renderer rend) {
		super.renderStart(rend);

		Vector4f o = new Vector4f(0, 0, 0, 1).mul(trans.genMVP());
		center.set(o.x, o.y);

		setUniform("center", center);
		setUniform("pulseRadii", pulses);
		setUniform("viewport", Camera.main.viewport);
	}
}
