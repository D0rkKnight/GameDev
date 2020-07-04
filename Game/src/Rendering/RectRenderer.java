package Rendering;

import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.HashMap;

import GameController.Camera;
import Wrappers.Rect;
import Wrappers.Vector2;

public abstract class RectRenderer extends Renderer implements Cloneable {
	
	public Rect rect;
	protected Vector2 pos;
	
	protected Mesh mesh;
	
	protected int vaoId;
	protected int vboId;
	protected int vertexCount;
	
	protected boolean hasInit;
	
	public RectRenderer(Shader shader) {
		super(shader);
		
		this.rect = null;
		this.pos = null;
		
		hasInit = false;
	}
	
	/**
	 * Run init in order to prepare the renderer for use.
	 * @param pos
	 * @param rect
	 */
	public void init() {
		/*this.rect = rect;
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
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STREAM_DRAW);
		
		//Format data in buffer (you'd need stride if the data represented multiple things)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		//Empty cache
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);*/
		
		System.out.println("This needs to be reworked");
	}

	@Override
	public void render() {
		if (!hasInit) {
			System.err.println("Renderer not initialized!");
			System.exit(1);
		}
		
		// TODO Auto-generated method stub
		shader.bind();
		System.out.println("Wrong renderer.");
	}
	
	/**
	 * Link the position of this renderer to another position.
	 * @param pos
	 */
	public void linkPos(Vector2 pos) {
		this.pos = pos;
	}
	
	/**
	 * Returns an array of vertices.
	 * @return
	 */
	protected float[] genVerts() {
		//Now this also needs to be normalized...
		Vector2 ul = mapVert(pos.x, pos.y + rect.h);
		Vector2 ur = mapVert(pos.x + rect.w, pos.y + rect.h);
		Vector2 bl = mapVert(pos.x, pos.y);
		Vector2 br = mapVert(pos.x + rect.w, pos.y);
		
		float[] verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				ul.x, ul.y, 0,
		};
		
		return verts;
	}
	
	/**
	 * Returns clipped vertex values
	 * TODO: Do this with matrices instead
	 * @param x
	 * @param y
	 * @return
	 */
	protected Vector2 mapVert(float x, float y) {
		Vector2 p = new Vector2(x, y);
		
		//View step of rendering
		p.subtract(Camera.main.pos);
		
		
		//Clip step of rendering (simple, since we're in an orthographic mode.
		p.x /= Camera.main.viewport.w;
		p.y /= Camera.main.viewport.h;
		
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
