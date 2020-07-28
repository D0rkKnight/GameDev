package Rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import Collision.HammerShape;
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
		
		//Move mesh
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
	public void init(Vector2 pos, Rect rect, int shape) {
		this.rect = rect;
		this.pos = pos;
		this.shape = shape;
		hasInit = true;
		
		//Vertex count
		switch(shape) {
		case HammerShape.HAMMER_SHAPE_SQUARE:
			vertexCount = 6;
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BL:
			vertexCount = 3;
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BR:
			vertexCount = 3;
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UL:
			vertexCount = 3;
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UR:
			vertexCount = 3;
			break;
		default:
			System.err.println("Shape not recognized.");
		}
		
		//Write to mesh
		mesh = new Mesh(vertexCount * 5);
		mesh.write(genVerts(), 3, 5, 0);
		mesh.write(genUV(), 2, 5, 3);
		
		//Renderer stuff
		
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(mesh.data.length);
		verticesBuffer.put(mesh.data);
		verticesBuffer.flip();

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
	
	protected float[] genUV() {
		//
		Vector2 ul = new Vector2(0, 0);
		Vector2 ur = new Vector2(1, 0);
		Vector2 bl = new Vector2(0, 1);
		Vector2 br = new Vector2(1, 1);
		
		float[] uv = null;
		
		switch (shape) {
		case HammerShape.HAMMER_SHAPE_SQUARE:
			uv = new float[] {
				ul.x, ul.y,
				bl.x, bl.y,
				br.x, br.y,
				br.x, br.y,
				ur.x, ur.y,
				ul.x, ul.y,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BL:
			uv = new float[] {
				ul.x, ul.y,
				bl.x, bl.y,
				br.x, br.y,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BR:
			uv = new float[] {
				br.x, br.y,
				ur.x, ur.y,
				bl.x, bl.y,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UL:
			uv = new float[] {
				ul.x, ul.y,
				bl.x, bl.y,
				ur.x, ur.y
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UR:
			uv = new float[] {
				ur.x, ur.y,
				ul.x, ul.y,
				br.x, br.y
			};
			break;
		default:
			System.err.println("Shape not recognized.");
		};
		
		return uv;
	}
}
