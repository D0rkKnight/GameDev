package Graphics.Rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GrassShader extends TimedShader {

	protected GrassShader(String filename) {
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
		rend.transform.trans.getTranslation(pos);
		rend.shader.setUniform("WPos", new Vector2f(pos.x, pos.y));
	}

	public static GrassShader genShader(String filename) {
		return (GrassShader) cacheShader(filename, (fname) -> {
			return new GrassShader(fname);
		});
	}
}
