package Graphics.Rendering;

import GameController.Time;
import Utility.Callback;

public class TimedShader extends SpriteShader {

	public Callback timeFunction;
	private float t;

	public TimedShader(String filename) {
		super(filename);

		timeFunction = () -> {
			t = Time.timeSinceStart();
		};
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("Time");
	}

	@Override
	public void renderStart(Renderer rend) {
		super.renderStart(rend);

		if (timeFunction != null)
			timeFunction.invoke();
		setUniform("Time", t);
	}
}
