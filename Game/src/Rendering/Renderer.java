package Rendering;

public abstract class Renderer implements Cloneable{
	protected Shader shader;
	
	Renderer(Shader shader) {
		this.shader = shader;
	}
	
	public abstract void render();
	
	@Override
	public Renderer clone() throws CloneNotSupportedException {
		return (Renderer) super.clone();
	}
}
