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
		ul = new Vector2(pos.x, pos.y + rect.h);
		ul.subtract(Camera.main.pos);
		
		ur = new Vector2(pos.x + rect.w, pos.y + rect.h);
		ur.subtract(Camera.main.pos);
		
		bl = new Vector2(pos.x, pos.y);
		bl.subtract(Camera.main.pos);
		
		br = new Vector2(pos.x + rect.w, pos.y);
		br.subtract(Camera.main.pos);
	}
	
	protected void setVert(Vector2 p) {
		glVertex2f(p.x, p.y);
	}
}
