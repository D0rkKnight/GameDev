package Rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;

import org.joml.Vector2f;

public abstract class Renderer implements Cloneable{
	protected Shader shader;
	public static final int RENDERER_POS_ID = 0;
	public static final int RENDERER_RECT_ID = 1;
	
	protected int vaoId;
	protected int vboId;
	protected int rowSize;
	protected Attribute[] attribs;
	
	Renderer(Shader shader) {
		this.shader = shader;
	}
	
	public abstract void render();
	public abstract void linkPos(Vector2f pos);
	
	@Override
	public Renderer clone() throws CloneNotSupportedException {
		return (Renderer) super.clone();
	}
	
	protected void initData(FloatBuffer vBuff, Attribute[] attribs) {
		//Creating vertex array
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		
		//VERTEX STUFF
		//New vertex buffer (also bind it to the VAO) TODO: Make it not static
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vBuff, GL_STREAM_DRAW);
		
		for (Attribute a : attribs) {
			//Format data in buffer (you'd need stride if the data represented multiple things)
			int dataType = GL_FLOAT;
			int dataLength = Float.BYTES;
			glVertexAttribPointer(a.id, a.groupSize, dataType, false, a.stride * dataLength, a.offset * dataLength);
		}
		
		//Empty cache
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	protected void enableVAOs() {
		glBindVertexArray(vaoId);
		for (Attribute a : attribs) {
			glEnableVertexAttribArray(a.id);
		}
	}
	
	protected void disableVAOs() {
		for (Attribute a : attribs) {
			glDisableVertexAttribArray(a.id);
		}
		glBindVertexArray(0);
	}

	
	static class Attribute {
		public int id;
		public int groupSize;
		public int stride;
		public int offset;
		
		/** Holder for some mesh parsing data. Makes stuff more readable.
		 * 
		 * @param id
		 * @param groupSize
		 * @param stride
		 * @param offset
		 */
		Attribute(int id, int groupSize, int stride, int offset) {
			this.id = id;
			this.groupSize = groupSize;
			this.stride = stride;
			this.offset = offset;
		}
	}
}
