package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import Accessories.Accessory;
import Collision.HammerRightTriangle;
import Collision.HammerShape;
import Collision.HammerSquare;
import Collision.Physics;
import Debug.Debug;
import Entities.Entity;
import Entities.PhysicsEntity;
import Entities.Player;
import Rendering.SpriteRenderer;
import Rendering.SpriteShader;
import Tiles.SquareTile;
import Tiles.Tile;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Texture;


public class GameManager {

	// The frame and canvas
	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	
	//Helper variable for frame walking
	public static boolean waitingForFrameWalk = true;
	
	/**
	 * Configuration and debug
	 */
	public static float timeScale = 1;
	public static boolean frameWalk = false;
	public static float frameDelta = 10f;
	public static boolean showCollisions = false;
	public static boolean debugElementsEnabled = false;
	
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

		glfwFreeCallbacks(Drawer.window);
		glfwDestroyWindow(Drawer.window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}


	
	private void init() {
		initTime();
		
		serializer = new Serializer();
		Drawer.initGraphics();
		Input.initInput();
		Debug.init();
		

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
				if (i>20) mapData[i][i-14] = tile.clone();
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
				if (i > 20 && i == j+14) {
					t.setHammerState(hammerLookup.get(HammerShape.HAMMER_SHAPE_TRIANGLE_UL));
				}
				else if (i + j == 10) {
					t.setHammerState(hammerLookup.get(HammerShape.HAMMER_SHAPE_TRIANGLE_BL));
				}
				
				t.init(new Vector2f(i*tileSize, j*tileSize), new Vector2f(tileSize, tileSize));
			}
		}
		
		
		currmap = new Map(mapData, null, null, null);//TODO
	}

	private void loadProgression() {

	}

	private void loadState() {

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
		player = new Player(0, new Vector2f(100, 100), null, renderer, "Player", null);
		
		
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
		while (!glfwWindowShouldClose(Drawer.window)) {
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
			glfwSwapBuffers(Drawer.window);

			// Event listening stuff. Key callback is invoked here.
			// Do wipe input before going in
			Input.update();
			glfwPollEvents();
			
			//Frame walking debug tools
			if (frameWalk) {
				while(waitingForFrameWalk) {
					//Input.update(); //No need to wipe stuff
					glfwPollEvents();
					
					if (glfwWindowShouldClose(Drawer.window)) break;
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
		//Clear tile collision colorings (debug purposes)
		if (GameManager.showCollisions) {
			for (Tile[] tArr : currmap.getGrid()) {
				for (Tile t : tArr) if (t != null) t.renderer.col = new Color(0.5f, 0.5f, 0.5f);
			}
		}
		
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
