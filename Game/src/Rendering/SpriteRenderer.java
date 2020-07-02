package Rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import Wrappers.Rect;
import Wrappers.Texture;
import Wrappers.Vector2;

public class SpriteRenderer extends RectRenderer implements Cloneable {
	
	public Texture spr;
	protected int texVboId;
	
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
		//TODO: implement camera with view matrix, not vertex updates.
		
		mesh.write(genVerts(), 3, 5, 0);
		
		FloatBuffer fBuff = BufferUtils.createFloatBuffer(mesh.data.length);
		fBuff.put(mesh.data);
		fBuff.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
	    glBufferSubData(GL_ARRAY_BUFFER, 0, fBuff);
	    
	    
	    
		
		shader.bind();
		spr.bind();
		
		//TODO: Make it so that stuff can move.
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		// Draw the vertices
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		
		//Reset to normal
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}
	
	/**
	 * Initialize
	 */
	public void init(Vector2 pos, Rect rect) {
		this.rect = rect;
		this.pos = pos;
		hasInit = true;
		
		//Group testing
		mesh = new Mesh(30);
		
		//Write vertices
		mesh.write(genVerts(), 3, 5, 0);
		
		float[] t = {
				0, 0,
				0, 1,
				1, 1,
				1, 1,
				1, 0,
				0, 0
		};
		mesh.write(t, 2, 5, 3);
		
		//Renderer stuff
		
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(mesh.data.length);
		verticesBuffer.put(mesh.data);
		verticesBuffer.flip();
		
		vertexCount = 6;

		//Creating vertex array
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		
		//VERTEX STUFF
		//New vertex buffer (also bind it to the VAO) TODO: Make it not static
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STREAM_DRAW);
		
		//Format data in buffer (you'd need stride if the data represented multiple things)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		
		
		//Empty cache
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	@Override
	public SpriteRenderer clone() throws CloneNotSupportedException {
		return (SpriteRenderer) super.clone();
	}
}
