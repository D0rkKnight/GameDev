package Graphics.Elements;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER_BINDING;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import Wrappers.Color;

/**
 * Renders a buffer to the screen
 * 
 * @author Hanzen Shou
 *
 */
public class DrawOrderBuffer extends DrawOrderElement {

	public DrawBuffer dbuff;

	public DrawOrderBuffer(int z, DrawBuffer dbuff) {
		super(z);

		this.dbuff = dbuff;
	}

	public void tryRender() {
		// TODO: causes errors

		dbuff.rend.render();
	}

	public void clear() {
		int currBuff = 0;

		// Retrieve current draw buffer
		try (MemoryStack stack = stackPush()) {
			IntBuffer id = stack.mallocInt(1);
			glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, id);

			currBuff = id.get();
		}

		dbuff.bind();

		Color.setGLClear(new Color(0, 0, 0, 0));
		glClear(GL_COLOR_BUFFER_BIT);

		glBindFramebuffer(GL_FRAMEBUFFER, currBuff);
	}

}
