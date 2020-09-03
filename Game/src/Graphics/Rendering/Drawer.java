package Graphics.Rendering;

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
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
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
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Framework.Entity;
import GameController.Camera;
import GameController.GameManager;
import GameController.Input;
import GameController.Map;
import Graphics.Elements.Texture;
import Tiles.Tile;
import UI.UI;
import Utility.Transformation;
import Wrappers.Color;

/*
 * Calls shaders to render themselves.
 */
public class Drawer {
	public static long window;
	
	public static int drawBuff;
	public static int drawTex;
	private static DrawBufferRenderer fBuffRend;
	private static GeneralRenderer chunkRend;
	
	private static int[][] tileChunkBuffers;
	private static Texture[][] tileChunkTexs;
	public static final int CHUNK_SIZE = 16;
	
	public static void draw(Map map, ArrayList<Entity> entities) {
		//Draw to framebuffer please
		glBindFramebuffer(GL_FRAMEBUFFER, Drawer.drawBuff);
		
		// Clear frame buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  		
  		//Get clipping bounds
  		Camera cam = Camera.main;
  		Vector2f cPos = cam.pos;
  		Vector2f cDims = cam.viewport;
  		int txMin = (int) (cPos.x - (cDims.x/2));
  		int txMax = (int) (cPos.x + (cDims.x/2));
  		int tyMin = (int) (cPos.y - (cDims.y/2));
  		int tyMax = (int) (cPos.y + (cDims.y/2));
  		
  		txMin /= GameManager.tileSize * CHUNK_SIZE;
  		txMax /= GameManager.tileSize * CHUNK_SIZE;
  		tyMin /= GameManager.tileSize * CHUNK_SIZE;
  		tyMax /= GameManager.tileSize * CHUNK_SIZE;
  		
  		txMax ++;
  		tyMax ++;
  		
  		int chunkGridW = tileChunkTexs.length;
  		int chunkGridH = tileChunkTexs[0].length;
  		
  		txMin = Math.max(txMin, 0);
  		txMax = Math.min(txMax, chunkGridW);
  		tyMin = Math.max(tyMin, 0);
  		tyMax = Math.min(tyMax, chunkGridH);
  		
  		//Draw all of the textures
  		for (int i=txMin; i<txMax; i++) {
  			for (int j=tyMin; j<tyMax; j++) {
  				Texture tex = tileChunkTexs[i][j];
  				
  				chunkRend.spr = tex;
  				
  				Vector2f pos = new Vector2f(i, j).mul(GameManager.tileSize).mul(CHUNK_SIZE);
  				chunkRend.transform.pos.set(pos);
  				chunkRend.transform.pos.add(0, GameManager.tileSize * CHUNK_SIZE);
  				
  				chunkRend.transform.scale.identity().scale(1, -1, 1);
  				
  				chunkRend.render();
  			}
  		}
  		
  		for (Entity ent : entities) {
  			ent.render();
  		}
  		
  		//UI elements
  		UI.render();
  		
  		/**
  		 * Now draw the texture to the screen as a quad
  		 */
  		if (!fBuffRend.hasInit) {
  			fBuffRend.init(new Transformation(new Vector2f(0, Camera.main.viewport.y), Transformation.MATRIX_MODE_SCREEN), 
  					Camera.main.viewport, HammerShape.HAMMER_SHAPE_SQUARE, new Color(0, 0, 0, 0));
  			fBuffRend.spr = new Texture(drawTex);
  		}
  		
  		glBindFramebuffer(GL_FRAMEBUFFER, 0);
  		fBuffRend.render();
  		
  		//Overlay debug elements
  		Debug.renderDebug();
  		
  		
  		//Yeesh that was hard.
//  		ByteBuffer pixels = BufferUtils.createByteBuffer(20*20*4);
//  		glReadPixels(0, 0, 20, 20, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
//  		
//		byte[] arr = new byte[20*20*4];
//		pixels.get(arr);
//		
//		for(byte b:arr) System.err.println(b);
  		
  		
//  		glBindFramebuffer(GL_FRAMEBUFFER, Drawer.drawBuff);
//  		fBuffRend.spr.bind();
//  		//This has been quite the experience.
//  		ByteBuffer pixels = BufferUtils.createByteBuffer(1280*720*4);
//  		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
//  		
//  		byte[] arr = new byte[1280*720*4];
//  		pixels.get(arr);
//  		
//  		for(byte b:arr) System.err.println(b);
  		
  		// Cache the buffer, load the one cached last frame.
  		// VSync affects this
		glfwSwapBuffers(Drawer.window);
	}
	
	public static void initGraphics() {
		initOpenGL();
		
		initDrawBuffer();
	}
	
	private static void initOpenGL() {
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
		
		// Set clear color
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
	}
	
	private static void initDrawBuffer() {
		/**
		 * Draw buffer configuration
		 */
		
		drawBuff = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, drawBuff);
		
		drawTex = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, drawTex);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1280, 720, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		
		// Poor filtering. Needed !
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		//Bind texture to active frame buffer (not the same thing as "drawBuff", it is GL_COLOR_ATTACHMENT0 in this case. It's just a static buffer.)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, drawTex, 0);
		
		//Configure that color0 is to be drawn to by shaders.
		glDrawBuffers(GL_COLOR_ATTACHMENT0);
		
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			System.err.println("Error!!!");
		}
		
		//Reset to the regular buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		
		
		//Now set up the renderer that deals with this.
		Shader shader = new SpriteShader("texShader");
		fBuffRend = new DrawBufferRenderer(shader);
	}
	
	public static void initTileChunks(Tile[][] grid) {
		int w=grid.length;
		int h=grid[0].length;
		
		if (w%CHUNK_SIZE != 0 || h%CHUNK_SIZE != 0) {
			new Exception("Bad size!").printStackTrace();
		}
		
		tileChunkBuffers = new int[w/CHUNK_SIZE][h/CHUNK_SIZE];
		tileChunkTexs = new Texture[tileChunkBuffers.length][tileChunkBuffers[0].length];
		
		for (int i=0; i<tileChunkBuffers.length; i++) {
			for (int j=0; j<tileChunkBuffers[0].length; j++) {
				//Gen the buffer
				int buff = glGenFramebuffers();
				tileChunkBuffers[i][j] = buff;
				glBindFramebuffer(GL_FRAMEBUFFER, buff);
				
				int tex = glGenTextures();
				
				glBindTexture(GL_TEXTURE_2D, tex);
				
				int chunkDims = CHUNK_SIZE * GameManager.tileSize;
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, chunkDims, chunkDims, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
				
				tileChunkTexs[i][j] = new Texture(tex, chunkDims, chunkDims);
				
				// Poor filtering. Needed !
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				
				//Bind texture to active frame buffer (not the same thing as "drawBuff", it is GL_COLOR_ATTACHMENT0 in this case. It's just a static buffer.)
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
				
				//Configure that color0 is to be drawn to by shaders.
				glDrawBuffers(GL_COLOR_ATTACHMENT0);
				
				if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
					System.err.println("Error!!!");
				}
				
				// Clear frame buffer, sets background color
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				//Now iterate over every tile in this grid that lies within the chunk
				for (int a=i*CHUNK_SIZE; a<(i+1)*CHUNK_SIZE; a++) {
					for (int b=j*CHUNK_SIZE; b<(j+1)*CHUNK_SIZE; b++) {
						Tile t = grid[a][b];
						if (t == null) continue;
						
						Transformation oldTrans = t.renderer.transform;
						
						Transformation newTrans = new Transformation(new Vector2f(oldTrans.pos), Transformation.MATRIX_MODE_STATIC);
						t.renderer.transform = newTrans;
						
						float offsetX = i * GameManager.tileSize * CHUNK_SIZE;
						float offsetY = j * GameManager.tileSize * CHUNK_SIZE;
						
						Vector2f camOffset = new Vector2f(Camera.main.viewport).div(2);
						newTrans.view.setTranslation(-offsetX-camOffset.x, -offsetY-camOffset.y, 0);
						
						float x = a*GameManager.tileSize;
						float y = b*GameManager.tileSize;
						t.render(new Vector2f(x, y), GameManager.tileSize);
						
						t.renderer.transform = oldTrans;
					}
				}
			}
		}
		
		//Free the frame buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		//Setting up the renderer
		chunkRend = new GeneralRenderer(new SpriteShader("texShader"));
		
		float dim = GameManager.tileSize * CHUNK_SIZE;
		chunkRend.init(new Transformation(new Vector2f(), Transformation.MATRIX_MODE_WORLD), new Vector2f(dim, dim), 
				HammerShape.HAMMER_SHAPE_SQUARE, new Color());
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
		// Another benefit: garbage collection is avoided because the stack is
		// popped and reclaimed immediately after the try block.
	}
}