package Debug;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import GameController.Camera;
import Rendering.Shader;
import Wrappers.Rect;
import Wrappers.Vector2;

public class DebugBox extends DebugElement {
	Vector2 p;
	Vector2 dims;
	
	public DebugBox(Vector2 p, Vector2 dims, int lifespan) {
		this(p, dims);
		
		this.lifespan = lifespan;
	}
	
	public DebugBox(Vector2 p, Vector2 dims) {
		this.p = p;
		this.dims = dims;
		
		this.lifespan = 1;
	}
	
	@Override
	public void render(Shader shader) {
		//Bind shader
		shader.bind();
		
		Camera cam = Camera.main;
		
		Vector2[] points = new Vector2[4];
		points[0] = p;
		points[1] = p.add(new Vector2(0, dims.y));
		points[2] = p.add(dims);
		points[3] = p.add(new Vector2(dims.x, 0));
		
		//Map
		for (int i=0; i<points.length; i++) {
			points[i] = cam.mapVert(points[i]);
		}
		
		//TODO: Update this deprecated code
		glBegin(GL_LINES);
		    for (int i=0; i<points.length; i++) {
		    	Vector2 p1 = points[i];
		    	Vector2 p2 = null;
		    	if (i == points.length - 1) p2 = points[0];
		    	else p2 = points[i+1];
		    	
		    	glVertex2f(p1.x, p1.y);
		    	glVertex2f(p2.x, p2.y);
		    }
		glEnd();
	}
}
