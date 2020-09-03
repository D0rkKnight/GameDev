package Graphics.Rendering;

import java.util.Arrays;

import Graphics.Elements.Texture;

public class SpriteSheet {
	public int w;
	public int h;
	
	public Texture[] texs;
	
	public SpriteSheet(Texture[] texs, int w, int h) {
		this.texs = texs;
		this.w = w;
		this.h = h;
	}
	
	public Texture[] getRow(int row) {
		return getRow(row, w);
	}
	
	public Texture[] getRow(int row, int len) {
		int start = row*w;
		int end = start+len;
		Texture[] out = Arrays.copyOfRange(texs, start, end);
		
		return out;
	}
}
