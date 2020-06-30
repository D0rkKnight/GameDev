package Rendering;

import static org.lwjgl.opengl.GL30.*;

import GameController.Camera;
import Wrappers.Vector2;
import Wrappers.Rect;

public class RectRenderer extends Renderer {
	
	public Rect rect;
	public Vector2 pos;
	
	protected Vector2 ul;
	protected Vector2 ur;
	protected Vector2 bl;
	protected Vector2 br;
	
	public RectRenderer(Shader shader) {
		super(shader);
		
		//Just set these before calling render
		this.rect = null;
		this.pos = null;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		shader.bind();
		
		genVerts();
		glBegin(GL_QUADS);
			setVert(bl);
			setVert(br);
			setVert(ur);
			setVert(ul);
		glEnd();
	}
	
	protected void genVerts() {
		//Now this also needs to be normalized...
		
		ul = mapVert(pos.x, pos.y + rect.h);
		
		ur = mapVert(pos.x + rect.w, pos.y + rect.h);
		
		bl = mapVert(pos.x, pos.y);
		
		br = mapVert(pos.x + rect.w, pos.y);
	}
	
	protected Vector2 mapVert(float x, float y) {
		Vector2 p = new Vector2(x, y);
		
		p.subtract(Camera.main.pos);
		p.x += Camera.main.viewport.w/2;
		p.y += Camera.main.viewport.h/2;
		
		//float ar = Camera.main.viewport.h / Camera.main.viewport.w;
		//p.x *= ar;
		//p.y /= Camera.main.viewport.h;
		
		return p;
	}
	
	protected void setVert(Vector2 p) {
		glVertex2f(p.x, p.y);
	}
}
