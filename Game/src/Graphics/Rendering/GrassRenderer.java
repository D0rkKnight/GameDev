package Graphics.Rendering;

public class GrassRenderer extends TimedRenderer {

	public GrassRenderer(Shader shader) {
		super(shader);
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		// TODO: Enqueue motion factor/position
		shader.setUniform("WPos", transform.pos);
	}

}
