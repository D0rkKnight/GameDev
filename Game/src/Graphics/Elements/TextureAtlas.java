package Graphics.Elements;

import java.util.ArrayList;

public class TextureAtlas {
	public Texture tex;

	public int tw;
	public int th;

	public TextureAtlas(Texture tex, int tw, int th) {
		this.tex = tex;

		this.tw = tw;
		this.th = th;
	}

	public SubTexture genSubTex(int x, int y) {
		return genSubTex(x, y, 1, 1);
	}

	public SubTexture genSubTex(int x, int y, int w, int h) {
		float tilesWide = tex.width / (float) tw;
		float tilesTall = tex.height / (float) th;

		float subW = tw / (float) tex.width * w;
		float subH = th / (float) tex.height * h;

		return new SubTexture(tex, x / tilesWide, y / tilesTall, subW, subH);
	}

	public SubTexture[] genSubTexSet(int x1, int y1, int x2, int y2) {
		return genSubTexSet(x1, y1, x2, y2, 1, 1);
	}

	/**
	 * Generates an array of subtextures. Bounds are inclusive.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param w: tiles wide
	 * @param h: tiles tall
	 * @return
	 */
	public SubTexture[] genSubTexSet(int x1, int y1, int x2, int y2, int w, int h) {
		ArrayList<SubTexture> arr = new ArrayList<>();
		for (int i = y1; i <= y2; i += h) {
			for (int j = x1; j <= x2; j += w) {
				arr.add(genSubTex(j, i, w, h));
			}
		}

		SubTexture[] out = new SubTexture[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			out[i] = arr.get(i);
		}
		return out;
	}
}
