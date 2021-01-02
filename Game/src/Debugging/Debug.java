package Debugging;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import GameController.Map;
import GameController.World;
import Graphics.Drawer;
import Graphics.Elements.Texture;
import Graphics.Rendering.ColorShader;
import Graphics.Rendering.Shader;
import Tiles.Tile;
import Utility.Transformation;
import Utility.Vector;
import Wrappers.Color;

public class Debug {

	private static ArrayList<DebugElement> debugElements;
	public static Shader debugShader;
	public static Texture debugTex;
	public static Transformation trans;

	public static boolean drawEdges;
	public static boolean logIssues;

	public static float timeScale = 1;
	public static boolean frameWalk = false;
	public static float frameDelta = 10f;
	public static boolean waitingForFrameWalk = true; // Helper var for frame walking

	public static boolean showCollisions = false;
	public static boolean debugElementsEnabled = false;

	public static void config() {
		timeScale = 1f;
		frameWalk = false;
		frameDelta = 20f;

		showCollisions = false;
		debugElementsEnabled = true;
		drawEdges = false;
		logIssues = true;

		Drawer.windowResizeable = false;
	}

	public static void init() {
		debugElements = new ArrayList<DebugElement>();

		debugShader = new ColorShader("shader");
		debugTex = Texture.getTex("assets/Sprites/debugTex.png");

		trans = new Transformation(new Vector2f(0, 0), Transformation.MATRIX_MODE_WORLD);

		TestSpace.test();
	}

	public static void renderDebug() {
		// Render vectors
		ArrayList<DebugElement> clearList = new ArrayList<DebugElement>();
		for (DebugElement e : debugElements) {
			if (debugElementsEnabled)
				e.render(debugShader);
			e.lifespan--;
			if (e.lifespan <= 0)
				clearList.add(e);
		}

		// Clear cache
		for (DebugElement e : clearList) {
			debugElements.remove(e);
		}

		// Draw some coll debug
		if (drawEdges) {
			Tile[][] ts = World.currmap.grids.get(GameManager.Grid.COLL.name);
			for (Tile[] tarr : ts)
				for (Tile t : tarr) {
					if (t == null)
						continue;
					for (Map.CompEdgeSegment e : t.edgeSegs) {

						Vector2f dir = new Vector2f(e.v2).sub(e.v1.x, e.v1.y);
						Debug.enqueueElement(new DebugVector(new Vector2f(e.v1).mul(GameManager.tileSize), dir,
								GameManager.tileSize, 1));

						// Draw normals too!
						Vector2f nOrigin = Vector.lerp(new Vector2f(e.v1), new Vector2f(e.v2), 0.5f)
								.mul(GameManager.tileSize);
						Debug.enqueueElement(
								new DebugVector(nOrigin, e.normal, GameManager.tileSize, new Color(1, 1, 0, 1), 1));
					}
				}
		}
	}

	// Some unique behavior here so I'll insulate the process a bit
	public static void trackMovementVector(Vector2f p, Vector2f v, float mult) {
		enqueueElement(new DebugVector(p, new Vector2f(v), mult));
	}

	public static void enqueueElement(DebugElement e) {
		debugElements.add(e);
	}

	/**
	 * Really shouldn't need this method
	 * 
	 * @param pos
	 * @param dims
	 * @param col
	 */
	public static void highlightRect(Vector2f pos, Vector2f dims, Color col, int lifespan) {
		DebugBox box = new DebugBox(pos, dims, col, lifespan); // Just use a debug box for now
		enqueueElement(box);
	}

	public static void highlightRect(Vector2f pos, Vector2f dims, Color col) {
		highlightRect(pos, dims, col, 1);
	}

	public static void poll() {
		int err;
		if ((err = glGetError()) != GL_NO_ERROR) {
			System.err.println(err);
			new Exception("OpenGL ERROR").printStackTrace();
			System.exit(1);
		}
	}

	public static void clearElements() {
		debugElements.clear();
	}
}
