package Wrappers;

public class Color {
	public float r;
	public float g;
	public float b;
	public float a;
	
	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}
	
	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color() {
		r = 0;
		g = 0;
		b = 0;
		a = 0;
	}
}
