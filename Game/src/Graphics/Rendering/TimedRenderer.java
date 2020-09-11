package Graphics.Rendering;

import GameController.Time;
import Graphics.Elements.RendererCallback;

public class TimedRenderer extends GeneralRenderer {

	public RendererCallback timeFunction;
	private float t;

	public TimedRenderer(Shader shader) {
		super(shader);

		timeFunction = new RendererCallback() {

			@Override
			public void invoke(Renderer rend) {
				TimedRenderer tRend = (TimedRenderer) rend;

				tRend.t = Time.timeSinceStart();
			}
		};
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		if (timeFunction != null)
			timeFunction.invoke(this);
		shader.setUniform("Time", t);
	}
}
