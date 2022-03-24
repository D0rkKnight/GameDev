package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.util.ArrayList;
import java.util.HashMap;

import Collision.Collidable;
import Collision.Collider;
import Collision.Physics;
import Debugging.Debug;
import Debugging.DebugPolygon;
import Debugging.TestSpace;
import Entities.InteractableFlag;
import Entities.Framework.Entity;
import Entities.Framework.EntityFlag;
import Entities.Framework.Interactive;
import Entities.Framework.PhysicsEntity;
import Entities.PlayerPackage.Player;
import GameController.procedural.WorldGenerator;
import Graphics.Drawer;
import Tiles.Tile;
import UI.UI;
import Utility.Timers.Timer;
import Wrappers.Color;
import audio.Audio;

public class GameManager {

	// Lookup table for different kinds of tiles
	static HashMap<String, HashMap<Integer, Tile>> tileLookup;
	// Lookup table for different kinds of accessories
	//	private HashMap<Integer, Accessory> accessoryLookup;
	// static HashMap<String, Entity> entityHash;

	// Entity positions in current room
	static ArrayList<Entity> entities;
	static ArrayList<Entity> entityWaitingList;
	static private ArrayList<Entity> entityClearList;
	static private ArrayList<Collider> coll;

	public static Player player;

	// If room is changing (all entities continue to move/freeze in place)
	public static boolean roomChanging = false;
	public static Timer switchTimer;

	public static final int tileSize = 16;
	public static final int tileSpriteSize = 8;

	private static final EntityFlag.FlagFactory iFlagFactory = (pos) -> {
		return new InteractableFlag(pos);
	};

	public static final long COMBAT_FPS = 60;

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
//		initEntityHash("assets/Hashfiles/", "EntitiesTest.txt");
		Serializer.loadTemplates("assets/Maps/Templates");

		WorldGenerator.init();
		World.init(); // also should initialize entities
		initPlayer();

		UI.init();
		Time.initTime();

		ArenaController.init();
		Physics.init();

		TestSpace.init();

		// Perform some things on load
		updateEntityList();
		for (Entity e : entities) {
			e.onGameLoad();
		}
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

//	private void initEntityHash(String fileDir, String fileName) {
//		try {
//			entityHash = Serializer.loadEntityHash(fileDir, fileName);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

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
			Collidable ec = (Collidable) e;
			for (Collider c : ec.getColl())
				coll.add(c);
		}
	}

	public static void unsubscribeEntity(Entity e) {
		e.unsubSelf(entityClearList, coll);
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
		Audio.update();

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
			UI.changeCanvas(UI.CEnum.RUNNING);
			Time.endPause();
			break;
		case PAUSED:
			UI.changeCanvas(UI.CEnum.PAUSED);
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
			if (ent.parent == null) {
				ent.calculate();

				// TODO: Move this elsewhere
				if (ent instanceof Interactive) {
					float activationDistance = 100f;
					float distToInteractable = player.getPosition().distance(ent.getPosition());

					if (distToInteractable <= activationDistance) {
						if (ent.flag == null)
							ent.flagEntity(iFlagFactory);

						if (Input.interactAction && !Input.interactEaten) {
							((Interactive) ent).interact(player);
						}
					} else {
						if (ent.flag != null)
							ent.deflagEntity();
					}
				}

				ent.updateChildren();
			}
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
			Collider c = coll.get(i);

			// Draw if debug enabled
			if (Debug.showHitboxes) {
				DebugPolygon poly = new DebugPolygon(c.genWorldVerts(), 1, new Color(1, 1, 1, 1));
				Debug.enqueueElement(poly);
			}

			// Collide against other hitboxes
			for (int j = i + 1; j < coll.size(); j++) {
				if (j == coll.size())
					break;

				Collider otherC = coll.get(j);

				if (!c.isActive || !otherC.isActive)
					continue; // Skip if inactive

				Physics.checkEntityCollision(c, otherC);
			}

			// Figure out movement (but only if it's a physics entity)
			Object e = c.owner;
			if (e instanceof PhysicsEntity && ((PhysicsEntity) e).hasCollision) {
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

		ArenaController.update();
		Camera.main.update();

		if (switchTimer != null) {
			switchTimer.update();
		}

		if (!TestSpace.ffExecuted) {
			TestSpace.firstFrame();
			TestSpace.ffExecuted = true;
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
