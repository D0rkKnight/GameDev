package Text;

import java.nio.FloatBuffer;

import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;

public class TextChar {

	public SubTexture subTex;
	public STBTTBakedChar charData;
	public char c;

	// Used for setting dimensions for quads
	public float w;
	public float h;
	public float xOff;
	public float yOff;
	public float advance;

	public TextChar(char c, Font font, Texture tex, STBTTBakedChar.Buffer charBuffer) {
		this.c = c;
		int charIndex = c - font.firstChar;

		this.charData = charBuffer.get(charIndex);

		// Load some data
		w = charData.x1() - charData.x0();
		h = charData.y1() - charData.y0();
		xOff = charData.xoff();
		yOff = -h - charData.yoff();
		advance = charData.xadvance();

		System.out.println("xOff: " + charData.xoff());
		System.out.println("yOff: " + charData.yoff());

		// Pull UVs
		float u1, u2, v1, v2;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer x = stack.mallocFloat(1);
			FloatBuffer y = stack.mallocFloat(1);

			STBTTAlignedQuad quad = STBTTAlignedQuad.malloc();
			STBTruetype.stbtt_GetBakedQuad(charBuffer, tex.width, tex.height, charIndex, x, y, quad, true);

			u1 = quad.s0();
			u2 = quad.s1();
			v1 = quad.t0();
			v2 = quad.t1();

		}

		// Data format translates to subtexture nicely
		this.subTex = new SubTexture(tex, u1, v1, u2 - u1, v2 - v1);
	}
}
