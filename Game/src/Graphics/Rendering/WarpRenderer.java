package Graphics.Rendering;

import GameController.Camera;

public class WarpRenderer extends GeneralRenderer {

	public WarpRenderer(Shader shader) {
		super(shader);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		if (Camera.main != null) {
			shader.setUniform("viewport", Camera.main.viewport);
		} else {
			System.err.println("No camera to render to");
		}
	}
}
