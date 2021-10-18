package Wrappers;

import static org.lwjgl.opengl.GL11.glClearColor;

public class Color {

	public final static Color DARK_GRAY = new Color(0.3f, 0.3f, 0.3f);
	public final static Color WHITE = new Color(1, 1, 1);

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

	public Color(Color col) {
		r = col.r;
		g = col.g;
		b = col.b;
		a = col.a;
	}

	public static enum hexFormat {
		ARGB
	}

	public Color(String hex, hexFormat format) {
		switch (format) {
		case ARGB:
			// Shear off hashtag, then split
			String sheared = hex.replace("#", "");
			float[] data = new float[4];
			for (int i = 0; i < data.length; i++) {
				data[i] = Integer.parseInt(sheared.substring(i * 2, i * 2 + 2), 16) / 255f;
			}

			a = data[0];
			r = data[1];
			g = data[2];
			b = data[3];

			break;
		default:
			System.err.println("Color format not recognized");
			System.exit(1);
		}
	}

	public static void setGLClear(Color col) {
		glClearColor(col.r, col.g, col.b, col.a);
	}
}
