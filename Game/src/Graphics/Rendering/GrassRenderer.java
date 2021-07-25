package Graphics.Rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GrassRenderer extends TimedRenderer {

	public GrassRenderer(Shader shader) {
		super(shader);
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		// TODO: Enqueue motion factor/position
		// Retrieve transform position
		Vector3f pos = new Vector3f();
		transform.trans.getTranslation(pos);
		shader.setUniform("WPos", new Vector2f(pos.x, pos.y));
	}

}
