package Graphics.Elements;

import java.util.ArrayList;

import Graphics.Rendering.Renderer;

public class DrawOrderRenderers extends DrawOrderElement {
	private ArrayList<Renderer> renderers;

	public DrawOrderRenderers(int z) {
		super(z);

		renderers = new ArrayList<>();
	}

	public void addRend(Renderer rend) {
		renderers.add(rend);
	}

	public void tryRender() {
		for (Renderer rend : renderers)
			rend.render();
	}
}
