package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.Physics;
import Collision.HammerShapes.HammerRightTriangle;
import Collision.HammerShapes.HammerShape;
import Collision.HammerShapes.HammerSquare;
import Debugging.Debug;
import Entities.Player;
import Entities.Framework.Entity;
import Entities.Framework.PhysicsEntity;
import Graphics.Elements.Texture;
import Graphics.Rendering.Drawer;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import UI.UI;

public class GameManager {

	// The frame and canvas
	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	private static long startTime;

	// Helper variable for frame walking
	public static boolean waitingForFrameWalk = true;

	/**
	 * Configuration and debug
	 */
	public static float timeScale = 1;
	public static boolean frameWalk = false;
	public static float frameDelta = 10f;
	public static boolean showCollisions = false;
	public static boolean debugElementsEnabled = false;

	// Rendering stuff
	public static GeneralRenderer renderer;
	public static SpriteShader shader;
	public static Texture[] tileSpritesheet;

	// Storage for tiles
//	private ArrayList<Map> maps;
	public static Map currmap;

	// Lookup table for different kinds of tiles
	private HashMap<String, HashMap<Integer, Tile>> tileLookup;
	// Lookup table for different kinds of accessories
//	private HashMap<Integer, Accessory> accessoryLookup;
	// Lookup table for hammershapes
	public static HashMap<Integer, HammerShape> hammerLookup;
	private HashMap<Integer, Entity> entityHash;

	// current progression of player ingame
//	private int chapter; // chapter, determines plot events
//	private int[] levelnums; // number of levels within each chapter - down to map design
//	private int level; // level within each chapter (represents biomes
//	private int room; // specific room within each level

	// Entity positions in current room
	static private ArrayList<Entity> entities;
	static private ArrayList<Entity> entityWaitingList;
	static private ArrayList<Entity> entityClearList;
	static private ArrayList<Hitbox> coll;

	public static Player player;

	public static final int tileSize = 8;
	public static final int tileSpriteSize = 4;

	public static final String GRID_SET = "set";
	public static final String GRID_COLL = "ground";
	public static final String GRID_GROUND = "ground";

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
		Drawer.initGraphics();
		Input.initInput();
		Debug.init();

		// Init camera
		new Camera();

		// Init renderer
		// TODO: We have to make a renderer factory in order for this to, like, work.

		shader = new SpriteShader("texShader");
		renderer = new GeneralRenderer(shader);

		initCollision();
		initTiles();
		initEntityHash("assets/Hashfiles/", "EntitiesTest.txt");
		initMap("assets/Maps/", "test.tmx");// also should initialize entities

		UI.init();
		initTime();
	}

//	private void loadProgression() {
//
//	}
//
//	private void loadState() {
//
//	}

	/*
	 * Loads and constructs tiles based off of external file, then logs in
	 * tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();

		initTileSet("assets/Maps/Tilesets/", "ground.tsx", tileLookup);
		initTileSet("assets/Maps/Tilesets/", "set.tsx", tileLookup);
		initTileSet("assets/Maps/Tilesets/", "coll.tsx", tileLookup);
	}

	private void initTileSet(String fileDir, String fileName, HashMap<String, HashMap<Integer, Tile>> masterTSet) {
		try {
			HashMap<Integer, Tile> tSet = new HashMap<>();
			Serializer.loadTileHash(fileDir, fileName, tSet, hammerLookup, renderer);
			masterTSet.put(fileName, tSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initMap(String fileDir, String fileName) {
		Document mapFile = null;
		try {
			mapFile = Serializer.readDoc(fileDir, fileName);
		} catch (Exception e) {
			System.err.println("File not found");
			e.printStackTrace();
		}
		HashMap<String, Tile[][]> mapData = null;
		try {
			mapData = Serializer.loadTileGrids(mapFile, tileLookup);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("map error");
			e.printStackTrace();
		}

		currmap = new Map(mapData, null, null, null);// TODO
		initEntities(mapFile);

		Drawer.initTileChunks(currmap.grids.get("ground"));
	}

//	private void finishArea() { // called when character finishes a major area, updates level and chapter of
//								// character
//
//	}

	private void initEntityHash(String fileDir, String fileName) {
		try {
			entityHash = Serializer.loadEntityHash(fileDir, fileName, renderer);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initEntities(Document mapFile) {
		entities = new ArrayList<>();
		entityWaitingList = new ArrayList<>();
		entityClearList = new ArrayList<>();

		ArrayList<Entity> entitytemp = Serializer.loadEntities(mapFile, entityHash, tileSize);
		for (Entity e : entitytemp) {
			subscribeEntity(e);
		}
		player = (Player) entityWaitingList.get(0);
		initPlayer();

	}

	private void initCollision() {
		coll = new ArrayList<>();

		// Generate hammer shapes
		hammerLookup = new HashMap<>();
		ArrayList<HammerShape> cache = new ArrayList<>();
		cache.add(new HammerSquare());

		for (int i = HammerShape.HAMMER_SHAPE_TRIANGLE_BL; i <= HammerShape.HAMMER_SHAPE_TRIANGLE_UR; i++)
			cache.add(new HammerRightTriangle(i));

		for (HammerShape h : cache)
			hammerLookup.put(h.shapeId, h);
	}

	public static void subscribeEntity(Entity e) {
		entityWaitingList.add(e);

		if (e instanceof Collidable) {
			Hitbox hb = ((Collidable) e).getHb();
			if (hb != null)
				coll.add(hb);
			else {
				new Exception("Collider of " + e.name + " not defined!").printStackTrace();
			}
		}
	}

	public static void unsubscribeEntity(Entity e) {
		entityClearList.add(e);

		if (e instanceof Collidable) {
			Hitbox hb = ((Collidable) e).getHb();
			if (hb != null)
				coll.remove(hb);
			else {
				new Exception("Collider of " + e.name + " not defined!").printStackTrace();
			}
		}
	}

	private void initPlayer() {
		Camera.main.attach(player);
	}

	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {
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

			// Frame walking debug tools
			if (frameWalk) {
				while (waitingForFrameWalk) {
					// Input.update(); //No need to wipe stuff
					glfwPollEvents();

					if (glfwWindowShouldClose(Drawer.window))
						break;
				}
				waitingForFrameWalk = true;
			}
		}
	}

	private void initTime() {
		currTime = System.nanoTime() / 1000000;
		lastTime = currTime;

		startTime = currTime;
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

	public static long timeSinceStart() {
		return (currTime - startTime);
	}

	/**
	 * Called once per frame, and is responsible for updating internal game logic.
	 */
	private void update() {

		// Dump entity waiting list into entity list
		for (Entity e : entityWaitingList) {
			entities.add(e);
		}
		entityWaitingList.clear();

		// Filter deleted entities out
		for (Entity e : entityClearList) {
			entities.remove(e);
		}

		// Each entity makes decisions
		for (Entity ent : entities) {
			ent.calculate();
			ent.updateChildren();
		}

		// Physics simulation step begin from here
		// ________________________________________________________

		// Push in collision deltas
		Tile[][] grid = currmap.grids.get(GRID_COLL);

		for (int i = 0; i < coll.size(); i++) {
			Hitbox c = coll.get(i);

			// Collide against other hitboxes
			for (int j = i + 1; j < coll.size(); j++) {
				if (j == coll.size())
					break;

				Hitbox otherC = coll.get(j);

				Physics.checkEntityCollision(c, otherC);
			}

			// Figure out movement (but only if it's a physics entity)
			Object e = c.owner;
			if (c.owner instanceof PhysicsEntity) {
				Physics.calculateDeltas((PhysicsEntity) e, grid);
			}
		}

		// Push physics outcomes
		for (Entity e : entities) {
			// Somehow need to avoid pushing the movement of entities that don't move.
			if (e instanceof PhysicsEntity) {
				PhysicsEntity pe = (PhysicsEntity) e;
				if (pe.pData.collidedWithTile) {
					pe.onTileCollision();
					pe.pData.collidedWithTile = false;
				}
				pe.pushMovement();
			}
		}

		Camera.main.update();
	}

}
