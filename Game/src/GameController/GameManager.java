package GameController;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import Shaders.SpriteShader;
import Tiles.SquareTile;
import Tiles.Tile;

public class GameManager {
	
	//The frame and canvas
	private JFrame frame;
	private Renderer canvas;
	
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
		initTiles();
		map = loadMap();
		
		//Setting up renderer
		frame = new JFrame();
		
		canvas = new Renderer(map);
		canvas.setSize(1280, 720);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);
		
		loop();
	}
	
	/*
	 * Loads and constructs tiles based off of external file, then logs in tileLookup
	 */
	private void initTiles() {
		tileLookup = new HashMap<>();
		
		SpriteShader sprShader = new SpriteShader();
		
		BufferedImage img = loadImage("tile1.png");
		SquareTile t1 = new SquareTile(1, img, sprShader);
		
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
		while(true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//canvas.paint();
		}
	}
}
