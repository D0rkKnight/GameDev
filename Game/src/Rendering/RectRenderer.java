package Rendering;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import GameController.Camera;
import Wrappers.Rect;
import Wrappers.Vector2;

public class RectRenderer extends Renderer implements Cloneable {
	
	public Rect rect;
	public Vector2 pos;
	
	protected Vector2 ul;
	protected Vector2 ur;
	protected Vector2 bl;
	protected Vector2 br;
	
	protected int vaoId;
	protected int vertexVboId;
	protected int vertexCount;
	
	protected boolean hasInit;
	
	public RectRenderer(Shader shader) {
		super(shader);
		
		this.rect = null;
		this.pos = null;
		
		hasInit = false;
	}
	
	public void init(Vector2 pos, Rect rect) {
		this.rect = rect;
		this.pos = pos;
		hasInit = true;
		
		//Renderer stuff
		genVerts();
		
		float[] vertices = {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				ul.x, ul.y, 0
		};
		
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();
		
		vertexCount = 6;

		//Creating vertex array
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		
		//New vertex buffer (also bind it to the VAO) TODO: Make it not static
		vertexVboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_DYNAMIC_DRAW);
		
		//Format data in buffer (you'd need stride if the data represented multiple things)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		//Empty cache
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	@Override
	public void render() {
		if (!hasInit) {
			System.err.println("Renderer not initialized!");
			System.exit(1);
		}
		
		// TODO Auto-generated method stub
		shader.bind();
		
		genVerts();
//		glBegin(GL_QUADS);
//			setVert(bl);
//			setVert(br);
//			setVert(ur);
//			setVert(ul);
//		glEnd();
	}
	
	protected void genVerts() {
		//Now this also needs to be normalized...
		
		ul = mapVert(pos.x, pos.y + rect.h);
		
		ur = mapVert(pos.x + rect.w, pos.y + rect.h);
		
		bl = mapVert(pos.x, pos.y);
		
		br = mapVert(pos.x + rect.w, pos.y);
	}
	
	protected Vector2 mapVert(float x, float y) {
		Vector2 p = new Vector2(x, y);
		
		p.subtract(Camera.main.pos);
		
		p.x /= Camera.main.viewport.w;
		p.y /= Camera.main.viewport.h;
		
		//float ar = Camera.main.viewport.h / Camera.main.viewport.w;
		//p.x *= ar;
		//p.y /= Camera.main.viewport.h;
		
		return p;
	}
	
	protected void setVert(Vector2 p) {
		glVertex2f(p.x, p.y);
	}
	
	@Override
	public RectRenderer clone() throws CloneNotSupportedException {
		return (RectRenderer) super.clone();
	}
}
