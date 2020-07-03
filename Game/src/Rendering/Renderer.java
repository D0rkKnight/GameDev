package Rendering;

import java.util.HashMap;

import Wrappers.Vector2;

public abstract class Renderer implements Cloneable{
	protected Shader shader;
	public static final int RENDERER_POS_ID = 0;
	public static final int RENDERER_RECT_ID = 1;
	
	Renderer(Shader shader) {
		this.shader = shader;
	}
	
	public abstract void render();
	public abstract void linkPos(Vector2 pos);
	
	@Override
	public Renderer clone() throws CloneNotSupportedException {
		return (Renderer) super.clone();
	}
}
