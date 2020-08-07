package Debug;

import static org.lwjgl.opengl.GL21.*;

import org.joml.Vector2f;

import GameController.Camera;
import Rendering.Shader;

public class DebugBox extends DebugElement {
	Vector2f p;
	Vector2f dims;
	
	public DebugBox(Vector2f p, Vector2f dims, int lifespan) {
		this(p, dims);
		
		this.lifespan = lifespan;
	}
	
	public DebugBox(Vector2f p, Vector2f dims) {
		this.p = p;
		this.dims = dims;
		
		this.lifespan = 1;
	}
	
	@Override
	public void render(Shader shader) {
		//Bind shader
		shader.bind();
		
		Camera cam = Camera.main;
		
		Vector2f[] points = new Vector2f[4];
		points[0] = new Vector2f(p);
		points[1] = new Vector2f(p).add(new Vector2f(0, dims.y));
		points[2] = new Vector2f(p).add(dims);
		points[3] = new Vector2f(p).add(new Vector2f(dims.x, 0));
		
		//Map
		for (int i=0; i<points.length; i++) {
			points[i] = cam.mapVert(points[i]);
		}
		
		//TODO: Update this deprecated code
		glBegin(GL_LINES);
		    for (int i=0; i<points.length; i++) {
		    	Vector2f p1 = points[i];
		    	Vector2f p2 = null;
		    	if (i == points.length - 1) p2 = points[0];
		    	else p2 = points[i+1];
		    	
		    	glVertex2f(p1.x, p1.y);
		    	glVertex2f(p2.x, p2.y);
		    }
		glEnd();
	}
}
