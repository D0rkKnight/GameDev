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
import Entities.Framework.Entity;
import GameController.Camera;
import GameController.GameManager;
import GameController.Input;
import GameController.Map;
import Graphics.Elements.DrawBuffer;
import Graphics.Elements.DrawOrderElement;
import Graphics.Elements.DrawOrderEntities;
import Graphics.Elements.DrawOrderTileLayer;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Graphics.Elements.TileGFX;
import Graphics.Elements.TileRenderLayer;
import Graphics.Rendering.DrawBufferRenderer;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Graphics.Rendering.TimedRenderer;
import Graphics.Rendering.TimedShader;
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
	private static GeneralRenderer chunkRend;
	private static HashMap<LayerEnum, TileRenderLayer> tLayers;

	public static enum LayerEnum {
		BG(-50), FG(50), GROUND(0);

		public int z;

		LayerEnum(int z) {
			this.z = z;
		}
	}

	public static boolean windowResizeable = false; // Set in debug
	public static final int CHUNK_SIZE = 16;

	public static Color clearCol;

	// GFX layers are marked with strings so they can be dynamically generated
	private static HashMap<String, TileRenderLayer> GFXLayers;
	private static HashMap<String, GeneralRenderer> GFXRends;

	private static ArrayList<DrawOrderElement> drawOrder;

	public static GeneralRenderer TEST_RENDERER;

	public static void draw(Map map, ArrayList<Entity> entities) {
		// Draw to framebuffer please
		glBindFramebuffer(GL_FRAMEBUFFER, drawBuff.fbuff);

		// Clear frame buffer
		Color.setGLClear(clearCol);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Get clipping bounds
		Camera cam = Camera.main;
		Vector2f cPos = cam.pos;
		Vector2f cDims = cam.viewport;
		int txMin = (int) (cPos.x - (cDims.x / 2));
		int txMax = (int) (cPos.x + (cDims.x / 2));
		int tyMin = (int) (cPos.y - (cDims.y / 2));
		int tyMax = (int) (cPos.y + (cDims.y / 2));

		txMin /= GameManager.tileSize * CHUNK_SIZE;
		txMax /= GameManager.tileSize * CHUNK_SIZE;
		tyMin /= GameManager.tileSize * CHUNK_SIZE;
		tyMax /= GameManager.tileSize * CHUNK_SIZE;

		txMax++;
		tyMax++;

		// TODO: Layer width and height should be set elsewhere
		Tile[][] tArr = tLayers.get(LayerEnum.GROUND).chunkRendGrid.get(0);
		int chunkGridW = (int) Math.ceil(((double) tArr.length) / CHUNK_SIZE); // This does just raises the chunk width
																				// up to accomodate for the remainder.
		int chunkGridH = (int) Math.ceil(((double) tArr[0].length) / CHUNK_SIZE);

		txMin = Math.max(txMin, 0);
		txMax = Math.min(txMax, chunkGridW);
		tyMin = Math.max(tyMin, 0);
		tyMax = Math.min(tyMax, chunkGridH);

		// TESTING: Draw the temp renderer instead
		TEST_RENDERER.render();

		for (DrawOrderElement doe : drawOrder) {
			if (doe instanceof DrawOrderTileLayer) {
				// TESTING: Do nothing instead
				// ((DrawOrderTileLayer) doe).tryRender(txMin, txMax, tyMin, tyMax, chunkGridW);
			}

			if (doe instanceof DrawOrderEntities) {
				((DrawOrderEntities) doe).tryRender(entities);
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
		GFXLayers = new HashMap<>();
		GFXRends = new HashMap<>();
		drawOrder = new ArrayList<>();

		insertDrawOrderElement(new DrawOrderEntities(0));

		Shader warpShade = new TimedShader("vortex");
		GeneralRenderer warpRend = new TimedRenderer(warpShade);
		warpRend.init(new Transformation(), new Vector2f(CHUNK_SIZE * GameManager.tileSize), Shape.ShapeEnum.SQUARE,
				new Color(1, 1, 0, 1));
		GFXRends.put("Warp", warpRend);

		// Creating graphical elements
		initDrawBuffer();
		initTileChunks();
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
		Shader shader = new SpriteShader("texShader");
		fBuffRend = new DrawBufferRenderer(shader);
		drawBuff = DrawBuffer.genEmptyBuffer(1280, 720, fBuffRend);

		// Reset to the regular buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public static void initTileChunks() {
		tLayers = new HashMap<>();

		// Setting up the renderer
		chunkRend = new GeneralRenderer(new SpriteShader("texShader"));

		float dim = GameManager.tileSize * Drawer.CHUNK_SIZE;
		chunkRend.init(new Transformation(new Vector2f(), Transformation.MatrixMode.WORLD), new Vector2f(dim, dim),
				Shape.ShapeEnum.SQUARE, new Color());
	}

	/**
	 * Bakes tiles into a set of chunks
	 * 
	 * @param g              the hash map holding all tiles
	 * @param renderedLayers the layers within the hash map that should be baked
	 * @param targetLayer    the name of the layer that is being baked to (BG, FG,
	 *                       etc. etc.)
	 * @param z              z value used to index layer in draw order
	 */
	public static void genTileChunkLayer(HashMap<String, Tile[][]> g, ArrayList<String> renderedLayers,
			LayerEnum targetLayer) {
		TileRenderLayer trl;
		if (tLayers.get(targetLayer) == null) {
			trl = new TileRenderLayer();
			tLayers.put(targetLayer, trl);

			insertDrawOrderElement(new DrawOrderTileLayer(targetLayer.z, trl));
		} else {
			trl = tLayers.get(targetLayer);
		}

		trl.populateChunks(g, renderedLayers, chunkRend);
		if (!renderedLayers.isEmpty())
			trl.isActive = true;
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

	public static void bakeGFX() {
		// Clear GFX
		for (String key : GFXLayers.keySet()) {
			GFXLayers.get(key).clearGrids();
		}

		// Search through every tile layer for tiles that need to be baked
		for (LayerEnum key : tLayers.keySet()) {
			TileRenderLayer tl = tLayers.get(key);

			if (!tl.isActive)
				continue;

			ArrayList<Tile[][]> grids = tl.chunkRendGrid;

			TileRenderLayer gfxLayer;
			HashMap<String, Tile[][]> gridCache = new HashMap<>();

			for (Tile[][] g : grids) {
				for (int i = 0; i < g.length; i++) {
					for (int j = 0; j < g[0].length; j++) {
						Tile t = g[i][j];
						if (t == null)
							continue;

						for (TileGFX gfx : t.tGFX) {
							// Load tiles into chunk render grid

							// Generate new layer
							if (!GFXLayers.containsKey(gfx.name)) {
								gfxLayer = new TileRenderLayer();

								GFXLayers.put(gfx.name, gfxLayer);

								// TODO: Set z layer dynamically
								insertDrawOrderElement(new DrawOrderTileLayer(-3, gfxLayer));
							}

							// Generate new cache
							if (!gridCache.containsKey(gfx.name)) {
								gridCache.put(gfx.name, new Tile[g.length][g[0].length]);
							}

							// Stick the tile into a layer
							gridCache.get(gfx.name)[i][j] = t;
						}
					}
				}
			}

			// Now load the grid caches into the draw layers.
			for (String cacheKey : gridCache.keySet()) {
				Tile[][] g = gridCache.get(cacheKey);
				TileRenderLayer gfxL = GFXLayers.get(cacheKey);

				// Grids are now in
				gfxL.appendSingleGrid(g);

				// Now draw to the GFXLayer!
				gfxL.drawChunks(GFXRends.get(cacheKey));

				// And enable it
				gfxL.isActive = true;
			}
		}
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

	public static void disableTCLayers() {
		for (LayerEnum key : tLayers.keySet()) {
			tLayers.get(key).isActive = false;
		}

		for (String key : GFXLayers.keySet()) {
			GFXLayers.get(key).isActive = false;
		}
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
		HashMap<Texture, ArrayList<Vector2f>> positionData = new HashMap<>();
		HashMap<Texture, ArrayList<Vector2f>> UVData = new HashMap<>();

		// Used to access data for testing
		Texture tempTex = null;

		// For every layer, create vertex data to be submitted to renderers
		for (String str : renderedLayers) {
			Tile[][] grid = g.get(str);

			// Traverse grid
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					Tile t = grid[i][j];

					SubTexture subTex = t.subTex;
					Texture tex = subTex.tex;

					tempTex = tex;

					// Submit new member to pairing hash map if foreign
					if (!positionData.containsKey(tex)) {
						positionData.put(tex, new ArrayList<Vector2f>());
					}
					if (!UVData.containsKey(tex)) {
						UVData.put(tex, new ArrayList<Vector2f>());
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
						positionData.get(tex).add(v);
					}

					// Generate appropriate UV data
					Vector2f[] uvs = subTex.genSubUV(t.shape.v);
					for (Vector2f uv : uvs)
						UVData.get(tex).add(uv);
				}
			}
		}

		// Check data
//		int i = 0;
//		for (Texture tex : UVData.keySet()) {
//			for (Vector2f v : UVData.get(tex)) {
//				i++;
//				System.out.println(v);
//
//				if (i > 100)
//					break;
//			}
//
//			System.out.println("NOT GOOD");
//		}
		ArrayList<Vector2f> posArr = positionData.get(tempTex);
		Vector2f[] posOut = new Vector2f[posArr.size()];
		for (int i = 0; i < posOut.length; i++) {
			posOut[i] = posArr.get(i);
		}

		ArrayList<Vector2f> uvArr = UVData.get(tempTex);
		Vector2f[] uvOut = new Vector2f[uvArr.size()];
		for (int i = 0; i < uvOut.length; i++) {
			uvOut[i] = uvArr.get(i);
		}

		// Add to a renderer and store that renderer
		TEST_RENDERER = new GeneralRenderer(new SpriteShader("texShader"));
		TEST_RENDERER.init(new Transformation(new Vector2f(), Transformation.MatrixMode.WORLD), posOut, uvOut,
				new Color());
		TEST_RENDERER.spr = tempTex;
	}
}
