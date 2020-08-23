package Rendering;

import GameController.GameManager;

public class WavyRenderer extends SpriteRenderer{

	public WavyRenderer(Shader shader) {
		super(shader);
		// TODO Auto-generated constructor stub
	}
	
	protected void renderStart() {
		super.renderStart();
		
		float t = GameManager.timeSinceStart();
		
		shader.setUniform("Time", t);
	}
}
