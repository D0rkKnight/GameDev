package Debugging;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Graphics.Rendering.Shader;
import Wrappers.Color;

public class DebugPolygon extends DebugElement {

	public Vector2f[] verts;

	public DebugPolygon(Vector2f[] verts, int lifespan, Color col) {
		this.verts = verts;
		this.lifespan = lifespan;
		this.col = col;
	}

	@Override
	public void render(Shader shader) {
		Matrix4f mvp = Debug.trans.genMVP();
		shader.setUniform("MVP", mvp);
		shader.setUniform("Color", col);

		// Bind shader
		shader.bind();

		glBegin(GL_LINES);
		for (int i = 0; i < verts.length; i++) {
			Vector2f p1 = verts[i];
			Vector2f p2 = null;
			if (i == verts.length - 1)
				p2 = verts[0];
			else
				p2 = verts[i + 1];

			glVertex2f(p1.x, p1.y);
			glVertex2f(p2.x, p2.y);
		}
		glEnd();
	}

}
