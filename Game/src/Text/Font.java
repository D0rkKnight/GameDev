package Text;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_R8;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import Graphics.Elements.Texture;

public class Font {

	private ByteBuffer dataBuff;

	public char firstChar;
	public char lastChar;

	TextChar[] characters;
	public Texture tex;

	public Font(String url) {
		// Read font file
		File f = new File("assets/Fonts/" + url);
		dataBuff = null;

		try {
			byte[] data = new byte[(int) f.length()];

			FileInputStream fStream = new FileInputStream(f);
			fStream.read(data);
			fStream.close();

			dataBuff = BufferUtils.createByteBuffer(data.length);
			dataBuff.put(data);
			dataBuff.flip(); // Remember, everything has to be flipped going into JNI, just because.
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Prepare data containers for font information
		firstChar = ' ';
		lastChar = '~';
		int glyphCount = lastChar - firstChar + 1;
		int pw = 512;
		int ph = 512;
		ByteBuffer pixels = BufferUtils.createByteBuffer(pw * ph);
		STBTTBakedChar.Buffer charData = STBTTBakedChar.calloc(glyphCount);

		// Bake the font
		STBTruetype.stbtt_BakeFontBitmap(dataBuff, 32f, pixels, pw, ph, firstChar, charData);

		// This seems to work?
//		STBTTFontinfo fontInfo = STBTTFontinfo.calloc();
//		STBTruetype.stbtt_InitFont(fontInfo, dataBuff);
//
//		ByteBuffer pixels = null;
//		int w, h;
//		try (MemoryStack stack = MemoryStack.stackPush()) {
//			IntBuffer width = stack.mallocInt(1);
//			IntBuffer height = stack.mallocInt(1);
//
//			pixels = STBTruetype.stbtt_GetCodepointBitmap(fontInfo, 0, 1, 'a', width, height, null, null);
//			System.out.println(pixels);
//			w = width.get(0);
//			h = height.get(0);
//		}

		// Texture tex = new Texture(bitmap, w, h);
		// TODO: Write with overrides instead
		// TODO: Workable TrueType example (USE THIS INSTEAD OF THE CURRENT SOLUTION)
		// https://github.com/nothings/stb/blob/master/tests/test_truetype.c

		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, pw, ph, 0, GL_RED, GL_UNSIGNED_BYTE, pixels);
		glBindTexture(GL_TEXTURE_2D, 0);

		Texture texObj = new Texture(tex, pw, ph);
		this.tex = texObj;

		// Generate characters
		characters = new TextChar[glyphCount];
		for (char c = firstChar; c <= lastChar; c++) {
			characters[c - firstChar] = new TextChar(c, this, texObj, charData);
		}
	}

}
