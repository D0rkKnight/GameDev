package Graphics.Elements;

import java.util.ArrayList;

import Graphics.Drawer.DBEnum;
import Graphics.Rendering.Renderer;

public class DrawOrderRenderers extends DrawOrderElement {
	private ArrayList<Renderer> renderers;
	private DBEnum targetBuff;

	public DrawOrderRenderers(int z, DBEnum targetBuff) {
		super(z);

		this.targetBuff = targetBuff;

		renderers = new ArrayList<>();
	}

	public void addRend(Renderer rend) {
		renderers.add(rend);
	}

	public void tryRender() {
		targetBuff.bind();

		for (Renderer rend : renderers)
			rend.render();
	}
}
