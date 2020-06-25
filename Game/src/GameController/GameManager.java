package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Shaders.Shader;
import Tiles.SquareTile;
import Tiles.Tile;

public class GameManager {
	
	//The frame and canvas
	/*private JFrame frame;
	private RendererOld canvas;*/
	private long window;
	//
	private Renderer renderer;
	
	//Storage for tiles
	private ArrayList<Map> maps;
	private Map map;
	
	//Lookup table for different kinds of tiles
	private HashMap<Integer, Tile> tileLookup;
	
	/*
	 * Creates components before entering loop
	 */
	GameManager() {
		//Initialization
		init();
		
		//Setting up renderer
		/*frame = new JFrame();
		
		canvas = new RendererOld(map);
		canvas.setSize(1280, 720);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);*/
		
		loop();
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void init() {
		initGraphics();
		renderer = new Renderer();
		
		initTiles();
		map = loadMap();
	}
	
	private void initGraphics() {
		//Error callback
		GLFWErrorCallback.createPrint(System.err).set();
		
		//Initialize glfw, pretty important. Anything glfw stuff that happens before this will break.
		if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
		
		//Configure GLFW
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); //Hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);//Window will be resizeable
		
		//Create the window!
		//Note: NULL is a constant that denotes the null value in OpenGL. Not the same thing as Java null.
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create GLFW window");
		}
		
		//Setup key callbacks (includes a lambda, fun.)
		//We can pass in a delegate to handle controls.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true); //Later detected in rendering loop
			}
		});
		
		//A wack process required to move the window. Why this is necessary, I'm not entirely clear on.
		//Oh I think it's because the wrapped OpenGL function has to return multiple values so
		//this allocates the memory in a manner that C recognizes.
		//Read more on MemoryStack as a solution to interfacing problems between the two languages.
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			//Get window size
			glfwGetWindowSize(window, pWidth, pHeight);
			
			//Get resolution of primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			//Set pos
			glfwSetWindowPos(window, 
					(vidmode.width() - pWidth.get(0))/2,
					(vidmode.height() - pHeight.get(0))/2);
		}
		//Another benefit: garbage collection bsery is avoided because the stack is 
		//					popped and reclaimed immediately after the try block.
		
		//Tells the GPU to write to this window.
		glfwMakeContextCurrent(window);
		
		//V-SYNC!!!
		glfwSwapInterval(1);
		
		//Make the window visible
		glfwShowWindow(window);
		
		//Creating the context to which all graphics operations will be executed upon
		GL.createCapabilities();
	}
	
	/*
	 * Loads and constructs tiles based off of external file, then logs in tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();
		
		Shader redShader = new Shader("shader");
		
		BufferedImage img = loadImage("tile1.png");
		SquareTile t1 = new SquareTile(1, img, redShader);
		
		tileLookup.put(1, t1);
	}
	
	/*
	 * Load a map from an external file.
	 * Right now using placeholder
	 */
	private Map loadMap() {
		Tile[][] mapData = new Tile[10][10];
		mapData[0][0] = tileLookup.get(1);
		mapData[0][5] = tileLookup.get(1);
		mapData[5][3] = tileLookup.get(1);
		
		Map m = new Map(mapData);
		return m;
	}
	
	/*
	 * Wrapper function for loading an image
	 */
	private BufferedImage loadImage(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return img;
	}
	
	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {
		
		//Set clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		
		//Into the rendering loop we go
		//Remember the lambda callback we attached to key presses? This is where the function returns.
		while(!glfwWindowShouldClose(window)) {
			//Clear frame buffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			//Drawing stuff
			/*map.getGrid()[0][0].shader.bind();
			glBegin(GL_QUADS);
				glVertex2f(-0.5f, 0.5f);
				glVertex2f(0.5f, 0.5f);
				glVertex2f(0.5f, -0.5f);
				glVertex2f(-0.5f, -0.5f);
			glEnd();*/
			renderer.draw(map);
			
			
			//tldr: there are two buffers, one that is being displayed and one that we are writing to.
			//This function waits until one buffer is written to before writing the next one.
			//This is because of v-sync.
			glfwSwapBuffers(window);
			
			//Event listening stuff. Key callback is invoked here.
			glfwPollEvents();
			
			//canvas.paint();
		}
	}
}
