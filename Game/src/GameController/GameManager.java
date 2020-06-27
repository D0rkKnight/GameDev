package GameController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
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
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import Accessories.Accessory;
import Entities.Entity;
import Entities.Player;
import Rendering.RectRenderer;
import Rendering.Shader;
import Rendering.SpriteRenderer;
import Tiles.SquareTile;
import Tiles.Tile;
import Wrappers.Position;
import Wrappers.Texture;


public class GameManager {

	// The frame and canvas
	/*
	 * private JFrame frame; private RendererOld canvas;
	 */
	public static long window;
	private boolean[] keyStates;
	//
	private Drawer drawer;
	private RectRenderer renderer;

	// Storage for tiles
	private ArrayList<Map> maps;
	private Map currmap;

	// Lookup table for different kinds of tiles
	private HashMap<Integer, Tile> tileLookup;
	// Lookup table for different kinds of accessories
	private HashMap<Integer, Accessory> accessoryLookup;

	// current progression of player ingame
	private int chapter; // chapter, determines plot events
	private int[] levelnums; // number of levels within each chapter - down to map design
	private int level; // level within each chapter (represents biomes
	private int room; // specific room within each level

	// Entity positions in current room
	private ArrayList<Entity> entities;
	private Serializer serializer;
	private Player player;


	/*
	 * Creates components before entering loop
	 */
	GameManager() {
		// Initialization
		init();

		// Setting up renderer
		/*
		 * frame = new JFrame();
		 * 
		 * canvas = new RendererOld(map); canvas.setSize(1280, 720); frame.add(canvas);
		 * frame.pack(); frame.setVisible(true);
		 */

		loop();

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		serializer = new Serializer();
		keyStates = new boolean[GLFW_KEY_LAST];
		initGraphics();
		drawer = new Drawer();

		initTiles();
		
		/*
		try {
			serializer.loadMap("place holder file name", tileLookup); //TODO set up code to load each map that is needed in the level
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		//Just do this for now
		Tile[][] mapData = new Tile[10][10];
		Tile tile = tileLookup.get(1);
		mapData[2][0] = tile.clone();
		mapData[0][3] = tile.clone();
		mapData[2][9] = tile.clone();
		currmap = new Map(mapData, null, null);//TODO
		
		
		
		
		//Init player
		initEntities();
		initPlayer();
		
	}
	
	/*
	 * This is the LWJGL backed input solution.
	 */
	public void inputListener(long window, int key, int scancode, int action, int mods) {
		//Record key states here
		if (action == GLFW_PRESS) keyStates[key] = true;
		if (action == GLFW_RELEASE) keyStates[key] = false;
		
		//Individual press and release stuff
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(window, true); // Later detected in rendering loop
		}
		
		//Player movement!
		float moveX = 0;
		if (keyStates[GLFW_KEY_D]) moveX ++;
		if (keyStates[GLFW_KEY_A]) moveX --;
		player.input.moveX = moveX;
		
		float moveY = 0;
		if (keyStates[GLFW_KEY_W]) moveY ++;
		if (keyStates[GLFW_KEY_S]) moveY --;
		player.input.moveY = moveY;
	}

	private void loadProgression() {

	}

	private void loadState() {

	}

	private void initGraphics() {
		// Error callback
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize glfw, pretty important. Anything glfw stuff that happens before
		// this will break.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);// Window will be resizeable

		// Create the window!
		// Note: NULL is a constant that denotes the null value in OpenGL. Not the same
		// thing as Java null.
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create GLFW window");
		}

		// Setup key callbacks (includes a lambda, fun.)
		// We can pass in a delegate to handle controls.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			inputListener(window, key, scancode, action, mods);
		});

		// A wack process required to move the window. Why this is necessary, I'm not
		// entirely clear on.
		// Oh I think it's because the wrapped OpenGL function has to return multiple
		// values so
		// this allocates the memory in a manner that C recognizes.
		// Read more on MemoryStack as a solution to interfacing problems between the
		// two languages.
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			// Get window size
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get resolution of primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Set pos
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}
		// Another benefit: garbage collection bsery is avoided because the stack is
		// popped and reclaimed immediately after the try block.

		// Tells the GPU to write to this window.
		glfwMakeContextCurrent(window);

		// V-SYNC!!!
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		// Creating the context to which all graphics operations will be executed upon
		GL.createCapabilities();
		glEnable(GL_TEXTURE_2D);
	}

	/*
	 * Loads and constructs tiles based off of external file, then logs in
	 * tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();

		Shader shader = new Shader("shader");
		
		SpriteRenderer sprRenderer = new SpriteRenderer(shader);
		sprRenderer.spr = new Texture("tile1.png");
		renderer = sprRenderer;


		BufferedImage img = serializer.loadImage("tile1.png");
		SquareTile t1 = new SquareTile(1, img, renderer);

		tileLookup.put(1, t1);
	}

	
	private void loadTileHash(String filename) { // loads a hashmap assigning tile ID to Tile objects
		BufferedReader tileHashFile = null;

		try {
			tileHashFile = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		int num = 0;
		try {
			num = Integer.parseInt(tileHashFile.readLine());
		} catch (NumberFormatException e) {
			System.out.println("First line of file should be int");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < num; i++) {
			try {
				/*
				 * info[0] is tyr type of tile object we will put in .
				 * info[1] is the name of the sprite image
				 */
				String info[] = tileHashFile.readLine().split(":");
				BufferedImage sprite = ImageIO.read(new File(info[1]));
				// TODO change type of til
				if (Integer.parseInt(info[0]) == 0) { // squaretile: placeholder
					tileLookup.put(i, new SquareTile(i, sprite, renderer));
				}
				//TODO add more types of tiles
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void finishArea() { // called when character finishes a major area, updates level and chapter of
								// character

	}

	/**
	 * Adds a map object to maps variable. File should be directed to the correct map.
	 * @return 
	 * @throws IOException 
	 */
	private void loadMap(String filename) throws IOException {
		BufferedReader mapFile = null;

		try {
			mapFile = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		//first line is in format [xwidth]:[yheight]
		String[] mapsize = mapFile.readLine().split(":");
		int xwidth = Integer.parseInt(mapsize[0]);
		int yheight = Integer.parseInt(mapsize[1]);
		Tile[][] maptiles = new Tile[Integer.parseInt(mapsize[0])][Integer.parseInt(mapsize[1])];
		for(int i = 0; i < yheight; i++) {
			String[] tileLine = mapFile.readLine().split(":");
			for(int j = 0; j < xwidth; j++) {
				maptiles[i][j] = (Tile) (tileLookup.get(Integer.parseInt(tileLine[i]))).clone(); //want to clone the tile we load into array
			}
		}
		maps.add(new Map(maptiles, null, null));//TODO
	}
	private void loadCharData(String chardata) {
		//TODO
		
	}
	private void loadEntityData(String entityData) {
		
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
	
	private void initEntities() {
		entities = new ArrayList();
	}
	
	private void initPlayer() {
		player = new Player(0, new Position(0, 0), null, renderer, null);
		
		
		entities.add(player);
	}

	/*
	 * Game loop that handles rendering and stuff
	 */
	private void loop() {

		// Set clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		// Into the rendering loop we go
		// Remember the lambda callback we attached to key presses? This is where the
		// function returns.
		while (!glfwWindowShouldClose(window)) {
			// Clear frame buffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// Drawing stuff
			/*
			 * map.getGrid()[0][0].shader.bind(); glBegin(GL_QUADS); glVertex2f(-0.5f,
			 * 0.5f); glVertex2f(0.5f, 0.5f); glVertex2f(0.5f, -0.5f); glVertex2f(-0.5f,
			 * -0.5f); glEnd();
			 */
			update();
			drawer.draw(currmap, entities);

			// tldr: there are two buffers, one that is being displayed and one that we are
			// writing to.
			// This function waits until one buffer is written to before writing the next
			// one.
			// This is because of v-sync.
			glfwSwapBuffers(window);

			// Event listening stuff. Key callback is invoked here.
			glfwPollEvents();

			// canvas.paint();
		}
	}
	
	private void update() {
		for (Entity ent : entities) {
			ent.calculate();
		}
	}

}
