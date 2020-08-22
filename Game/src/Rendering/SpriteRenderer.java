package Rendering;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferSubData;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import Collision.HammerShape;
import GameController.GameManager;
import Wrappers.Color;

public class SpriteRenderer extends Renderer implements Cloneable {
	
	public Texture spr;
	
	protected Mesh mesh;
	
	protected int texVboId;
	protected int vertexCount;
	
	protected boolean hasInit;
	
	public Color col;
	public int matrixMode;
	
	private boolean hasBufferUpdate;
	
	public SpriteRenderer(Shader shader) {
		super(shader);
		spr = null;
		
		hasBufferUpdate = false;
	}
	
	@Override
	public void render() {
		if (!hasInit) {
			new Exception("Renderer not initialized!").printStackTrace();
			System.exit(1);
		}
		
		//Put in new colors
		mesh.write(genColors(col), attribs[2]);
		
		//This should be buffered once per frame, right?
		if (hasBufferUpdate) {
			FloatBuffer fBuff = mesh.toBuffer();
			
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
		    glBufferSubData(GL_ARRAY_BUFFER, 0, fBuff);
		    
		    hasBufferUpdate = false;
		}
	    
	    setTransformMatrix();
		
	    // Enable blending
	    glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    
		shader.bind();
		spr.bind();
		
		//Draw stuff
		enableVAOs();
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		disableVAOs();
	}
	
	public void setTransformMatrix() {
		//Setting model space transformations
		Matrix4f mvp = transform.genMVP();
		
		//Set matrix uniform
		shader.bind();
		shader.setUniform("MVP", mvp);
	}
	
	public void init(Transformation transform, Vector2f dims, int shape, Color col) {
		HammerShape hs = GameManager.hammerLookup.get(shape);
		
		Vector2f[] vertices = hs.getRenderVertices(dims);
		Vector2f[] uvs = hs.getRenderUVs();
		
		init(transform, vertices, uvs, col);
	}
	
	/**
	 * Initialize
	 */
	public void init(Transformation transform, Vector2f[] vertices, Vector2f[] uvs, Color col) {
		
		//Link objects (these objects cannot be destroyed)
		this.transform = transform;
		this.col = col;
		hasInit = true;
		
		//Generate attributes
		attribs = new Attribute[3];
		Attribute.addAttribute(attribs, new Attribute(0, 3)); //Vertices
		Attribute.addAttribute(attribs, new Attribute(1, 2)); //Tex UVs
		Attribute.addAttribute(attribs, new Attribute(2, 4)); //Colors
		
		//Write to mesh
		vertexCount = vertices.length;
		mesh = new Mesh(vertexCount * Attribute.getRowsize(attribs));
		mesh.write(genVerts(vertices), attribs[0]);
		mesh.write(genUVs(uvs), attribs[1]);
		mesh.write(genColors(col), attribs[2]);
		
		initData(mesh.toBuffer(), attribs);
	}
	
	@Override
	public SpriteRenderer clone() throws CloneNotSupportedException {
		return (SpriteRenderer) super.clone();
	}
	
	//Note: this is an expensive operation
	public void updateVertices(Vector2f[] verts) {
		bufferSubData(genVerts(verts), 0);
	}
	
	//Encapsulated to make sure that hasBufferUpdate is set to true
	private void bufferSubData(float[] data, int attribId) {
		//Buffer sub data
		mesh.write(data, attribs[attribId]);
		
		hasBufferUpdate = true; //this should be set to true, otherwise the update won't be seen.
	}
	
	protected float[] genVerts(Vector2f[] vertices) {
		float[] out = new float[vertices.length * 3];
		for (int i=0; i<vertices.length; i++) {
			Vector2f v = vertices[i];
			out[i*3] = v.x;
			out[i*3 + 1] = v.y;
			out[i*3 + 2] = 0;
		}
		return out;
	}
	
	protected float[] genUVs(Vector2f[] uvs) {
		float[] out = new float[uvs.length * 2];
		for (int i=0; i<uvs.length; i++) {
			Vector2f v = uvs[i];
			out[i*2] = v.x;
			out[i*2 + 1] = v.y;
		}
		return out;
	}
	
	protected float[] genColors(Color col) {
		int stride = 4;
		float[] out = new float[vertexCount * stride];
		for (int i=0; i<vertexCount; i++) {
			out[i*stride] = col.r;
			out[i*stride+1] = col.g;
			out[i*stride+2] = col.b;
			
			if (stride > 3) out[i*stride+3] = col.a;
		}
		
		return out;
	}
	
	protected void setVert(Vector2f p) {
		glVertex2f(p.x, p.y);
	}
}
