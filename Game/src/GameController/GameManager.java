package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;
import org.w3c.dom.Document;

import Accessories.Accessory;
import Collision.HammerRightTriangle;
import Collision.HammerShape;
import Collision.HammerSquare;
import Collision.Physics;
import Debug.Debug;
import Entities.Entity;
import Entities.FloaterEnemy;
import Entities.PhysicsEntity;
import Entities.Player;
import Entities.ShardSlimeEnemy;
import Rendering.SpriteRenderer;
import Rendering.SpriteShader;
import Rendering.Texture;
import Tiles.Tile;
import UI.UI;
import Wrappers.Hitbox;
import Wrappers.Stats;


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
	
	//Rendering stuff
	public static SpriteRenderer renderer;
	public static SpriteShader shader;
	public static Texture[] tileSpritesheet;

	// Storage for tiles
	private ArrayList<Map> maps;
	public static Map currmap;

	// Lookup table for different kinds of tiles
	private HashMap<Integer, Tile> tileLookup;
	// Lookup table for different kinds of accessories
	private HashMap<Integer, Accessory> accessoryLookup;
	// Lookup table for hammershapes
	public static HashMap<Integer, HammerShape> hammerLookup;
	private HashMap<Integer, Entity> entityHash;

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
		renderer = sprRenderer;
		
		initCollision();
		
		initTiles("tset1.tsx");
		
		Document doc = null;
		try {
			doc = Serializer.readDoc("assets/TestMap64.tmx");
		} catch (Exception e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		initMap(doc);//also should initialize entities
		
		UI.init();
		initTime();
	}

	private void loadProgression() {

	}

	private void loadState() {

	}
	
	

	/*
	 * Loads and constructs tiles based off of external file, then logs in
	 * tileLookup
	 */
	private void initTiles(String tileFile) {
		tileLookup = new HashMap<>();
		try {
			//assets/TestTiles.tsx
			//TODO add the tileset that works
			Serializer.loadTileHash("assets/" + tileFile, tileLookup, hammerLookup, renderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initMap(Document mapFile) {
		Tile[][] mapData = null;
		try {
			//assets/TestMap64.tmx
			mapData = Serializer.loadTileGrid(mapFile, tileLookup);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("map error");
			e.printStackTrace();
		}
		
		
		//Tiles are currently raw and unitialized. Initialize them.
		for (int i=0; i<mapData.length; i++) for (int j=0; j<mapData[0].length; j++) {
			Tile t = mapData[i][j];
			if (t != null) {
				t.init(new Vector2f(i*tileSize, j*tileSize), new Vector2f(tileSize, tileSize));
			}
		}
		
		currmap = new Map(mapData, null, null, null);//TODO
		initEntities(mapFile);
		int xTiles = 5;
		int yTiles = 90;
		
		
		
		//Hardcoding some enemy spawns
	}

	private void finishArea() { // called when character finishes a major area, updates level and chapter of
								// character

	}
	
	private void initEntities(Document mapFile) {
		//TODO this is hardcoded, make a initEntityHash
		entityHash = new HashMap<Integer, Entity>();
		entityHash.put(0, new Player(0, new Vector2f(0f, 0f), renderer, "Player", new Stats(100, 100, 0.5f, 1f)));
		entityHash.put(1, new FloaterEnemy(10, new Vector2f(0, 0), renderer, "Enemy", new Stats(100, 100, 0, 0)));
		entityHash.put(2, new ShardSlimeEnemy(10, new Vector2f(0, 0), renderer, "BEnemy", new Stats(100, 100, 0, 0)));
		
		
		entities = new ArrayList();
		entityWaitingList = new ArrayList();
		entityClearList = new ArrayList();
		
		ArrayList<Entity> entitytemp = Serializer.loadEntities(mapFile, entityHash);
		for(Entity e : entitytemp) {
			subscribeEntity(e);
		}
		player = (Player) entityWaitingList.get(0);
		initPlayer();
		
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
		//if(entities.get(0) == null) {
		//	System.out.println("ERROR, PLAYER NOT SERIALIZED");
		//	player = new Player(0, new Vector2f(5 * 16, 90 * 16), renderer, "Player", new Stats());
		//}
		
		Camera.main.attach(player);
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

			// Drawing stuff
			update();
			Drawer.draw(currmap, entities);

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
		deltaTime = Math.max(1, deltaTime);
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
		if (GameManager.showCollisions) Debug.clearHighlights();
		
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
		for (int i=0; i<coll.size(); i++) {
			Hitbox c = coll.get(i);
			
			//Collide against other hitboxes
			for (int j=i+1; j<coll.size(); j++) {
				if (j==coll.size()) break;
				
				Hitbox otherC = coll.get(j);
				
				Physics.checkEntityCollision(c, otherC);
			}
			
			//Figure out movement
			Physics.calculateDeltas(c, grid);
		}
		
		//Push physics outcomes
		for (Entity e : entities) {
			//Somehow need to avoid pushing the movement of entities that don't move.
			PhysicsEntity pe = (PhysicsEntity) e;
			if (pe.pData.collidedWithTile) {
				pe.onTileCollision();
				pe.pData.collidedWithTile = false;
			}
			pe.pushMovement();
		}
		
		Camera.main.update();
	}

}
