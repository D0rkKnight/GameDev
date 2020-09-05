package Graphics.Rendering;

import GameController.Time;

public class WavyRenderer extends GeneralRenderer {

	public WavyRenderer(Shader shader) {
		super(shader);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		float t = Time.timeSinceStart();

		shader.setUniform("Time", t);
	}
}
