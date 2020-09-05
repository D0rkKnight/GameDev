package Graphics.Rendering;

import java.util.Arrays;

import Graphics.Elements.Texture;

public class SpriteSheet {
	public int w;
	public int h;
	public int tw;
	public int th;

	public Texture[] texs;

	public SpriteSheet(Texture[] texs, int w, int h, int tw, int th) {
		this.texs = texs;
		this.w = w;
		this.h = h;

		this.tw = tw;
		this.th = th;
	}

	public Texture[] getRow(int row) {
		return getRow(row, w);
	}

	public Texture[] getRow(int row, int len) {
		int start = row * w;
		int end = start + len;
		Texture[] out = Arrays.copyOfRange(texs, start, end);

		return out;
	}
}
