package GameController;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joml.Vector2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Accessories.Accessory;
import Collision.HammerShapes.HammerShape;
import Entities.Button;
import Entities.CrawlerEnemy;
import Entities.FloaterEnemy;
import Entities.Player;
import Entities.ShardSlimeEnemy;
import Entities.Framework.Entity;
import Entities.Framework.Entrance;
import Entities.Framework.Interactive;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Tiles.Tile;
import Wrappers.Stats;

public class Serializer {

	public static Document readDoc(String fdir, String fname) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new File(fdir + fname));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();

		return doc;
	}

	private static Element retrieveElement(Element e, String name) {
		return (Element) e.getElementsByTagName(name).item(0);
	}

	public static void loadTileHash(String fdir, String fname, HashMap<Integer, Tile> tileMap,
			HashMap<Integer, HammerShape> hsMap, GeneralRenderer rend) throws Exception {

		Document doc = readDoc(fdir, fname);
		NodeList nList = doc.getElementsByTagName("tile");

		// Grab textures
		Element tilesetE = (Element) doc.getElementsByTagName("tileset").item(0);
		Element srcE = (Element) doc.getElementsByTagName("image").item(0);
		int tw = Integer.parseInt(tilesetE.getAttribute("tilewidth"));
		int th = Integer.parseInt(tilesetE.getAttribute("tileheight"));

		String src = srcE.getAttribute("source");
		Texture[] tileSheet = Texture.getSprSheet(fdir + src, tw, th).texs;

		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			Element props = (Element) e.getElementsByTagName("properties").item(0);
			NodeList propList = props.getElementsByTagName("property");

			// Grab properties
			HammerShape hs = null;
			for (int j = 0; j < propList.getLength(); j++) {
				Element propE = (Element) propList.item(j);

				String type = propE.getAttribute("type");
				String name = propE.getAttribute("name");
				String val = propE.getAttribute("value");

				if (name.equals("HammerShape") && type.equals("int")) {
					int valInt = Integer.parseInt(val);

					hs = hsMap.get(valInt);
				}
			}

			// Check that properties were retrieved properly
			if (hs == null)
				throw new Exception("Hammershape not found!");

			// Create and submit tile
			int id = Integer.parseInt(e.getAttribute("id"));
			Texture tex = tileSheet[id];

			Tile t = new Tile(rend, tex, hs);
			tileMap.put(id, t);
		}
	}

	public static HashMap<String, Tile[][]> loadTileGrids(Document doc,
			HashMap<String, HashMap<Integer, Tile>> tileMap) {
		// Grab all gids
		ArrayList<Integer> gids = new ArrayList<>();
		ArrayList<String> tSetNames = new ArrayList<>();

		NodeList tilesets = doc.getElementsByTagName("tileset");
		for (int i = 0; i < tilesets.getLength(); i++) {
			Element tilesetE = (Element) tilesets.item(i);
			gids.add(Integer.parseInt(tilesetE.getAttribute("firstgid"))); // This is an offset value

			String path = tilesetE.getAttribute("source");

			// Trim the path to remove folders
			for (int j = path.length() - 1; j >= 0; j--) {
				if (path.charAt(j) == '/') {
					path = path.substring(j + 1, path.length());
					break;
				}
			}

			tSetNames.add(path);
		}

		NodeList layers = doc.getElementsByTagName("layer");
		HashMap<String, Tile[][]> grids = new HashMap<>();

		for (int i = 0; i < layers.getLength(); i++) {
			Element layer = (Element) layers.item(i);
			String name = layer.getAttribute("name");

			Tile[][] tGrid = null;
			try {
				tGrid = loadTileGridFromLayer(layer, tileMap, gids, tSetNames);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}

			grids.put(name, tGrid);
		}

		return grids;
	}

	public static Tile[][] loadTileGridFromLayer(Element layerE, HashMap<String, HashMap<Integer, Tile>> tileMap,
			ArrayList<Integer> gids, ArrayList<String> tSetNames) throws Exception {

		int w = Integer.parseInt(layerE.getAttribute("width"));
		int h = Integer.parseInt(layerE.getAttribute("height"));

		// Decode data
		Element dataE = retrieveElement(layerE, "data");
		String d = trim(dataE.getTextContent());

		// Base 64 decode
		byte[] bytes = Base64.getDecoder().decode(d);

		IntBuffer intBuff = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		int[] intArr = new int[intBuff.remaining()];
		intBuff.get(intArr);

		// Now put in the tiles
		Tile[][] grid = new Tile[w][h];

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int index = i * w + j;

				// Invert y only
				int x = j;
				int y = h - i - 1;

				int id = intArr[index];
				if (id == 0) {
					grid[x][y] = null;
					continue;
				}

				// Get the right tileset data
				int a = gids.size() - 1;
				for (int k = 0; k < gids.size(); k++) {
					if (id < gids.get(k)) {
						a = k - 1;
						break;
					}
				}

				int offset = gids.get(a);
				HashMap<Integer, Tile> tSet = tileMap.get(tSetNames.get(a));

				Tile t = tSet.get(id - offset);
				grid[x][y] = t.clone();
			}
		}

		return grid;
	}

	private static final int READ_MODE_NONE = 0;
	private static final int READ_MODE_COMBATANT = 1;
	private static final int READ_MODE_INTERACTABLE = 2;
	private static final int READ_MODE_STATIC = 3;

	private static int readMode = READ_MODE_NONE;
	private static HashMap<String, String> activeDataHash;

	public static HashMap<Integer, Entity> loadEntityHash(String fileDir, String fileName, GeneralRenderer renderer)
			throws NumberFormatException, IOException {
		BufferedReader charFile = null;
		try {
			charFile = new BufferedReader(new FileReader(fileDir + fileName));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			e.printStackTrace();
		}

		HashMap<Integer, Entity> entityHash = new HashMap<Integer, Entity>();

		String line;
		while ((line = charFile.readLine()) != null) {
			if (line.isEmpty())
				continue;

			if (line.contains("COMBATANTS")) {
				readMode = READ_MODE_COMBATANT;
				continue;
			} else if (line.contains("INTERACTABLES")) {
				readMode = READ_MODE_INTERACTABLE;
				continue;
			} else if (line.contains("STATIC")) {
				readMode = READ_MODE_STATIC;
				continue;
			}

			if (readMode == READ_MODE_NONE) {
				new Exception("Read mode not specified at start of file").printStackTrace();
				System.exit(1);
			}

			// Format the data
			String[] enemy = line.split(":");
			activeDataHash = new HashMap<>();
			for (String str : enemy) {
				String[] splitStr = str.split(",");
				activeDataHash.put(splitStr[0], splitStr[1]);
			}

			String name = activeDataHash.get("Name");
			int ID = Integer.parseInt(activeDataHash.get("ID"));
			Entity newE = null;

			if (readMode == READ_MODE_COMBATANT) {
				float HP = rhFloat("HP");
				float ST = rhFloat("Stamina");
				float HPR = rhFloat("HPregen");
				float STR = rhFloat("StaminaRegen");
				Stats stats = new Stats(HP, ST, HPR, STR);

				if (name.equals("Player")) {
					newE = new Player(ID, null, renderer, name, stats);
				} else if (name.equals("Floater")) {
					newE = new FloaterEnemy(ID, null, renderer, name, stats);
				} else if (name.equals("Bouncer")) {
					newE = new ShardSlimeEnemy(ID, null, renderer, name, stats);
				} else if (name.equals("Crawler")) {
					newE = new CrawlerEnemy(ID, null, renderer, name, stats);
				}
			}

			else if (readMode == READ_MODE_INTERACTABLE) {
				int STATE = rhInt("State");
				int TIME_ON = rhInt("TimeOn");
				float ACT_DIST = rhFloat("ActivationDistance");

				if (name.equals("Button")) {
					newE = new Button(ID, null, renderer, name, STATE, TIME_ON, ACT_DIST, null);
				}
			}

			else if (readMode == READ_MODE_STATIC) {
				if (name.equals("Entrance")) {
					newE = new Entrance(ID, null, renderer, name, new Vector2f(30, 30));
				}
			}

			if (newE == null) {
				new Exception("Enemy cannot be found").printStackTrace();
				System.exit(1);
			}

			entityHash.put(ID, newE);
		}
		return entityHash;

	}

	private static int rhInt(String str) {
		return Integer.parseInt(activeDataHash.get(str));
	}

	private static float rhFloat(String str) {
		return Float.parseFloat(activeDataHash.get(str));
	}

	private static String rhStr(String str) {
		return activeDataHash.get(str);
	}

	public static ArrayList<Entity> loadEntities(Document doc, HashMap<Integer, Entity> entityHash, int tileSize) {
		Element layerE = (Element) doc.getElementsByTagName("layer").item(0);

		int width = Integer.parseInt(layerE.getAttribute("width"));
		int height = Integer.parseInt(layerE.getAttribute("height"));

		Element layerO = (Element) doc.getElementsByTagName("objectgroup").item(0);
		NodeList objects = (layerO).getElementsByTagName("object");
		int entitynum = objects.getLength();
		ArrayList<Entity> entities = new ArrayList<Entity>();

		for (int i = 0; i < entitynum; i++) {
			Element entity = (Element) objects.item(i);
			int ID = Integer.parseInt((entity).getAttribute("type"));

			// Loading data in tile cords
			float eTileW = Float.parseFloat((entity).getAttribute("width")) / GameManager.tileSpriteSize;
			float eTileH = Float.parseFloat((entity).getAttribute("height")) / GameManager.tileSpriteSize;

			float xTPos = Float.parseFloat((entity).getAttribute("x")) / GameManager.tileSpriteSize;
			float yTPos = Float.parseFloat((entity).getAttribute("y")) / GameManager.tileSpriteSize;

			yTPos += eTileH;
			yTPos = height - yTPos;

			Entity baseE = entityHash.get(ID);
			Entity e = null;

			if (i == 0 && !(baseE instanceof Player)) {
				new Exception("Player not first in entity queue.").printStackTrace();
				System.exit(1);
			}

			// Converting to world cords
			float newX = xTPos * GameManager.tileSize;
			float newY = yTPos * GameManager.tileSize;
			float newW = eTileW * GameManager.tileSize;
			float newH = eTileH * GameManager.tileSize;

			boolean addEnt = true;

			if (baseE instanceof Player) {
				if (GameManager.player == null) {
					e = baseE.createNew(newX, newY);
					GameManager.player = (Player) e;
				} else {
					GameManager.player.getPosition().set(newX, newY);
					addEnt = false;
				}
			} else if (baseE instanceof Interactive) {
				e = ((Button) baseE).createNew(newX, yTPos * newY, GameManager.player);
			} else if (baseE instanceof Entrance) {

				e = ((Entrance) baseE).createNew(newX, newY, newW, newH);
			} else {
				e = baseE.createNew(newX, newY);
			}

			if (addEnt)
				entities.add(e);
		}
		return entities;

	}

	private static String trim(String str) {
		BufferedReader br = new BufferedReader(new StringReader(str));
		StringBuffer out = new StringBuffer();

		try {
			String l;
			while ((l = br.readLine()) != null) {
				out.append(l.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out.toString();
	}

	/**
	 * Loads the progress (chapter number, level number, and room)
	 * 
	 * @param filename
	 * @return {chapter num 0-3, level num, room number}
	 */
	public int[] loadProgress(String filename) {
		BufferedReader progressFile = null;

		try {
			progressFile = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			e.printStackTrace();
		}
		int[] progress = new int[3];
		try {
			progress[0] = Integer.parseInt(progressFile.readLine());
			progress[1] = Integer.parseInt(progressFile.readLine());
			progress[2] = Integer.parseInt(progressFile.readLine());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return progress;

	}

	/**
	 * TODO when accessory game mechanics created
	 * 
	 * @param filename
	 * @return
	 */
	public HashMap<Integer, Accessory> loadAccessoryHash(String filename) {

		return null;

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
			System.err.println("File not found");
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
			for (String ID : accessoryIDs) {
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

	/**
	 * 
	 * @param entityData
	 */
	public ArrayList<Entity> loadEntityData(String entityData) {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		return entities;
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
