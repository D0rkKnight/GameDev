package Graphics.Rendering;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Utility.Transformation;
import Wrappers.Color;

public class GeneralRenderer extends Renderer {

	public Texture spr;

	protected static final int INDEX_UV = 1;
	protected static final int INDEX_COLOR = 2;

	public GeneralRenderer(Shader shader) {
		super(shader);
		spr = null;

		hasBufferUpdate = false;
		spr = Debug.debugTex;
	}

	@Override
	protected void renderStart() {
		super.renderStart();

		setTransformMatrix();

		// Enable blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		spr.bind();
	}

	// TODO: Testing, in development
	public void init(Transformation transform, Vector2f dims, Shape.ShapeEnum shape, Color col, SubTexture subTex) {
		Shape s = shape.v;

		Vector2f[] vertices = s.getRenderVertices(dims);
		Vector2f[] uvs = subTex.genSubUV(s);

		init(transform, vertices, uvs, col);
	}

	public void init(Transformation transform, Vector2f dims, Shape.ShapeEnum shape, Color col) {
		Shape hs = shape.v;

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

		// Write to mesh
		buildMesh(attribsBuff, vertices, uvs, col);

		initData(mesh.toBuffer(), attribsBuff);
	}

	@Override
	protected void createAttribs(ArrayList<Attribute> attribsBuff) {
		super.createAttribs(attribsBuff);

		Attribute.addAttribute(attribsBuff, new Attribute(INDEX_UV, 2)); // Tex UVs
		Attribute.addAttribute(attribsBuff, new Attribute(INDEX_COLOR, 4)); // Colors
	}

	protected void buildMesh(ArrayList<Attribute> attribsBuff, Vector2f[] vertices, Vector2f[] uvs, Color col) {
		super.buildMesh(attribsBuff, vertices);

		mesh.write(genUVs(uvs), attribsBuff.get(INDEX_UV));
		mesh.write(genColors(col), attribsBuff.get(INDEX_COLOR));
	}

	public void rebuildMesh(Vector2f[] verts, Vector2f[] uvs, Color col) {
		// Build arraylist because... TODO: Fix this
		ArrayList<Attribute> attribsArr = new ArrayList<>();
		for (Attribute a : attribs)
			attribsArr.add(a);

		buildMesh(attribsArr, verts, uvs, col);
	}

	public void updateColors(Color color) {
		bufferData(genColors(color), INDEX_COLOR);
	}

	public void updateUVs(Vector2f[] uv) {
		bufferData(genUVs(uv), INDEX_UV);
	}

	protected float[] genUVs(Vector2f[] uvs) {
		float[] out = new float[uvs.length * 2];
		for (int i = 0; i < uvs.length; i++) {
			Vector2f v = uvs[i];
			out[i * 2] = v.x;
			out[i * 2 + 1] = v.y;
		}
		return out;
	}

	protected float[] genColors(Color col) {
		int stride = 4;
		float[] out = new float[vertexCount * stride];
		for (int i = 0; i < vertexCount; i++) {
			out[i * stride] = col.r;
			out[i * stride + 1] = col.g;
			out[i * stride + 2] = col.b;

			if (stride > 3)
				out[i * stride + 3] = col.a;
		}

		return out;
	}
}
