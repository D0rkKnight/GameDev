package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Accessories.Accessory;
import Collision.HammerRightTriangle;
import Collision.HammerShape;
import Collision.HammerSquare;
import Collision.Physics;
import Entities.Entity;
import Entities.PhysicsEntity;
import Entities.Player;
import Rendering.SpriteRenderer;
import Rendering.SpriteShader;
import Tiles.SquareTile;
import Tiles.Tile;
import Wrappers.Hitbox;
import Wrappers.Rect;
import Wrappers.Texture;
import Wrappers.Vector2;


public class GameManager {

	// The frame and canvas
	/*
	 * private JFrame frame; private RendererOld canvas;
	 */
	public static long window;
	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	
	//Helper variable for frame walking
	public static boolean waitingForFrameWalk = true;
	
	/**
	 * Configuration and debug
	 */
	private static float timeScale = 1;
	public static boolean frameWalk = false;
	public static float frameDelta = 10f;
	
	//
	private Drawer drawer;
	public static SpriteRenderer renderer;
	public static SpriteShader shader;

	// Storage for tiles
	private ArrayList<Map> maps;
	private Map currmap;

	// Lookup table for different kinds of tiles
	private HashMap<Integer, Tile> tileLookup;
	// Lookup table for different kinds of accessories
	private HashMap<Integer, Accessory> accessoryLookup;
	// Lookup table for hammershapes
	public static HashMap<Integer, HammerShape> hammerLookup;

	// current progression of player ingame
	private int chapter; // chapter, determines plot events
	private int[] levelnums; // number of levels within each chapter - down to map design
	private int level; // level within each chapter (represents biomes
	private int room; // specific room within each level

	// Entity positions in current room
	static private ArrayList<Entity> entities;
	static private ArrayList<Entity> entityWaitingList;
	static private ArrayList<Entity> entityClearList;
	static private ArrayList<Hitbox> coll;
	
	private Serializer serializer;
	public static Player player;
	
	public static final int tileSize = 16;

	public static final int MOVE_AXIS_X = 0;
	public static final int MOVE_AXIS_Y = 1;
	public static final float NUDGE_CONSTANT = 0.1f;
	
	public static final int CORNER_NONE = 1;
	public static final int CORNER_UL = 1;
	public static final int CORNER_BL = 2;
	public static final int CORNER_UR = 3;
	public static final int CORNER_BR = 4;

	/*
	 * Creates components before entering loop
	 */
	GameManager() {
		// Initialization
		init();
		loop();

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void config() {
		timeScale = 1f;
		frameWalk = true;
		frameDelta = 20f;
	}
	
	private void init() {
		config();
		initTime();
		
		serializer = new Serializer();
		initGraphics();
		initInput();
		drawer = new Drawer();

		//Init camera
		new Camera();
		
		//Init renderer
		//TODO: We have to make a renderer factory in order for this to, like, work.
		shader = new SpriteShader("texShader");
				SpriteRenderer sprRenderer = new SpriteRenderer(shader);
				sprRenderer.spr = new Texture("tile1.png");
				renderer = sprRenderer;
		
		
		//Init player
		initEntities();
		initCollision();
		initPlayer();
		Camera.main.attach(player);
		
		
		
		
		initTiles();
		
		/*
		try {
			serializer.loadMap("place holder file name", tileLookup); //TODO set up code to load each map that is needed in the level
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		//Just do this for now
		Tile[][] mapData = new Tile[100][100];
		Tile tile = tileLookup.get(1);
		try {
			mapData[2][0] = tile.clone();
			mapData[0][3] = tile.clone();
			mapData[2][15] = tile.clone();
			for (int i=0; i<mapData.length; i++) {
				mapData[i][0] = tile.clone();
				mapData[i][30] = tile.clone();
				mapData[0][i] = tile.clone();
				mapData[50][i] = tile.clone();
				
				if (i>20) mapData[i][i-20] = tile.clone();
				if (i>20) mapData[i][i-15] = tile.clone();
				if (i<=10) mapData[i][10-i] = tile.clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		
		//Tiles are currently raw and unitialized. Initialize them.
		for (int i=0; i<mapData.length; i++) for (int j=0; j<mapData[0].length; j++) {
			Tile t = mapData[i][j];
			if (t != null) {
				//Do this before the renderer is initialized
				if (i == j+20) {
					t.setHammerState(hammerLookup.get(HammerShape.HAMMER_SHAPE_TRIANGLE_BR));
				}
				if (i > 20 && i == j+15) {
					t.setHammerState(hammerLookup.get(HammerShape.HAMMER_SHAPE_TRIANGLE_UL));
				}
				else if (i + j == 10) {
					t.setHammerState(hammerLookup.get(HammerShape.HAMMER_SHAPE_TRIANGLE_BL));
				}
				
				t.init(new Vector2(i*tileSize, j*tileSize), new Rect(tileSize, tileSize));
			}
		}
		
		
		currmap = new Map(mapData, null, null, null);//TODO
	}

	private void loadProgression() {

	}

	private void loadState() {

	}

	private void initGraphics() {
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
		Input.windowDims = new Vector2(windowW, windowH);
		window = glfwCreateWindow(windowW, windowH, "PLACEHOLDER TITLE", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create GLFW window");
		}

		
		Rect r = GetWindowSize();
			// Get resolution of primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Set pos
		glfwSetWindowPos(window, (vidmode.width() - (int) r.w) / 2, (vidmode.height() - (int) r.h) / 2);

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
	    glOrtho(0, r.w, 0, r.h, -1, 1);
	}
	
	private void initInput() {
		// Setup key callbacks (includes a lambda, fun.)
		// We can pass in a delegate to handle controls.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			Input.updateKeys(window, key, scancode, action, mods);
		});
		
		//I dunno why this has to be different for mouse inputs...
		glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
		    @Override
		    public void invoke(final long window, final int button, final int action, final int mods) {
		    	Input.updateMouse(window, button, action, mods);
		    }
	    });
		
		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {

			@Override
			public void invoke(long window, double xPos, double yPos) {
				Input.updateCursor(xPos, yPos);
			}
			
		});
		
		glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {

			@Override
			public void invoke(long window, int width, int height) {
				Input.updateWindowSize(width, height);
			}
			
		});
	}
	
	public static Rect GetWindowSize() {
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

			return new Rect(pWidth.get(0), pHeight.get(0));
		}
		// Another benefit: garbage collection bsery is avoided because the stack is
		// popped and reclaimed immediately after the try block.
	}

	/*
	 * Loads and constructs tiles based off of external file, then logs in
	 * tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();


		BufferedImage img = serializer.loadImage("tile1.png");
		SquareTile t1 = new SquareTile(1, img, renderer);

		tileLookup.put(1, t1);
	}

	private void finishArea() { // called when character finishes a major area, updates level and chapter of
								// character

	}
	
	private void initEntities() {
		entities = new ArrayList();
		entityWaitingList = new ArrayList();
		entityClearList = new ArrayList();
	}
	
	private void initCollision() {
		coll = new ArrayList();
		
		//Generate hammer shapes
		hammerLookup = new HashMap<>();
		ArrayList<HammerShape> cache = new ArrayList<>();
		cache.add(new HammerSquare());
		
		for (int i=HammerShape.HAMMER_SHAPE_TRIANGLE_BL; i<=HammerShape.HAMMER_SHAPE_TRIANGLE_UR; i++) cache.add(new HammerRightTriangle(i));
			
		for (HammerShape h : cache) hammerLookup.put(h.shapeId, h);
	}
	
	public static void subscribeEntity(Entity e) {
		entityWaitingList.add(e);
		
		Hitbox hb = e.getHitbox();
		if (hb != null) coll.add(hb);
		else System.err.println("Collider not defined!");
	}
	
	public static void unsubscribeEntity(Entity e) {
		entityClearList.add(e);
		
		Hitbox hb = e.getHitbox();
		if (hb != null) coll.remove(hb);
		else System.err.println("Collider not defined!");
	}
	
	private void initPlayer() {
		player = new Player(0, new Vector2(100, 100), null, renderer, "Player", null);
		
		
		subscribeEntity(player);
	}

	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {
		// Set clear color
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

		// Into the rendering loop we go
		// Remember the lambda callback we attached to key presses? This is where the
		// function returns.
		while (!glfwWindowShouldClose(window)) {
			updateTime();
			
			// Clear frame buffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// Drawing stuff
			update();
			drawer.draw(currmap, entities);


			// tldr: there are two buffers, one that is being displayed and one that we are
			// writing to.
			// This function waits until one buffer is written to before writing the next
			// one.
			// This is because of v-sync.
			glfwSwapBuffers(window);

			// Event listening stuff. Key callback is invoked here.
			// Do wipe input before going in
			Input.update();
			glfwPollEvents();
			
			//Frame walking debug tools
			if (frameWalk) {
				while(waitingForFrameWalk) {
					Input.update();
					glfwPollEvents();
					
					if (glfwWindowShouldClose(window)) break;
				}
				waitingForFrameWalk = true;
			}
		}
	}
	
	private void initTime() {
		currTime = System.nanoTime() / 1000000;
		lastTime = currTime;
	}
	
	private void updateTime() {
		lastTime = currTime;
		currTime = System.nanoTime() / 1000000;
		
		deltaTime = currTime - lastTime;
	}
	
	public static long deltaT() {
		if (frameWalk) {
			return (long) frameDelta;
		}
		
		return (long) (deltaTime * timeScale);
	}
	
	public static long getFrameTime() {
		return currTime;
	}
	
	/**
	 * Called once per frame, and is responsible for updating internal game logic.
	 */
	private void update() {
		//Dump entity waiting list into entity list
		for (Entity e : entityWaitingList) {
			entities.add(e);
		}
		entityWaitingList.clear();
		
		//Filter deleted entities out
		for (Entity e : entityClearList) {
			entities.remove(e);
		}
		
		//Each entity makes decisions
		for (Entity ent : entities) {
			ent.calculate();
		}
		
		//Physics simulation step begin from here ________________________________________________________
		
		//Push in collision deltas
		Tile[][] grid = currmap.getGrid();
		for (Hitbox c : coll) {
			Physics.calculateDeltas(c, grid);
		}
		
		//Push physics outcomes
		for (Entity e : entities) {
			//Somehow need to avoid pushing the movement of entities that don't move.
			PhysicsEntity pe = (PhysicsEntity) e;
			if (pe.collidedWithTile) {
				pe.onTileCollision();
				pe.collidedWithTile = false;
			}
			e.pushMovement();
		}
		
		Camera.main.update();
	}

}
