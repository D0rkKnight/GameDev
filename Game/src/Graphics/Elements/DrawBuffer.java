package Graphics.Elements;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import Graphics.Rendering.GeneralRenderer;

public class DrawBuffer {
	public int fbuff;
	public Texture tex;
	public boolean isActive = false;

	public GeneralRenderer rend;

	public DrawBuffer(int fbuff, Texture tex, GeneralRenderer rend) {
		this.fbuff = fbuff;
		this.tex = tex;

		this.rend = rend;
	}

	public static DrawBuffer genEmptyBuffer(int pixelsW, int pixelsT, GeneralRenderer newRend) {
		// Gen the buffer
		int buff = glGenFramebuffers();

		glBindFramebuffer(GL_FRAMEBUFFER, buff);

		int texId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, texId);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, pixelsW, pixelsT, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		Texture texObj = new Texture(texId, pixelsW, pixelsT);

		// Poor filtering. Needed !
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		// Bind texture to active frame buffer (not the same thing as "drawBuff", it is
		// GL_COLOR_ATTACHMENT0 in this case. It's just a static buffer.)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);

		// Configure that color0 is to be drawn to by shaders.
		glDrawBuffers(GL_COLOR_ATTACHMENT0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			System.err.println("Error!!!");
		}

		// Clear frame buffer, sets background color
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		return new DrawBuffer(buff, texObj, newRend);
	}
}
