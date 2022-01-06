package Graphics.Rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
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
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import Collision.Collider;
import Entities.Framework.Anchored;
import Entities.Framework.Entity;
import Graphics.Elements.Mesh;
import UI.UIElement;
import Utility.Transformations.ModelTransform;
import Utility.Transformations.ProjectedTransform;

public abstract class Renderer implements Anchored {
	public Shader shader;

	protected int vaoId;
	protected int vboId;
	protected Attribute[] attribs;

	public ModelTransform localTrans = new ModelTransform();
	public ProjectedTransform worldToScreen = new ProjectedTransform();

	protected Mesh mesh;
	protected boolean hasBufferUpdate;

	public int matrixMode;
	protected int vertexCount;

	public boolean hasInit;

	protected static final int INDEX_VERTEX = 0;
	public Object parent;

	Vector2f origin = new Vector2f();

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

		shader.renderStart(this);

		setTransformMatrix();
	}

	protected void draw() {
		// Draw stuff
		enableVAOs();
		if (useIndexing)
			glDrawElements(GL_TRIANGLES, indexCount, GL30.GL_UNSIGNED_INT, 0l);
		else
			glDrawArrays(GL_TRIANGLES, 0, vertexCount);
		disableVAOs();
	}

	protected void init(ProjectedTransform transform) {
		// TODO: Fix this hack. For now break the transform apart. In the future, create
		// two matrix types to handle this...
		this.localTrans.setModel(transform);

		this.worldToScreen.proj.set(transform.proj);
		this.worldToScreen.view.set(transform.view);
		this.worldToScreen.matrixMode = transform.matrixMode;

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

		if (useIndexing)
			glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, indexId);
	}

	protected void disableVAOs() {
		for (Attribute a : attribs) {
			glDisableVertexAttribArray(a.id);
		}
		glBindVertexArray(0);

		if (useIndexing)
			glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
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
		Matrix4f localToWorld = genL2WMat();
		Matrix4f mvp = worldToScreen.genMVP().mul(localToWorld).mul(localTrans.genModel());

		shader.setUniform("MVP", mvp);
	}

	/**
	 * Generates a mat that converts from local space coords to world space coords.
	 * Includes translation from variable anchors.
	 * 
	 * @return
	 */

	public Matrix4f genL2WMat() {
		if (parent != null && parent instanceof Entity) {
			Entity ent = (Entity) parent;

			Matrix4f originOffset = new Matrix4f();
			originOffset.translate(-origin.x, -origin.y, 0);

			Matrix4f o = ent.genChildL2WMat().mul(originOffset);
			return o;
		}

		if (parent != null && parent instanceof UIElement) {
			UIElement uiE = (UIElement) parent;

			Matrix4f originOffset = new Matrix4f();
			originOffset.translate(-origin.x, -origin.y, 0);

			Matrix4f o = uiE.genChildL2WMat().mul(originOffset);

			return o;
		}

		if (parent != null && parent instanceof Collider) {
			Collider coll = (Collider) parent;

			Matrix4f originOffset = new Matrix4f();
			originOffset.translate(-origin.x, -origin.y, 0);

			Matrix4f o = coll.genChildL2WMat().mul(originOffset);

			return o;
		}

		return new Matrix4f();
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

	protected int indexId = -1; // Indexing is not directly associated with VAO, instead used in draw call
	protected int indexCount = -1;
	protected boolean useIndexing = false;

	public void setIndexBuffer(int[] vf) {
		useIndexing = true;
		indexCount = vf.length;

		// Init indexing
		if (indexId == -1) {
			indexId = glGenBuffers();
		}

		glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, indexId);

		IntBuffer buff = BufferUtils.createIntBuffer(vf.length);
		buff.put(vf);
		buff.flip();

		// Static because indexing doesn't change but is used a lot
		glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buff, GL30.GL_STATIC_DRAW);

		// Return to default
		glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
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

	public Vector2f getOrigin() {
		return origin;
	}

	public void setOrigin(Vector2f o) {
		this.origin = o;
	}
}
