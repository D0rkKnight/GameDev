package Rendering;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import Wrappers.Rect;
import Wrappers.Texture;
import Wrappers.Vector2;

public class SpriteRenderer extends RectRenderer implements Cloneable {
	
	public Texture spr;
	
	public SpriteRenderer(Shader shader) {
		super(shader);
		spr = null;
	}
	
	@Override
	public void render() {
		if (!hasInit) {
			System.err.println("Renderer not initialized!");
			System.exit(1);
		}
		
		//Update verts
		//??? TODO: make it so that when you move, the renders upgrade.
		genVerts();
		float[] vertices = {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				ul.x, ul.y, 0
		};
		
		FloatBuffer fBuff = BufferUtils.createFloatBuffer(vertices.length);
		fBuff.put(vertices);
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
	    glBufferSubData(GL_ARRAY_BUFFER, 0, fBuff);
	    
	    
	    
		
		shader.bind();
		spr.bind();
		
		//TODO: Make it so that stuff can move.
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);
		
		// Draw the vertices
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		
		//Reset to normal
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
	}
	
	@Override
	public SpriteRenderer clone() throws CloneNotSupportedException {
		return (SpriteRenderer) super.clone();
	}
}
