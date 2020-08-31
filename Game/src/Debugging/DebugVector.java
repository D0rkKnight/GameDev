package Debugging;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import GameController.Camera;
import Rendering.Shader;
import Wrappers.Color;

public class DebugVector extends DebugElement {
	Vector2f p;
	Vector2f v;
	float mult;
	
	public DebugVector(Vector2f p, Vector2f v, float mult, Color col, int lifespan) {
		this.p = p;
		this.v = v;
		this.mult = mult;
		
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
		Matrix4f mvp = Debug.trans.genMVP();
		shader.setUniform("MVP", mvp);
		shader.setUniform("Color", col);
		
		Vector2f fullVec = new Vector2f(v).mul(mult);
		
		Vector2f start = new Vector2f(p);
		Vector2f end = new Vector2f(start).add(fullVec);
		
		//Bind shader
		shader.bind();
		
		//TODO: Update this deprecated code
		glBegin(GL_LINES);
		    glVertex2f(start.x, start.y);
		    glVertex2f(end.x, end.y);
		glEnd();
	}
}
