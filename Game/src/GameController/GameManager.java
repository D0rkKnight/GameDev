package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
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
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
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

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Accessories.Accessory;
import Entities.Combatant;
import Entities.Entity;
import Entities.Player;
import Rendering.Shader;
import Rendering.SpriteRenderer;
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
	private static boolean[] keyStates;
	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	
	//
	private Drawer drawer;
	private SpriteRenderer renderer;

	// Storage for tiles
	private ArrayList<Map> maps;
	private Map currmap;

	// Lookup table for different kinds of tiles
	private HashMap<Integer, Tile> tileLookup;
	// Lookup table for different kinds of accessories
	private HashMap<Integer, Accessory> accessoryLookup;

	// current progression of player ingame
	private int chapter; // chapter, determines plot events
	private int[] levelnums; // number of levels within each chapter - down to map design
	private int level; // level within each chapter (represents biomes
	private int room; // specific room within each level

	// Entity positions in current room
	private ArrayList<Entity> entities;
	
	private ArrayList<Hitbox> coll;
	
	private Serializer serializer;
	private Player player;
	
	private final int tileSize = 16;

	public static final int MOVE_AXIS_X = 0;
	public static final int MOVE_AXIS_Y = 1;
	public static final float NUDGE_CONSTANT = 0.1f;

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

	private void init() {
		initTime();
		
		serializer = new Serializer();
		keyStates = new boolean[GLFW_KEY_LAST];
		initGraphics();
		drawer = new Drawer();

		//Init camera
		new Camera();
		
		//Init renderer
		//TODO: We have to make a renderer factory in order for this to, like, work.
		Shader shader = new Shader("texShader");
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
				mapData[i][10] = tile.clone();
				mapData[0][i] = tile.clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		
		//Tiles are currently raw and unitialized. Initialize them.
		for (int i=0; i<mapData.length; i++) for (int j=0; j<mapData[0].length; j++) {
			Tile t = mapData[i][j];
			if (t != null) t.init(new Vector2(i*tileSize, j*tileSize), new Rect(tileSize, tileSize));
		}
		
		
		currmap = new Map(mapData, null, null, null);//TODO
	}
	
	/*
	 * This is the LWJGL backed input solution.
	 */
	public void inputListener(long window, int key, int scancode, int action, int mods) {
		//Record key states here
		if (action == GLFW_PRESS) keyStates[key] = true;
		if (action == GLFW_RELEASE) keyStates[key] = false;
		
		//Individual press and release stuff
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(window, true); // Later detected in rendering loop
		}
		
		//Player movement!
		float moveX = 0;
		if (keyStates[GLFW_KEY_D]) moveX ++;
		if (keyStates[GLFW_KEY_A]) moveX --;
		player.input.moveX = moveX;
		
		float moveY = 0;
		if (keyStates[GLFW_KEY_W]) moveY ++;
		if (keyStates[GLFW_KEY_S]) moveY --;
		player.input.moveY = moveY;
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
		window = glfwCreateWindow(1280, 720, "PLACEHOLDER TITLE", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create GLFW window");
		}

		// Setup key callbacks (includes a lambda, fun.)
		// We can pass in a delegate to handle controls.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			inputListener(window, key, scancode, action, mods);
		});

		
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
	}
	
	private void initCollision() {
		coll = new ArrayList();
	}
	
	private void subscribeEntity(Entity e) {
		entities.add(e);
		
		Hitbox hb = e.getHitbox();
		if (hb != null) coll.add(hb);
	}
	
	private void initPlayer() {
		player = new Player(0, new Vector2(100, 100), null, renderer, "Player", null);
		
		
		subscribeEntity(player);
	}

	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {
		
		Shader shader = new Shader("shader");
		
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
			glfwPollEvents();

			// canvas.paint();
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
		return deltaTime;
	}
	
	private void update() {
		//Each entity makes decisions
		for (Entity ent : entities) {
			ent.calculate();
		}
		
		//Physics simulation step begin from here ________________________________________________________
		
		//Push in collision deltas
		Tile[][] grid = currmap.getGrid();
		for (Hitbox c : coll) {
			Combatant e = c.owner;
			
			//Presume to be free falling, until able to prove otherwise
			e.grounded = false;
			
			//Just take the corner and assume this to be a good position to move from.
			//Grab projected movement
			Vector2 deltaMove = new Vector2(e.xVelocity * GameManager.deltaT(), e.yVelocity * GameManager.deltaT());
			Vector2 velo = new Vector2(e.xVelocity, e.yVelocity);
			
			//Calculate projected position
			Vector2 rawPos = e.getPosition();
			

			
			//Holds data to be pushed later, when reasonable movement is achieved
			Vector2 deltaTemp = new Vector2(deltaMove.x, deltaMove.y);
			
			//Split into cycles to check physics
			//yCycle
			int tilesTraversed = (int) Math.ceil(Math.abs(deltaMove.y/tileSize));
			int cycles = Math.max(tilesTraversed, 1);
			for (int i=0; i<cycles; i++) {
				//Now inch forwards with increasingly larger deltas
				Vector2 deltaInch = new Vector2(0, deltaMove.y * (i+1)/cycles);
				
				boolean isSuccess = moveTo(rawPos, deltaInch, velo, e, grid, MOVE_AXIS_Y);
				if (!isSuccess) {
					//The inch is more legitimate than deltaMove now
					//I think I'll have to split it into a horizontal move and a vertical move
					//Push the vertical change only.
					deltaTemp.y = deltaInch.y;
					
					break;
				}
			}
			
			
			//xCycle
			tilesTraversed = (int) Math.ceil(Math.abs(deltaMove.x/tileSize));
			cycles = Math.max(tilesTraversed, 1);

			for (int i=0; i<cycles; i++) {
				//Now inch forwards with increasingly larger deltas
				Vector2 deltaInch = new Vector2(deltaMove.x * (i+1)/cycles, 0);
				
				boolean isSuccess = moveTo(rawPos, deltaInch, velo, e, grid, MOVE_AXIS_X);
				if (!isSuccess) {
					//The inch is more legitimate than deltaMove now
					//I think I'll have to split it into a horizontal move and a vertical move
					//Push the vertical change only.
					deltaTemp.x = deltaInch.x;
					
					break;
				}
			}
			
			deltaMove = deltaTemp;
			
			//Push changes
			e.setMoveDelta(deltaMove);
			e.yVelocity = velo.y;
			e.xVelocity = velo.x;
		}
		
		//Push physics outcomes
		for (Entity e : entities) {
			e.pushMovement();
		}
		
		Camera.main.update();
	}
	
	/**
	 * 
	 * @param pos
	 * @param delta
	 * @param e
	 * @return Whether or not the entity collided when attempting to move
	 */
	private boolean moveTo(Vector2 rawPos, Vector2 deltaMove, Vector2 velo, Entity e, Tile[][] grid, int moveAxis) {
		Vector2 ePos = new Vector2(rawPos.x + deltaMove.x, rawPos.y + deltaMove.y);
		int tileSpacePosX = (int) ePos.x/tileSize;
		int tileSpacePosY = (int) ePos.y/tileSize;
		
		//return var
		boolean isSuccess = true;
		
		//Only calculate within acceptable dims
		if (tileSpacePosX >= 0 && tileSpacePosY >= 0 && tileSpacePosX < grid.length && tileSpacePosY < grid[0].length) {
			
			boolean isOccupied = false;
			if (grid[tileSpacePosX][tileSpacePosY] != null) isOccupied = true;
			if (isOccupied) {
				isSuccess = false;
				
				//Figure out the direction of approach
				//This is done by figuring out where the entity is, relative to the tile.
				//Assume to be coming from the bottom left
				Vector2 snap = new Vector2(0, 0);
				
				//If the current, unmodified position is to the right of the tile, snap to the right side.
				if (rawPos.x > tileSpacePosX * tileSize) snap.x = 1;
				if (rawPos.y > tileSpacePosY * tileSize) snap.y = 1;
				
				//Do the intended snap
				snapTo(snap, moveAxis, velo, deltaMove, ePos, e);
			}
		}
		
		return isSuccess;
	}
	
	private void snapTo(Vector2 snap, int snapAxis, Vector2 velo, Vector2 deltaMove, Vector2 ePos, Entity e) {
		//Get distance to edge
		Vector2 modSpacePos = new Vector2(ePos.x % tileSize, ePos.y % tileSize);
		Vector2 modSpaceOppDist = new Vector2(tileSize - modSpacePos.x, tileSize - modSpacePos.y);
		
		if (snapAxis == MOVE_AXIS_Y) {
			//Snap up
			if (snap.y == 1) {
				//Limit ymove
				velo.y = Math.max(velo.y, 0);
				deltaMove.y += modSpaceOppDist.y;
				
				//You are now also grounded
				e.grounded = true;
			}
			
			//Snap down
			if (snap.y == 0) {
				velo.y = Math.min(velo.y, 0);
				deltaMove.y -= modSpacePos.y;
				
				//Note: moving down still leaves you within the tile, thanks to rounding. Nudge down to exit the tile.
				deltaMove.y -= NUDGE_CONSTANT;
			}
		}
		
		if (snapAxis == MOVE_AXIS_X) {
			//Snap right
			if (snap.x == 1) {
				//Limit xMove
				velo.x = Math.max(velo.x, 0);
				deltaMove.x += modSpaceOppDist.x;
			}
			
			//Snap left
			if (snap.x == 0) {
				velo.x = Math.min(velo.x, 0);
				deltaMove.x -= modSpacePos.x;
			}
		}
	}

}
