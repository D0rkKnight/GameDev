package Debug;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import GameController.Camera;
import Rendering.Shader;
import Wrappers.Vector2;

public class DebugVector extends DebugElement {
	Vector2 p;
	Vector2 v;
	float mult;
	
	DebugVector(Vector2 p, Vector2 v, float mult, int lifespan) {
		this(p, v, mult);
		
		this.lifespan = lifespan;
	}
	
	DebugVector(Vector2 p, Vector2 v, float mult) {
		this.p = p;
		this.v = v;
		this.mult = mult;
		
		this.lifespan = 1;
	}

	@Override
	public void render(Shader shader) {
		Camera cam = Camera.main;
		Vector2 fullVec = v.mult(mult);
		
		Vector2 start = cam.mapVert(p);
		Vector2 end = cam.mapVert(p.add(fullVec));
		
		System.out.println(end.toString());
		
		//Bind shader
		shader.bind();
		
		//TODO: Update this deprecated code
		glBegin(GL_LINES);
		    glVertex2f(start.x, start.y);
		    glVertex2f(end.x, end.y);
		glEnd();
	}
}
