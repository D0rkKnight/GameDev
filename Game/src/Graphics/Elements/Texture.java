package Graphics.Elements;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import Graphics.Rendering.SpriteSheet;

public class Texture {
	private int id;
	public int width;
	public int height;

	public Texture(int id) {
		this.id = id;
		System.err.println("Width and height not set!");
	}

	public Texture(int id, int w, int h) {
		this.id = id;
		this.width = w;
		this.height = h;
	}

	public Texture(ByteBuffer pixels, int w, int h) {
		this.width = w;
		this.height = h;
		genThisTex(pixels);

		integrityCheck();
	}

	public Texture(String url) {
		BufferedImage bi;
		try {
			bi = ImageIO.read(new File(url));

			width = bi.getWidth();
			height = bi.getHeight();
			ByteBuffer pixels = imageToBuffer(bi, width, height);

			genThisTex(pixels);

		} catch (IOException e) {
			e.printStackTrace();
		}

		integrityCheck();
	}

	private static ByteBuffer imageToBuffer(BufferedImage bi, int w, int h) {
		int[] pixels_raw = new int[w * h * 4];
		pixels_raw = bi.getRGB(0, 0, w, h, null, 0, w);

		ByteBuffer pixels = BufferUtils.createByteBuffer(w * h * 4);

		for (int i = 0; i < w * h; i++) {
			int pixel = pixels_raw[i];
			pixels.put((byte) ((pixel >> 16) & 0xFF)); // RED
			pixels.put((byte) ((pixel >> 8) & 0xFF)); // GREEN
			pixels.put((byte) (pixel & 0xFF)); // BLUE
			pixels.put((byte) ((pixel >> 24) & 0xFF)); // ALPHA
		}

		pixels.flip(); // DO NOT FORGET THIS!!!

		return pixels;
	}

	/**
	 * Given a properly formatted bytebuffer of pixels, register it to opengl as a
	 * texture.
	 * 
	 * @param pixels
	 */
	private void genThisTex(ByteBuffer pixels) {
		id = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, id);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

		// Check for errors
		int err;
		if ((err = glGetError()) != GL_NO_ERROR) {
			new Exception("OpenGL Error").printStackTrace();
			System.err.println(err);
			System.exit(1);
		}
	}

	private void integrityCheck() {
		if (width == 0 || height == 0) {
			new Exception("Dimensions not set!").printStackTrace();
			System.exit(1);
		}
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	/**
	 * Take a spritesheet and turn it into a 1D array of textures.s
	 * 
	 * @param url
	 * @param w:  pixel width
	 * @param h:  pixel height
	 * @param tw: width of 1 tile
	 * @param th: height of 1 tile
	 * @return
	 */
	public static SpriteSheet unpackSpritesheet(String url, int tw, int th) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(url));
		} catch (IOException e) {
			System.err.println("URL " + url + " not recognized.");
			e.printStackTrace();
		}

		int w = bi.getWidth();
		int h = bi.getHeight();

		int tilesWide = w / tw; // Note: this presumes the spritesheet's dimensions divides evenly.
		int tilesTall = h / th;
		Texture[] out = new Texture[tilesWide * tilesTall];

		for (int i = 0; i < tilesTall; i++) {
			for (int j = 0; j < tilesWide; j++) {
				BufferedImage subImage = bi.getSubimage(j * tw, i * th, tw, th);
				ByteBuffer subBuff = imageToBuffer(subImage, tw, th);

				out[i * tilesWide + j] = new Texture(subBuff, tw, th);
			}
		}

		return new SpriteSheet(out, tilesWide, tilesTall);
	}

	public int getId() {
		return id;
	}
}
