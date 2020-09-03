package Graphics.Rendering;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.HammerShapes.HammerShape;
import GameController.GameManager;
import Graphics.Elements.Texture;
import Utility.Transformation;
import Wrappers.Color;

public class GeneralRenderer extends Renderer implements Cloneable {
	
	public Texture spr;
	
	public GeneralRenderer(Shader shader) {
		super(shader);
		spr = null;
		
		hasBufferUpdate = false;
	}
	
	protected void renderStart() {
		super.renderStart();
		
	    setTransformMatrix();
		
	    // Enable blending
	    glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	    
		spr.bind();
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
		super.init(transform);
		
		ArrayList<Attribute> attribsBuff = new ArrayList<>();
		createAttribs(attribsBuff);
		
		//Write to mesh
		writeToMesh(attribsBuff, vertices, uvs, col);
		
		initData(mesh.toBuffer(), attribsBuff);
	}
	
	protected void createAttribs(ArrayList<Attribute> attribsBuff) {
		super.createAttribs(attribsBuff);

		Attribute.addAttribute(attribsBuff, new Attribute(1, 2)); //Tex UVs
		Attribute.addAttribute(attribsBuff, new Attribute(2, 4)); //Colors
	}
	
	protected void writeToMesh(ArrayList<Attribute> attribsBuff, Vector2f[] vertices, Vector2f[] uvs, Color col) {
		super.writeToMesh(attribsBuff, vertices);
		
		mesh.write(genUVs(uvs), attribsBuff.get(1));
		mesh.write(genColors(col), attribsBuff.get(2));
	}
	
	@Override
	public GeneralRenderer clone() throws CloneNotSupportedException {
		return (GeneralRenderer) super.clone();
	}
	
	public void updateColors(Color color) {
		bufferSubData(genColors(color), 2);
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
}