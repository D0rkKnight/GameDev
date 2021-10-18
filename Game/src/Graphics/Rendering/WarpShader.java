package Graphics.Rendering;

import GameController.Camera;

public class WarpShader extends TimedShader {

	public WarpShader(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("viewport");
	}

	@Override
	public void renderStart(Renderer rend) {
		super.renderStart(rend);

		if (Camera.main != null) {
			setUniform("viewport", Camera.main.viewport);
		} else {
			System.err.println("No camera to render to");
		}
	}
}
