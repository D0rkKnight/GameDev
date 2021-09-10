package Debugging;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Graphics.Rendering.Shader;
import Wrappers.Color;

public class DebugVector extends DebugElement {
	public Vector2f s;
	public Vector2f e;

	public DebugVector(Vector2f s, Vector2f e, Color col, int lifespan) {
		this.s = s;
		this.e = e;

		this.col = col;
		this.lifespan = lifespan;
	}

	public DebugVector(Vector2f p, Vector2f v, float mult, Color col, int lifespan) {
		Vector2f fullVec = new Vector2f(v).mul(mult);

		s = new Vector2f(p);
		e = new Vector2f(s).add(fullVec);

		this.lifespan = lifespan;
		this.col = col;
	}

	public DebugVector(Vector2f p, Vector2f v, float mult, int lifespan) {
		this(p, v, mult, new Color(1, 1, 0, 1), lifespan);
	}

	public DebugVector(Vector2f p, Vector2f v, float mult) {
		this(p, v, mult, 1);
	}

	@Override
	public void render(Shader shader) {
		shader.bind();
		Matrix4f mvp = Debug.trans.genMVP();

		shader.setUniform("MVP", mvp);
		shader.setUniform("Color", col);

		// Bind shader
		shader.bind();

		glBegin(GL_LINES);
		glVertex2f(s.x, s.y);
		glVertex2f(e.x, e.y);
		glEnd();
	}
}
