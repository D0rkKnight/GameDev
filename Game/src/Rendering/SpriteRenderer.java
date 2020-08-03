package Rendering;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferSubData;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import Collision.HammerShape;
import GameController.Camera;
import Wrappers.Color;
import Wrappers.Rect;
import Wrappers.Texture;
import Wrappers.Vector2;

public class SpriteRenderer extends Renderer implements Cloneable {
	
	public Texture spr;
	
	public Rect rect; //Bounding box? idk
	protected Vector2 pos;
	
	protected Mesh mesh;
	
	protected int texVboId;
	protected int vertexCount;
	
	protected boolean hasInit;
	protected int shape;
	
	public Color col;
	
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
		mesh.write(genVerts(), attribs[0]);
		mesh.write(genColors(col), attribs[2]);
		
		FloatBuffer fBuff = BufferUtils.createFloatBuffer(mesh.data.length);
		fBuff.put(mesh.data);
		fBuff.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
	    glBufferSubData(GL_ARRAY_BUFFER, 0, fBuff);
	    
	    
	    
		
		shader.bind();
		spr.bind();
		
		//Draw stuff
		enableVAOs();
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		disableVAOs();
	}
	
	/**
	 * Initialize
	 */
	public void init(Vector2 pos, Rect rect, int shape, Color col) {
		this.rect = rect;
		this.pos = pos;
		this.shape = shape;
		this.col = col;
		hasInit = true;
		
		//Generate attributes
		attribs = new Attribute[3];
		rowSize = 8;
		attribs[0] = new Attribute(0, 3, rowSize, 0); //vertices
		attribs[1] = new Attribute(1, 2, rowSize, 3); //Tex UVs
		attribs[2] = new Attribute(2, 3, rowSize, 5); //Colors
		
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
		mesh = new Mesh(vertexCount * rowSize);
		mesh.write(genVerts(), attribs[0]);
		mesh.write(genUV(), attribs[1]);
		mesh.write(genColors(col), attribs[2]);
		
		initData(mesh.toBuffer(), attribs);
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
		
		float[] verts = null;
		
		switch (shape) {
		case HammerShape.HAMMER_SHAPE_SQUARE:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				ul.x, ul.y, 0,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BL:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BR:
			verts = new float[] {
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				bl.x, bl.y, 0
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UL:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				ur.x, ur.y, 0
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UR:
			verts = new float[] {
				ur.x, ur.y, 0,
				ul.x, ul.y, 0,
				br.x, br.y, 0
			};
			break;
		default:
			System.err.println("Shape not recognized.");
		};
		
		return verts;
	}
	
	protected float[] genColors(Color col) {
		float[] out = new float[vertexCount * 3];
		for (int i=0; i<vertexCount; i++) {
			out[i*3] = col.r;
			out[i*3+1] = col.g;
			out[i*3+2] = col.b;
		}
		
		return out;
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
		p.subtractFromThis(Camera.main.pos);
		
		
		//Clip step of rendering (simple, since we're in an orthographic mode.
		p.x /= Camera.main.viewport.w;
		p.y /= Camera.main.viewport.h;
		
		return p;
	}
	
	protected void setVert(Vector2 p) {
		glVertex2f(p.x, p.y);
	}
}
