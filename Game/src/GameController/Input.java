package GameController;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

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
	public static boolean meleeAction;
	
	public static boolean dashAction; //Only active for one frame
	public static boolean knockbackTest;
	public static Vector2f knockbackVectorTest = new Vector2f(-2f, 0.5f);
	
	public static Vector2f mouseScreenPos;
	public static Vector2f mouseWorldPos;
	public static Vector2f windowDims;

	public static int frameWalkKey = GLFW_KEY_J;
	
	static {
		keyStates = new boolean[GLFW_KEY_LAST];
		mouseStates = new boolean[GLFW_MOUSE_BUTTON_LAST];
		mouseScreenPos = new Vector2f(0, 0);
	}
	
	public static void initInput() {
		// Setup key callbacks (includes a lambda, fun.)
		// We can pass in a delegate to handle controls.
		glfwSetKeyCallback(Drawer.window, (window, key, scancode, action, mods) -> {
			Input.updateKeys(window, key, scancode, action, mods);
		});
		
		//I dunno why this has to be different for mouse inputs...
		glfwSetMouseButtonCallback(Drawer.window, new GLFWMouseButtonCallback() {
		    @Override
		    public void invoke(final long window, final int button, final int action, final int mods) {
		    	Input.updateMouse(window, button, action, mods);
		    }
	    });
		
		glfwSetCursorPosCallback(Drawer.window, new GLFWCursorPosCallback() {

			@Override
			public void invoke(long window, double xPos, double yPos) {
				Input.updateCursor(xPos, yPos);
			}
			
		});
		
		glfwSetWindowSizeCallback(Drawer.window, new GLFWWindowSizeCallback() {

			@Override
			public void invoke(long window, int width, int height) {
				Input.updateWindowSize(width, height);
			}
			
		});
	}
	
	//Called once per frame
	public static void update() {
		dashAction = false;
		knockbackTest = false;
		meleeAction = false;
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
				break;
		    case GLFW_KEY_K:
		    	knockbackVectorTest = new Vector2f(-2f, 0.5f);
		    	knockbackTest = true;
		    	break;
		    case GLFW_KEY_L:
		    	knockbackVectorTest = new Vector2f(2f, 0.5f);
		    	knockbackTest = true;
		    	break;
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
		
		if (action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_RIGHT) {
			meleeAction = true;
			System.out.println("melee");
		}
	}

	
	//TODO: I'm fairly certain the input on these isn't x and y. Look into it tommorow.
	public static void updateCursor(double xPos, double yPos) {
		mouseScreenPos = new Vector2f((float) xPos, (float) yPos);
		
		//Flip to the right corner
		Vector2f quad1ScreenPos = new Vector2f(mouseScreenPos.x, windowDims.y - mouseScreenPos.y);
		
		//Shift so that it is anchored properly
		Vector2f offset = new Vector2f(windowDims).mul(0.5f);
		mouseWorldPos = new Vector2f(Camera.main.pos).add(quad1ScreenPos).sub(offset);
	}
	
	public static void updateWindowSize(int width, int height) {
		windowDims = new Vector2f(width, height);
	}
}
