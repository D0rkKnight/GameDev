package Debugging;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Graphics.Rendering.Shader;
import Wrappers.Color;

public class DebugBox extends DebugElement {
	Vector2f p;
	Vector2f dims;

	public DebugBox(Vector2f p, Vector2f dims, Color col, int lifespan) {
		this.p = p;
		this.dims = dims;

		this.lifespan = lifespan;
		this.col = col;
	}

	public DebugBox(Vector2f p, Vector2f dims, int lifespan) {
		this(p, dims, new Color(0, 1, 1, 1), lifespan);
	}

	public DebugBox(Vector2f p, Vector2f dims) {
		this(p, dims, 0);
	}

	@Override
	public void render(Shader shader) {
		Matrix4f mvp = Debug.trans.genMVP();
		shader.setUniform("MVP", mvp);
		shader.setUniform("Color", col);

		// Bind shader
		shader.bind();

		Vector2f[] points = new Vector2f[4];
		points[0] = new Vector2f(p);
		points[1] = new Vector2f(p).add(new Vector2f(0, dims.y));
		points[2] = new Vector2f(p).add(dims);
		points[3] = new Vector2f(p).add(new Vector2f(dims.x, 0));

		// This implementation is honestly faster than just constantly generating VAOs.
		glBegin(GL_LINES);
		for (int i = 0; i < points.length; i++) {
			Vector2f p1 = points[i];
			Vector2f p2 = null;
			if (i == points.length - 1)
				p2 = points[0];
			else
				p2 = points[i + 1];

			glVertex2f(p1.x, p1.y);
			glVertex2f(p2.x, p2.y);
		}
		glEnd();
	}
}
