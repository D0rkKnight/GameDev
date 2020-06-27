package Wrappers;

import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

public class Texture {
	private int id;
	private int width;
	private int height;
	
	public Texture(String url) {
		BufferedImage bi;
		try {
			bi = ImageIO.read(new File(url));
			width = bi.getWidth();
			height = bi.getHeight();
			
			int[] pixels_raw = new int[width*height*4];
			pixels_raw = bi.getRGB(0, 0, width, height, null, 0, width);
			
			ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
			
			for (int i=0; i<width*height; i++) {
				int pixel = pixels_raw[i];
				//System.out.println(pixel);
				pixels.put((byte)((pixel >> 16) & 0xFF)); //RED
				pixels.put((byte)((pixel >> 8) & 0xFF));  //GREEN
				pixels.put((byte)(pixel & 0xFF));         //BLUE
				pixels.put((byte)((pixel >> 24) & 0xFF)); //ALPHA
			}
			
			pixels.flip();
			
			/*for (int i=0; i<width*height; i++) {
				System.out.println(Integer.toBinaryString(pixels.getInt()));
			}*/
			
			id = glGenTextures();
			
			glBindTexture(GL_TEXTURE_2D, id);
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
			
			//Looks like we're good with errors
			int err;
			while((err = glGetError()) != GL_NO_ERROR)
			{
				System.err.println(err);
				System.exit(1);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}
}
