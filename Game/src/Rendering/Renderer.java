package Rendering;

public abstract class Renderer {
	protected Shader shader;
	
	Renderer(Shader shader) {
		this.shader = shader;
	}
	
	public abstract void render();
}
