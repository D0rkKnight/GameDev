package Rendering;

import static org.lwjgl.opengl.GL30.*;

import Wrappers.Position;
import Wrappers.Rect;

public class RectRenderer extends Renderer {
	
	public Rect rect;
	public Position pos;
	
	public RectRenderer(Shader shader) {
		super(shader);
		
		//Just set these before calling render
		this.rect = null;
		this.pos = null;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		shader.bind();
		
		glBegin(GL_QUADS);
			glVertex2f(pos.x, pos.y);
			glVertex2f(pos.x + rect.w, pos.y);
			glVertex2f(pos.x + rect.w, pos.y + rect.h);
			glVertex2f(pos.x, pos.y + rect.h);
		glEnd();
	}

}
