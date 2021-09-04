package Graphics.Rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
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
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Graphics.Elements.Mesh;
import Utility.Transformations.ProjectedTransform;

public abstract class Renderer {
	protected Shader shader;

	protected int vaoId;
	protected int vboId;
	protected Attribute[] attribs;

	public ProjectedTransform transform;
	protected Mesh mesh;
	protected boolean hasBufferUpdate;

	public int matrixMode;
	protected int vertexCount;

	public boolean hasInit;

	protected static final int INDEX_VERTEX = 0;

	Renderer(Shader shader) {
		this.shader = shader;
	}

	public void render() {
		renderStart();
		draw();
	}

	protected void renderStart() {
		if (!hasInit) {
			new Exception("Renderer not initialized!").printStackTrace();
			System.exit(1);
		}

		// This should be buffered once per frame, right?
		if (hasBufferUpdate) {
			FloatBuffer fBuff = mesh.toBuffer();

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, fBuff, GL_STREAM_DRAW);

			hasBufferUpdate = false;
		}

		shader.bind();
	}

	protected void draw() {
		// Draw stuff
		enableVAOs();
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		disableVAOs();
	}

	protected void init(ProjectedTransform transform) {
		this.transform = transform;
		hasInit = true;
	}

	protected void initData(FloatBuffer vBuff, ArrayList<Attribute> attribsBuff) {
		// Load attribute buffer to attribute list
		attribs = new Attribute[attribsBuff.size()];
		for (int i = 0; i < attribsBuff.size(); i++)
			attribs[i] = attribsBuff.get(i);

		// Creating vertex array
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// VERTEX STUFF
		// New vertex buffer (also bind it to the VAO)
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vBuff, GL_STREAM_DRAW);

		for (Attribute a : attribs) {
			// Format data in buffer (you'd need stride if the data represented multiple
			// things)
			int dataType = GL_FLOAT;
			int dataLength = Float.BYTES;
			glVertexAttribPointer(a.id, a.groupSize, dataType, false, a.stride * dataLength, a.offset * dataLength);
		}

		// Empty cache
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

	protected void createAttribs(ArrayList<Attribute> attribsBuff) {
		Attribute.addAttribute(attribsBuff, new Attribute(INDEX_VERTEX, 3)); // Vertices

		return;
	}

	protected void buildMesh(ArrayList<Attribute> attribsBuff, Vector2f[] vertices) {
		vertexCount = vertices.length;
		mesh = new Mesh(vertexCount * Attribute.getRowsize(attribsBuff));

		mesh.write(genVerts(vertices), attribsBuff.get(INDEX_VERTEX));

		hasBufferUpdate = true;
	}

	public void rebuildMesh(Vector2f[] verts) {
		// Build arraylist because... TODO: Fix this
		ArrayList<Attribute> attribsArr = new ArrayList<>();
		for (Attribute a : attribs)
			attribsArr.add(a);

		buildMesh(attribsArr, verts);
	}

	// Hmm this needs an MVP uniform, well if there isn't one then don't set the
	// transform matrix
	public void setTransformMatrix() {
		// Setting model space transformations
		Matrix4f mvp = transform.genMVP();

		// Set matrix uniform
		shader.bind();
		shader.setUniform("MVP", mvp);
	}

	protected float[] genVerts(Vector2f[] vertices) {
		float[] out = new float[vertices.length * 3];
		for (int i = 0; i < vertices.length; i++) {
			Vector2f v = vertices[i];
			out[i * 3] = v.x;
			out[i * 3 + 1] = v.y;
			out[i * 3 + 2] = 0;
		}
		return out;
	}

	// Note: this is an expensive operation
	public void updateVertices(Vector2f[] verts) {
		bufferData(genVerts(verts), INDEX_VERTEX);
	}

	// Encapsulated to make sure that hasBufferUpdate is set to true
	protected void bufferData(float[] data, int attribId) {
		// Buffer sub data
		mesh.write(data, attribs[attribId]);

		hasBufferUpdate = true; // this should be set to true, otherwise the update won't be seen.
	}

	public static class Attribute {
		public int id;
		public int groupSize;
		public int stride;
		public int offset;

		/**
		 * Holder for some mesh parsing data. Makes stuff more readable.
		 * 
		 * @param id
		 * @param groupSize
		 * @param stride
		 * @param anchorOffset
		 */
		Attribute(int id, int groupSize) {
			this.id = id;
			this.groupSize = groupSize;
			this.offset = 0;
		}

		static void addAttribute(ArrayList<Attribute> arr, Attribute a) {
			// Insert item and calculate offset
			int stride = 0;

			if (arr.size() > 0) {
				Attribute lastAttrib = arr.get(arr.size() - 1);
				a.offset = lastAttrib.offset + lastAttrib.groupSize;

				stride = getRowsize(arr);
			}

			arr.add(a);
			stride += a.groupSize;

			// Recalculate strides
			for (Attribute attrib : arr)
				attrib.stride = stride;
		}

		static int getRowsize(ArrayList<Attribute> attribBuff) {
			return attribBuff.get(0).stride;
		}
	}
}
