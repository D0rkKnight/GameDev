package Rendering;

import Wrappers.Texture;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
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
		
		genVerts();
		glBegin(GL_QUADS);
			glTexCoord2f(0, 0);
			setVert(bl);
			
			glTexCoord2f(1, 0);
			setVert(br);
			
			glTexCoord2f(1, 1);
			setVert(ur);
			
			glTexCoord2f(0, 1);
			setVert(ul);
		glEnd();
	}
}
