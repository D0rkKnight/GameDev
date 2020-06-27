package Rendering;

import Wrappers.Texture;
import static org.lwjgl.opengl.GL30.*;

public class SpriteRenderer extends RectRenderer{
	
	public Texture spr;
	
	public SpriteRenderer(Shader shader) {
		super(shader);
		spr = null;
	}
	
	@Override
	public void render() {
		//shader.bind();
		spr.bind();
		
		glBegin(GL_QUADS);
			glTexCoord2f(0, 0);
			glVertex2f(pos.x, pos.y + rect.h);
			
			glTexCoord2f(1, 0);
			glVertex2f(pos.x + rect.w, pos.y + rect.h);
		
			glTexCoord2f(1, 1);
			glVertex2f(pos.x + rect.w, pos.y);
			
			glTexCoord2f(0, 1);
			glVertex2f(pos.x, pos.y);
		glEnd();
	}
}
