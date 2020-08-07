package Debug;

import static org.lwjgl.opengl.GL21.*;

import org.joml.Vector2f;

import GameController.Camera;
import Rendering.Shader;

public class DebugVector extends DebugElement {
	Vector2f p;
	Vector2f v;
	float mult;
	
	public DebugVector(Vector2f p, Vector2f v, float mult, int lifespan) {
		this(p, v, mult);
		
		this.lifespan = lifespan;
	}
	
	public DebugVector(Vector2f p, Vector2f v, float mult) {
		this.p = p;
		this.v = v;
		this.mult = mult;
		
		this.lifespan = 1;
	}

	@Override
	public void render(Shader shader) {
		Camera cam = Camera.main;
		Vector2f fullVec = new Vector2f(v).mul(mult);
		
		Vector2f start = cam.mapVert(p);
		Vector2f end = cam.mapVert(new Vector2f(p).add(fullVec));
		
		//Bind shader
		shader.bind();
		
		//TODO: Update this deprecated code
		glBegin(GL_LINES);
		    glVertex2f(start.x, start.y);
		    glVertex2f(end.x, end.y);
		glEnd();
	}
}
