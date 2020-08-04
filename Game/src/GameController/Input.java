package GameController;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import Wrappers.Rect;
import Wrappers.Vector2;

/*
 * Input is a helper class to player and serves as a buffer for GM to write to and Player to read from.
 */
public class Input {
	public static boolean[] keyStates;
	public static boolean[] mouseStates;
	
	
	public static float moveX;
	public static float moveY;
	
	public static boolean primaryButtonDown;
	public static boolean secondaryButtonDown;
	public static boolean dashAction; //Only active for one frame
	
	public static Vector2 mouseScreenPos;
	public static Vector2 mouseWorldPos;
	public static Vector2 windowDims;

	public static int frameWalkKey = GLFW_KEY_J;
	
	static {
		keyStates = new boolean[GLFW_KEY_LAST];
		mouseStates = new boolean[GLFW_MOUSE_BUTTON_LAST];
		mouseScreenPos = new Vector2(0, 0);
	}
	
	//Called once per frame
	public static void update() {
		dashAction = false;
	}
	
	public static void updateKeys(long window, int key, int scancode, int action, int mods) {
		//Record key states here
		if (action == GLFW_PRESS) keyStates[key] = true;
		if (action == GLFW_RELEASE) keyStates[key] = false;
		
		//Individual press and release stuff
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(window, true); // Later detected in rendering loop
		}
		//Frame walking
		if (GameManager.frameWalk && key == frameWalkKey && action == GLFW_PRESS) {
			GameManager.waitingForFrameWalk = false;
		}
		
		//One frame listen
		//TODO: Buffer these for frame walking.
		if (action == GLFW_PRESS) {
			switch (key) {
			case GLFW_KEY_LEFT_SHIFT:
				dashAction = true;
			}
		}
		
		//Player movement!
		moveX = 0;
		if (keyStates[GLFW_KEY_D]) moveX ++;
		if (keyStates[GLFW_KEY_A]) moveX --;
		
		moveY = 0;
		if (keyStates[GLFW_KEY_W]) moveY ++;
		if (keyStates[GLFW_KEY_S]) moveY --;
	}
	
	public static void updateMouse(long window, int button, int action, int mods) {
		if (action == GLFW_PRESS) mouseStates[button] = true;
		if (action == GLFW_RELEASE) mouseStates[button] = false;
		
		primaryButtonDown = mouseStates[GLFW_MOUSE_BUTTON_LEFT];
		secondaryButtonDown = mouseStates[GLFW_MOUSE_BUTTON_RIGHT];
	}

	
	//TODO: I'm fairly certain the input on these isn't x and y. Look into it tommorow.
	public static void updateCursor(double xPos, double yPos) {
		mouseScreenPos = new Vector2((float) xPos, (float) yPos);
		
		//Flip to the right corner
		Vector2 quad1ScreenPos = new Vector2(mouseScreenPos.x, windowDims.y - mouseScreenPos.y);
		
		//Shift so that it is anchored properly
		Vector2 offset = windowDims.mult(0.5f);
		mouseWorldPos = Camera.main.pos.add(quad1ScreenPos).sub(offset);
	}
	
	public static void updateWindowSize(int width, int height) {
		windowDims = new Vector2(width, height);
	}
}
