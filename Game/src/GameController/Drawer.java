package GameController;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Debug.Debug;
import Entities.Entity;
import Tiles.Tile;

/*
 * Calls shaders to render themselves.
 */
public class Drawer {
	public static long window;
	
	public static void draw(Map map, ArrayList<Entity> entities) {
		// Clear frame buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		//calls render function of every tile of map within distance of player, and entities within certain distance
  		Tile[][] grid = map.getGrid();
  		
  		//Get clipping bounds
  		Camera cam = Camera.main;
  		Vector2f cPos = cam.pos;
  		Vector2f cDims = cam.viewport;
  		int txMin = (int) (cPos.x - (cDims.x));
  		int txMax = (int) (cPos.x + (cDims.x));
  		int tyMin = (int) (cPos.y - (cDims.y));
  		int tyMax = (int) (cPos.y + (cDims.y));
  		
  		System.out.println(cDims.toString());
  		
  		txMin /= GameManager.tileSize;
  		txMax /= GameManager.tileSize;
  		tyMin /= GameManager.tileSize;
  		tyMax /= GameManager.tileSize;
  		
  		txMax ++;
  		tyMax ++;
  		
  		int gridW = grid.length;
  		int gridH = grid[0].length;
  		
  		txMin = Math.max(txMin, 0);
  		txMax = Math.min(txMax, gridW);
  		tyMin = Math.max(tyMin, 0);
  		tyMax = Math.min(tyMax, gridH);
  		
  		for (int i=txMin; i<txMax; i++) {
  			for (int j=tyMin; j<tyMax; j++) {
  				Tile tile = grid[i][j];
  				if (tile == null) continue;
  				
  				tile.render(new Vector2f(i*GameManager.tileSize, j*GameManager.tileSize), GameManager.tileSize);
  			}
  		}
  		
  		for (Entity ent : entities) {
  			ent.render();
  		}
  		
  		//Overlay debug elements
  		Debug.renderDebug();
  		
  		
  		// tldr: there are two buffers, one that is being displayed and one that we are
		// writing to.
		// This function waits until one buffer is written to before writing the next
		// one.
		// This is because of v-sync.
		glfwSwapBuffers(Drawer.window);
	}
	
	public static void initGraphics() {
		// Error callback
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize glfw, pretty important. Anything glfw stuff that happens before
		// this will break.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);// Window will be resizeable

		// Create the window!
		// Note: NULL is a constant that denotes the null value in OpenGL. Not the same
		// thing as Java null.
		int windowW = 1280;
		int windowH = 720;
		Input.windowDims = new Vector2f(windowW, windowH);
		window = glfwCreateWindow(windowW, windowH, "PLACEHOLDER TITLE", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create GLFW window");
		}

		
		Vector2f r = GetWindowSize();
			// Get resolution of primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Set pos
		glfwSetWindowPos(window, (vidmode.width() - (int) r.x) / 2, (vidmode.height() - (int) r.y) / 2);

		// Tells the GPU to write to this window.
		glfwMakeContextCurrent(window);

		// V-SYNC!!!
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		// Creating the context to which all graphics operations will be executed upon
		GL.createCapabilities();
		glEnable(GL_TEXTURE_2D);
		
		// select projection matrix (controls view on screen)
	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    // set ortho to same size as viewport, positioned at 0,0
	    // TODO: Figure out what this like, does.
	    glOrtho(0f, r.x, 0f, r.y, -1f, 1f);
	    glPushMatrix();
	}
	
	public static Vector2f GetWindowSize() {
		// A wack process required to move the window. Why this is necessary, I'm not
		// entirely clear on.
		// Oh I think it's because the wrapped OpenGL function has to return multiple
		// values so
		// this allocates the memory in a manner that C recognizes.
		// Read more on MemoryStack as a solution to interfacing problems between the
		// two languages.
		
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			// Get window size
			glfwGetWindowSize(window, pWidth, pHeight);

			return new Vector2f(pWidth.get(0), pHeight.get(0));
		}
		// Another benefit: garbage collection bsery is avoided because the stack is
		// popped and reclaimed immediately after the try block.
	}
}
