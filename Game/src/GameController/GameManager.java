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

import Collision.Collidable;
import Collision.Hitbox;
import Collision.Physics;
import Debugging.Debug;
import Entities.Player;
import Entities.Framework.Entity;
import Entities.Framework.PhysicsEntity;
import Graphics.Drawer;
import Tiles.Tile;
import UI.UI;
import Utility.Timers.Timer;

public class GameManager {

	// Lookup table for different kinds of tiles
	static HashMap<String, HashMap<Integer, Tile>> tileLookup;
	// Lookup table for different kinds of accessories
//	private HashMap<Integer, Accessory> accessoryLookup;
	static HashMap<String, Entity> entityHash;

	// Entity positions in current room
	static ArrayList<Entity> entities;
	static ArrayList<Entity> entityWaitingList;
	static private ArrayList<Entity> entityClearList;
	static private ArrayList<Hitbox> coll;

	public static Player player;

	// If room is changing (all entities continue to move/freeze in place)
	public static boolean roomChanging = false;
	public static Timer switchTimer;

	public static final int tileSize = 16;
	public static final int tileSpriteSize = 8;

	// TODO: Write error checks for these
	public static enum Grid {
		// Used to map between Tiled2D and the internal hashmap tracking data.
		SET("ground"), COLL("collision"), GROUND("ground"), BG("background"), FG("foreground");

		public final String name;

		Grid(String name) {
			this.name = name;
		}
	}

	private static GameState gameState;

	public static enum GameState {
		RUNNING, PAUSED
	}

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
		gameState = GameState.RUNNING;

		Debug.config();

		coll = new ArrayList<>();
		Drawer.initGraphics();
		Audio.init();

		Input.initInput();
		Debug.init();

		// Init camera
		new Camera();

		entities = new ArrayList<>();
		entityWaitingList = new ArrayList<>();
		entityClearList = new ArrayList<>();

		initTiles();
		initEntityHash("assets/Hashfiles/", "EntitiesTest.txt");

		World.init(); // also should initialize entities
		initPlayer();

		UI.init();
		Time.initTime();
	}

	/*
	 * Loads and constructs tiles based off of external file, then logs in
	 * tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();

		initTileSet("assets/Maps/Tilesets/Forest/", "forestTSet.tsx", tileLookup);
		initTileSet("assets/Maps/Tilesets/", "coll8x8.tsx", tileLookup);
	}

	private void initTileSet(String fileDir, String fileName, HashMap<String, HashMap<Integer, Tile>> masterTSet) {
		try {
			HashMap<Integer, Tile> tSet = new HashMap<>();
			Serializer.loadTileHash(fileDir, fileName, tSet);
			masterTSet.put(fileName, tSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initEntityHash(String fileDir, String fileName) {
		try {
			entityHash = Serializer.loadEntityHash(fileDir, fileName);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void initPlayer() {
		Camera.main.attach(player);
	}

	// Simply subscribes an array of entities.
	public static void loadEntities(ArrayList<Entity> eArr) {
		for (Entity e : eArr) {
			GameManager.subscribeEntity(e);
		}
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

	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {

		// Into the rendering loop we go
		// Remember the lambda callback we attached to key presses? This is where the
		// function returns.

		while (!glfwWindowShouldClose(Drawer.window)) {
			Time.updateTime();

			// Event listening stuff. Key callback is invoked here.
			// Do wipe input before going in
			Input.update();
			glfwPollEvents();

			switch (gameState) {
			case RUNNING:
				GS_running();
				break;
			case PAUSED:
				GS_paused();
				break;
			}
		}
	}

	public void GS_running() {
		// Drawing stuff
		update();
		Drawer.draw(World.currmap, entities);

		// Frame walking debug tools
		if (Debug.frameWalk) {
			while (Debug.waitingForFrameWalk) {
				// Input.update(); //No need to wipe stuff
				glfwPollEvents();

				if (glfwWindowShouldClose(Drawer.window))
					break;
			}
			Debug.waitingForFrameWalk = true;
		}
	}

	private void GS_paused() {
		// Continue drawing
		Drawer.draw(World.currmap, entities);
	}

	public static GameState getGameState() {
		return gameState;
	}

	public static void setGameState(GameState gameState) {
		GameManager.gameState = gameState;

		switch (gameState) {
		case RUNNING:
			UI.changeCanvas(UI.CanvasEnum.RUNNING);
			Time.endPause();
			break;
		case PAUSED:
			UI.changeCanvas(UI.CanvasEnum.PAUSED);
			Time.beginPause();
			break;
		}
	}

	/**
	 * Called once per frame, and is responsible for updating internal game logic.
	 */
	private void update() {

		updateEntityList();

		// Each entity makes decisions
		for (Entity ent : entities) {
			ent.calculate();
			ent.updateChildren();
		}

		// Each entity generates its frame
		for (Entity ent : entities) {
			ent.calcFrame();
		}

		// Physics simulation step begin from here
		// ________________________________________________________

		// Push in collision deltas
		Tile[][] grid = World.currmap.grids.get(Grid.COLL.name);

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

		if (switchTimer != null) {
			switchTimer.update();
		}
	}

	public static void updateEntityList() {
		// Dump entity waiting list into entity list
		for (Entity e : entityWaitingList) {
			entities.add(e);
		}
		entityWaitingList.clear();

		// Filter deleted entities out
		for (Entity e : entityClearList) {
			entities.remove(e);
		}
		entityClearList.clear();
	}
}
