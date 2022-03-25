package Graphics.Rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GrassShader extends TimedShader {

	public GrassShader(String filename) {
		super(filename);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("WPos");
	}

	@Override
	public void renderStart(Renderer rend) {
		super.renderStart(rend);

		// TODO: Enqueue motion factor/position
		// Retrieve transform position
		Vector3f pos = new Vector3f();
		rend.localTrans.trans.getTranslation(pos);
		rend.shader.setUniform("WPos", new Vector2f(pos.x, pos.y));
	}
}
