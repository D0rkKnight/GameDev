package Graphics.Rendering;

import org.joml.Vector2f;
import org.joml.Vector4f;

import Utility.Transformations.ModelTransform.MatrixMode;
import Utility.Transformations.ProjectedTransform;

public class BleedShader extends TimedShader {

	public Vector2f center;
	public ProjectedTransform trans;

	public BleedShader(String filename) {
		super(filename);
		center = new Vector2f();
		trans = new ProjectedTransform(new Vector2f(0, 0), MatrixMode.WORLD);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("center");
	}

	@Override
	public void renderStart(Renderer rend) {
		super.renderStart(rend);

		Vector4f o = new Vector4f(0, 0, 0, 1).mul(trans.genMVP());
		center.set(o.x, o.y);

		setUniform("center", center);
	}
}
