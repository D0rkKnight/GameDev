package Wrappers;

import static org.lwjgl.opengl.GL11.glClearColor;

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

	public static void setGLClear(Color col) {
		glClearColor(col.r, col.g, col.b, col.a);
	}
}
