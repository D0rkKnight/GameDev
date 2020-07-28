package Rendering;

import static org.lwjgl.opengl.GL11.glVertex2f;

import Collision.HammerShape;
import GameController.Camera;
import Wrappers.Rect;
import Wrappers.Vector2;

public abstract class RectRenderer extends Renderer implements Cloneable {
	
	public Rect rect;
	protected Vector2 pos;
	
	protected Mesh mesh;
	
	protected int vaoId;
	protected int vboId;
	protected int vertexCount;
	
	protected boolean hasInit;
	protected int shape;
	
	public RectRenderer(Shader shader) {
		super(shader);
		
		this.rect = null;
		this.pos = null;
		
		hasInit = false;
	}

	@Override
	public void render() {
		if (!hasInit) {
			System.err.println("Renderer not initialized!");
			System.exit(1);
		}
		
		// TODO Auto-generated method stub
		shader.bind();
		System.out.println("Wrong renderer.");
	}
	
	/**
	 * Link the position of this renderer to another position.
	 * @param pos
	 */
	public void linkPos(Vector2 pos) {
		this.pos = pos;
	}
	
	/**
	 * Returns an array of vertices.
	 * @return
	 */
	protected float[] genVerts() {
		//Now this also needs to be normalized...
		Vector2 ul = mapVert(pos.x, pos.y + rect.h);
		Vector2 ur = mapVert(pos.x + rect.w, pos.y + rect.h);
		Vector2 bl = mapVert(pos.x, pos.y);
		Vector2 br = mapVert(pos.x + rect.w, pos.y);
		
		float[] verts = null;
		
		switch (shape) {
		case HammerShape.HAMMER_SHAPE_SQUARE:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				ul.x, ul.y, 0,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BL:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				br.x, br.y, 0,
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_BR:
			verts = new float[] {
				br.x, br.y, 0,
				ur.x, ur.y, 0,
				bl.x, bl.y, 0
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UL:
			verts = new float[] {
				ul.x, ul.y, 0,
				bl.x, bl.y, 0,
				ur.x, ur.y, 0
			};
			break;
		case HammerShape.HAMMER_SHAPE_TRIANGLE_UR:
			verts = new float[] {
				ur.x, ur.y, 0,
				ul.x, ul.y, 0,
				br.x, br.y, 0
			};
			break;
		default:
			System.err.println("Shape not recognized.");
		};
		
		return verts;
	}
	
	/**
	 * Returns clipped vertex values
	 * TODO: Do this with matrices instead
	 * @param x
	 * @param y
	 * @return
	 */
	protected Vector2 mapVert(float x, float y) {
		Vector2 p = new Vector2(x, y);
		
		//View step of rendering
		p.subtract(Camera.main.pos);
		
		
		//Clip step of rendering (simple, since we're in an orthographic mode.
		p.x /= Camera.main.viewport.w;
		p.y /= Camera.main.viewport.h;
		
		return p;
	}
	
	protected void setVert(Vector2 p) {
		glVertex2f(p.x, p.y);
	}
	
	@Override
	public RectRenderer clone() throws CloneNotSupportedException {
		return (RectRenderer) super.clone();
	}
}
