package Wrappers;

public class Vector2 implements Cloneable {
	public float x;
	public float y;
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void subtract(Vector2 pos) {
		this.x -= pos.x;
		this.y -= pos.y;
	}
	
	public void add(Vector2 pos) {
		this.x += pos.x;
		this.y += pos.y;
	}
	
	public void add(Rect r) {
		this.x += r.w;
		this.y += r.h;
	}
	
	public Vector2 clone() throws CloneNotSupportedException {
		return (Vector2) super.clone();
	}
}
