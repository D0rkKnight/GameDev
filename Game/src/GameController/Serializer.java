package GameController;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import Accessories.*;
import Rendering.Shader;
import Tiles.*;

public class Serializer {
	public void loadTileHash(String filename, HashMap<Integer, Tile> tileLookup, Shader shader) { // loads a hashmap
																									// assigning tile ID
																									// to Tile objects
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
				 * info[0] is tyr type of tile object we will put in . info[1] is the name of
				 * the sprite image
				 */
				String info[] = tileHashFile.readLine().split(":");
				BufferedImage sprite = ImageIO.read(new File(info[1]));
				// TODO change type of til
				if (Integer.parseInt(info[0]) == 0) { // squaretile: placeholder
					tileLookup.put(i, new SquareTile(i, sprite, shader));
				}
				// TODO add more types of tiles
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @return int[0] new chapter, int[1] new level
	 */
	public int[] finishArea() {

		return null; // called when character finishes a major area, updates level and chapter of
						// character

	}

	/**
	 * Returns a map in the form of 2d tile array. filename should be directed to
	 * the correct map data file
	 * 
	 * @return
	 * @throws IOException
	 */
	public Tile[][] loadMap(String filename, HashMap<Integer, Tile> tileLookup) throws IOException {
		BufferedReader mapFile = null;

		try {
			mapFile = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		/*
		 * Map data file is in format first line [xwidth]:[yheight], onwards is
		 * [tileID]:[tileID]:[tileID]:[tileID] [tileID]:[tileID]:[tileID]:[tileID]
		 * [tileID]:[tileID]:[tileID]:[tileID]
		 */
		String[] mapsize = mapFile.readLine().split(":");
		int xwidth = Integer.parseInt(mapsize[0]);
		int yheight = Integer.parseInt(mapsize[1]);
		Tile[][] maptiles = new Tile[Integer.parseInt(mapsize[0])][Integer.parseInt(mapsize[1])];
		for (int i = 0; i < yheight; i++) {
			String[] tileLine = mapFile.readLine().split(":");
			for (int j = 0; j < xwidth; j++) {
				maptiles[i][j] = (tileLookup.get(Integer.parseInt(tileLine[i]))).clone(); // want to clone the tile we
																							// load into array
			}
		}
		return maptiles;
	}

	public void loadAccessoryHash(String filename) {

	}

	/**
	 * returns a int array with the char hp, stamina, and accessory ids maximum
	 * hp/stamina will be calculated from accessories equipped
	 * 
	 * @param filename
	 * @param accessoryData
	 * @return
	 */
	public ArrayList<Integer> loadCharData(String filename, HashMap<Integer, Accessory> accessoryData) {
		BufferedReader charFile = null;
		try {
			charFile = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		/*
		 * Character data file should be in the format First line [currenthp] Second
		 * line [currentstamina] Third line accessories in the form
		 * [accessoryID]:[accessoryID]:[accessoryID]:[accessoryID]:[accessoryID]
		 * 
		 * 
		 */
		ArrayList<Integer> data = new ArrayList<Integer>();
		try {
			data.add(Integer.parseInt(charFile.readLine()));
			data.add(Integer.parseInt(charFile.readLine()));
			String[] accessoryIDs = charFile.readLine().split(":");
			for(String ID: accessoryIDs) {
				data.add(Integer.parseInt(ID));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;

	}

	public void loadEntityData(String entityData) {

	}

	/*
	 * Wrapper function for loading an image
	 */
	public BufferedImage loadImage(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return img;
	}
}
