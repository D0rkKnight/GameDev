package Debug;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import Rendering.ColorShader;
import Rendering.Shader;
import Rendering.SpriteRenderer;
import Rendering.Texture;
import Rendering.Transformation;
import Wrappers.Color;

public class Debug {
	
	private static ArrayList<DebugElement> debugElements;
	private static ArrayList<SpriteRenderer> highlightedRenderers;
	private static Shader debugShader;
	
	public static Texture debugTex;
	
	public static Transformation trans;
	
	private static void config() {
		GameManager.timeScale = 1f;
		GameManager.frameWalk = false;
		GameManager.frameDelta = 20f;
		
		GameManager.showCollisions = true;
		GameManager.debugElementsEnabled = false;
	}
	
	public static void init() {
		config();
		
		debugElements = new ArrayList<DebugElement>();
		highlightedRenderers = new ArrayList<SpriteRenderer>();
		
		debugShader = new ColorShader("shader");
		debugTex = new Texture("assets/debugTex.png");
		
		trans = new Transformation(new Vector2f(0, 0));
	}
	
	public static void renderDebug() {
		//Render vectors
		ArrayList<DebugElement> clearList = new ArrayList<DebugElement>();
		for (DebugElement e : debugElements) {
			if (GameManager.debugElementsEnabled) e.render(debugShader);
			e.lifespan --;
			
			if (e.lifespan <= 0) clearList.add(e);
		}
		
		//Clear cache
		for (DebugElement e : clearList) {
			debugElements.remove(e);
		}
	}
	
	public static void clearHighlights() {
		for (SpriteRenderer r : highlightedRenderers) r.col = new Color(1, 1, 1);
		
		highlightedRenderers.clear();
	}
	
	//Some unique behavior here so I'll insulate the process a bit
	public static void trackMovementVector(Vector2f p, Vector2f v, float mult) {
		enqueueElement(new DebugVector(p, new Vector2f(v), mult));
	}
	
	public static void enqueueElement(DebugElement e) {
		debugElements.add(e);
	}
	
	public static void highlightRenderer(SpriteRenderer rend, Color col) {
		rend.col = col;
		highlightedRenderers.add(rend);
	}
}
