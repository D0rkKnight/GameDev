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
	
	protected int vaoId;
	protected int vboId;
	protected Attribute[] attribs;
	
	public Transformation transform;
	
	Renderer(Shader shader) {
		this.shader = shader;
	}
	
	public abstract void render();
	
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
		Attribute(int id, int groupSize) {
			this.id = id;
			this.groupSize = groupSize;
		}
		
		static void addAttribute(Attribute[] arr, Attribute a) {
			//Insert item and calculate offset
			int stride = 0;
			boolean attribPlaced = false;
			for (int i=0; i<arr.length; i++) {
				if (arr[i] == null && !attribPlaced) {
					arr[i] = a;
					
					int off = 0;
					if (i>0) off = arr[i-1].offset + arr[i-1].groupSize; //Attributes are tightly packed
					a.offset = off;
					
					attribPlaced = true;
				}
				
				//Note: this includes arr[i] if the previous if statement triggers.
				if (arr[i] != null) {
					stride += arr[i].groupSize;
				}
			}
			
			//Recalculate strides
			for (int i=0; i<arr.length; i++) {
				if (arr[i] != null) arr[i].stride = stride;
			}
		}
		
		static int getRowsize(Attribute[] arr) {return arr[0].stride;}
	}
}
