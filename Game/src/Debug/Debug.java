package Debug;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import Rendering.ColorShader;
import Rendering.Shader;
import Rendering.Texture;

public class Debug {
	
	private static ArrayList<DebugElement> debugElements;
	private static Shader debugShader;
	
	public static Texture debugTex;
	
	private static void config() {
		GameManager.timeScale = 1f;
		GameManager.frameWalk = false;
		GameManager.frameDelta = 20f;
		
		GameManager.showCollisions = true;
		GameManager.debugElementsEnabled = true;
	}
	
	public static void init() {
		config();
		
		debugElements = new ArrayList<DebugElement>();
		debugShader = new ColorShader("shader");
		
		debugTex = new Texture("assets/debugTex.png");
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
	
	//Some unique behavior here so I'll insulate the process a bit
	public static void trackMovementVector(Vector2f p, Vector2f v, float mult) {
		enqueueElement(new DebugVector(p, new Vector2f(v), mult));
	}
	
	public static void enqueueElement(DebugElement e) {
		debugElements.add(e);
	}
}
