package Graphics;

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
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.TestSpace;
import Entities.Framework.Entity;
import GameController.Camera;
import GameController.GameManager;
import GameController.Input;
import GameController.Map;
import Graphics.Elements.DrawBuffer;
import Graphics.Elements.DrawOrderElement;
import Graphics.Elements.DrawOrderEntities;
import Graphics.Elements.DrawOrderRenderers;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Graphics.Rendering.DrawBufferRenderer;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Graphics.Rendering.TimedRenderer;
import Graphics.Rendering.TimedShader;
import Graphics.text.Text;
import Tiles.Tile;
import UI.UI;
import Utility.Transformation;
import Wrappers.Color;

/*
 * Calls shaders to render themselves.
 */
public class Drawer {
	public static long window;

	public static DrawBuffer drawBuff;
	private static DrawBufferRenderer fBuffRend;

	public static enum LayerEnum {
		BG(-50), FG(50), GROUND(0);

		public int z;

		LayerEnum(int z) {
			this.z = z;
		}
	}

	public static boolean windowResizeable = false; // Set in debug
	public static Color clearCol;

	// GFX layers are marked with strings so they can be dynamically generated

	private static ArrayList<DrawOrderElement> drawOrder;

	public static void draw(Map map, ArrayList<Entity> entities) {
		// Draw to framebuffer please
		glBindFramebuffer(GL_FRAMEBUFFER, drawBuff.fbuff);

		// Clear frame buffer
		Color.setGLClear(clearCol);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		for (DrawOrderElement doe : drawOrder) {
			if (doe instanceof DrawOrderEntities) {
				((DrawOrderEntities) doe).tryRender(entities);
			}

			else if (doe instanceof DrawOrderRenderers) {
				((DrawOrderRenderers) doe).tryRender();
			}
		}

		// UI elements
		UI.render();

		/**
		 * Now draw the texture to the screen as a quad
		 */
		if (!fBuffRend.hasInit) {
			fBuffRend.init(
					new Transformation(new Vector2f(0, Camera.main.viewport.y), Transformation.MatrixMode.SCREEN),
					Camera.main.viewport, Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0));
			fBuffRend.spr = drawBuff.tex;
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		fBuffRend.render();

		Debug.renderDebug();

		TestSpace.draw();

		// Yeesh that was hard.
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
		// Setup
		clearCol = new Color(0.5f, 0.5f, 0.5f, 1);
		initOpenGL();

		// Creating graphical framework
		drawOrder = new ArrayList<>();

		insertDrawOrderElement(new DrawOrderEntities(0));

		// Creating graphical elements
		initDrawBuffer();

		// Initializing text elements
		Text.init();

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
		if (windowResizeable)
			glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);// Window will not be resizeable.
		else
			glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

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
		Color.setGLClear(clearCol);
	}

	private static void initDrawBuffer() {
		/**
		 * Draw buffer configuration
		 */

		// Now set up the renderer that deals with this.
		Shader shader = SpriteShader.genShader("texShader");
		fBuffRend = new DrawBufferRenderer(shader);
		drawBuff = DrawBuffer.genEmptyBuffer(1280, 720, fBuffRend);

		// Reset to the regular buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public static Vector2f GetWindowSize() {
		// Stack used because C++, runs on CPU or something.
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			// Get window size
			glfwGetWindowSize(window, pWidth, pHeight);

			return new Vector2f(pWidth.get(0), pHeight.get(0));
		}
	}

	public static void clearScreenBuffer() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		// Clear both buffers
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glfwSwapBuffers(Drawer.window);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	private static void insertDrawOrderElement(DrawOrderElement doe) {
		for (int i = 0; i < drawOrder.size(); i++) {
			if (doe.z < drawOrder.get(i).z) {
				drawOrder.add(i, doe);
				return;
			}
		}

		// Add onto end
		drawOrder.add(doe);
	}

	/**
	 * Generates a list of texture-vertex array pairings for a single visual layer
	 * of tiles
	 * 
	 * @param g              Hash map of all tile grids
	 * @param renderedLayers Layers from hash map to render
	 * @param targetLayer    Visual layer to draw to
	 */
	public static void generateLayerVertexData(HashMap<String, Tile[][]> g, ArrayList<String> renderedLayers,
			LayerEnum targetLayer) {
		// TODO: Probably should combine these two hashmaps
		// TODO: Also they need to keyed to shaders AND textures for GFX stuff. One more
		// layer of Hashmaps?
		HashMap<VertexLayerKey, ArrayList<Vector2f>> positionData = new HashMap<>();
		HashMap<VertexLayerKey, ArrayList<Vector2f>> UVData = new HashMap<>();

		// For every layer, create vertex data to be submitted to renderers
		for (String str : renderedLayers) {
			Tile[][] grid = g.get(str);

			// Traverse grid
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					Tile t = grid[i][j];
					if (t == null)
						continue;

					SubTexture subTex = t.subTex;
					Texture tex = subTex.tex;
					String gfxName = t.tGFX.name;

					VertexLayerKey key = new VertexLayerKey(tex, gfxName);

					// Submit new member to pairing hash map if foreign
					boolean hasKey = false;
					for (VertexLayerKey pastKey : positionData.keySet()) {
						if (pastKey.equals(key)) {
							hasKey = true;
							key = pastKey; // Need to retrieve past key to add to position data. Retains a
											// singleton design of keys, in a sense.
							break;
						}
					}
					if (!hasKey) {
						positionData.put(key, new ArrayList<Vector2f>());
						UVData.put(key, new ArrayList<Vector2f>());
					}

					// Generate appropriate vertex position data (j<->x, i<->y)
					Vector2f[] verts = t.shape.v.getRenderVertices(new Vector2f(GameManager.tileSize));
					float xOffset = i * GameManager.tileSize;
					float yOffset = j * GameManager.tileSize;

					for (Vector2f v : verts) {
						v.add(xOffset, yOffset);
					}

					// Submit vertex position data
					for (Vector2f v : verts) {
						positionData.get(key).add(v);
					}

					// Generate appropriate UV data
					Vector2f[] uvs = subTex.genSubUV(t.shape.v);
					for (Vector2f uv : uvs)
						UVData.get(key).add(uv);
				}
			}
		}

		// Create DOE to store renderers
		DrawOrderRenderers doe = new DrawOrderRenderers(targetLayer.z);
		doe.destroyOnSceneChange = true;

		for (VertexLayerKey key : positionData.keySet()) {

			// Send data to renderer
			ArrayList<Vector2f> posArr = positionData.get(key);
			Vector2f[] posOut = new Vector2f[posArr.size()];
			for (int i = 0; i < posOut.length; i++) {
				posOut[i] = posArr.get(i);
			}

			ArrayList<Vector2f> uvArr = UVData.get(key);
			Vector2f[] uvOut = new Vector2f[uvArr.size()];
			for (int i = 0; i < uvOut.length; i++) {
				uvOut[i] = uvArr.get(i);
			}

			// Add to a renderer and store that renderer
			GeneralRenderer rend = null;
			if (key.gfx.equals("None")) {
				rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
				rend.init(new Transformation(new Vector2f(), Transformation.MatrixMode.WORLD), posOut, uvOut,
						new Color());
				rend.spr = key.tex;
			}

			else if (key.gfx.equals("Warp")) {
				Shader warpShade = TimedShader.genShader("vortex");
				rend = new TimedRenderer(warpShade);
				rend.init(new Transformation(new Vector2f(), Transformation.MatrixMode.WORLD), posOut, uvOut,
						new Color(1, 1, 0, 1));
				rend.spr = key.tex;
			}

			else {
				System.err.println("Fix this broken mess of a GFX system");
			}

			doe.addRend(rend);
		}

		// enqueue DOE
		insertDrawOrderElement(doe);
	}

	/**
	 * Occurs before loading of new map
	 */
	public static void onSceneChange() {

		// Destroy some DOEs
		for (int i = drawOrder.size() - 1; i >= 0; i--) {
			if (drawOrder.get(i).destroyOnSceneChange) {
				drawOrder.remove(i);
			}
		}
	}

	public static class VertexLayerKey {
		public Texture tex;
		public String gfx;

		public VertexLayerKey(Texture tex, String gfx) {
			this.tex = tex;
			this.gfx = gfx;
		}

		@Override
		public boolean equals(Object o) {
			VertexLayerKey key = (VertexLayerKey) o;

			if (key.tex == tex && key.gfx.equals(gfx))
				return true;

			return false;
		}
	}
}
