package GameController;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joml.Vector2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Accessories.Accessory;
import Collision.Shapes.Shape;
import Collision.Shapes.Shape.ShapeEnum;
import Entities.Button;
import Entities.CrawlerEnemy;
import Entities.FloaterEnemy;
import Entities.Player;
import Entities.ShardSlimeEnemy;
import Entities.Framework.Entity;
import Entities.Framework.Entrance;
import Entities.Framework.Interactive;
import Entities.Framework.Prop;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.GrassRenderer;
import Graphics.Rendering.GrassShader;
import Tiles.Tile;
import Wrappers.Stats;

public class Serializer {

	public static Document readDoc(File f) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(f);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();

		return doc;
	}

	public static Document readDoc(String fdir, String fname) throws Exception {
		return readDoc(new File(fdir + fname));
	}

	private static Element retrieveElement(Element e, String name) {
		return (Element) e.getElementsByTagName(name).item(0);
	}

	private static Element retrieveElement(Document doc, String name) {
		return (Element) doc.getElementsByTagName(name).item(0);
	}

	public static void loadTileHash(String fdir, String fname, HashMap<Integer, Tile> tileMap, GeneralRenderer rend)
			throws Exception {

		Document doc = readDoc(fdir, fname);
		NodeList nList = doc.getElementsByTagName("tile");

		// Grab textures
		Element tilesetE = retrieveElement(doc, "tileset");
		Element srcE = retrieveElement(doc, "image");
		int tw = Integer.parseInt(tilesetE.getAttribute("tilewidth"));
		int th = Integer.parseInt(tilesetE.getAttribute("tileheight"));
		int tilesWide = Integer.parseInt(tilesetE.getAttribute("columns"));

		String src = srcE.getAttribute("source");
		TextureAtlas tileSheet = new TextureAtlas(Texture.getTex(fdir + src), tw, th);

		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			Element props = retrieveElement(e, "properties");
			NodeList propList = props.getElementsByTagName("property");

			// Grab properties
			ShapeEnum hs = null;
			ArrayList<String> gfxs = new ArrayList<>();
			for (int j = 0; j < propList.getLength(); j++) {
				Element propE = (Element) propList.item(j);

				@SuppressWarnings("unused")
				String type = propE.getAttribute("type");
				String name = propE.getAttribute("name");
				String val = propE.getAttribute("value");

				if (name.equals("HammerShape")) {
					if (val.equals("bl"))
						hs = Shape.ShapeEnum.TRIANGLE_BL;
					else if (val.equals("br"))
						hs = Shape.ShapeEnum.TRIANGLE_BR;
					else if (val.equals("ul"))
						hs = Shape.ShapeEnum.TRIANGLE_UL;
					else if (val.equals("ur"))
						hs = Shape.ShapeEnum.TRIANGLE_UR;
				}

				if (name.equals("GFX")) {
					gfxs.add(val);
				}
			}

			// Create and submit tile
			int id = Integer.parseInt(e.getAttribute("id"));

			// Creating the tile
			int row = id / tilesWide;
			int column = id % tilesWide;
			Tile t = new Tile(rend, hs, tileSheet.genSubTex(column, row));

			for (String gfxName : gfxs)
				t.addGFX(gfxName);

			tileMap.put(id, t);
		}
	}

	public static String shearFileDirectory(String path) {
		String out = "";

		// Trim the path to remove folders
		for (int j = path.length() - 1; j >= 0; j--) {
			if (path.charAt(j) == '/') {
				out = path.substring(j + 1, path.length());
				break;
			}
		}

		return out;
	}

	/**
	 * 
	 * @param doc
	 * @param tileMap Tile data, organized by tile sets
	 * @return
	 */
	public static HashMap<String, Tile[][]> loadTileGrids(Document doc,
			HashMap<String, HashMap<Integer, Tile>> tileMap) {
		// Grab all gids
		ArrayList<Integer> gids = new ArrayList<>();
		ArrayList<String> tSetNames = new ArrayList<>();

		// Get list of tile sets that have been used, to access tileMap data
		NodeList tilesets = doc.getElementsByTagName("tileset");
		for (int i = 0; i < tilesets.getLength(); i++) {
			Element tilesetE = (Element) tilesets.item(i);
			gids.add(Integer.parseInt(tilesetE.getAttribute("firstgid"))); // This is an offset value

			String path = tilesetE.getAttribute("source");
			path = shearFileDirectory(path);

			tSetNames.add(path);
		}

		// Parse data and read grids
		NodeList layers = doc.getElementsByTagName("layer");
		HashMap<String, Tile[][]> grids = new HashMap<>();

		for (int i = 0; i < layers.getLength(); i++) {
			Element layer = (Element) layers.item(i);
			String name = layer.getAttribute("name");

			Tile[][] tGrid = null;
			try {
				tGrid = loadTileGridFromLayer(layer, tileMap, gids, tSetNames);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			grids.put(name, tGrid);
		}

		return grids;
	}

	/**
	 * 
	 * @param layerE
	 * @param tileMap
	 * @param gids
	 * @param tSetNames
	 * @return
	 * @throws Exception
	 */
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

				// TODO: Write in loading errors
				// Errors include:
				// Can't find tileset
				// Tileset is empty (because no properties were set)

				int offset = gids.get(a);
				HashMap<Integer, Tile> tSet = tileMap.get(tSetNames.get(a));

				Tile t = tSet.get(id - offset);
				grid[x][y] = t.clone();
			}
		}

		return grid;
	}

	private static enum ReadMode {
		NONE, COMBATANT, INTERACTABLE, STATIC, PROP
	}

	private static ReadMode readMode = ReadMode.NONE;
	private static HashMap<String, String> activeDataHash;

	// TODO: Load from templates, rather than the text file.
	public static HashMap<String, Entity> loadEntityHash(String fileDir, String fileName, GeneralRenderer renderer)
			throws NumberFormatException, IOException {
		BufferedReader charFile = null;
		try {
			charFile = new BufferedReader(new FileReader(fileDir + fileName));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			e.printStackTrace();
		}

		HashMap<String, Entity> entityHash = new HashMap<String, Entity>();

		String line;
		while ((line = charFile.readLine()) != null) {
			if (line.isEmpty())
				continue;

			if (line.contains("COMBATANTS")) {
				readMode = ReadMode.COMBATANT;
				continue;
			} else if (line.contains("INTERACTABLES")) {
				readMode = ReadMode.INTERACTABLE;
				continue;
			} else if (line.contains("STATIC")) {
				readMode = ReadMode.STATIC;
				continue;
			} else if (line.contains("PROPS")) {
				readMode = ReadMode.PROP;
				continue;
			}

			if (readMode == ReadMode.NONE) {
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

			String ID = activeDataHash.get("ID");
			Entity newE = null;

			if (readMode == ReadMode.COMBATANT) {
				float HP = rhFloat("HP");
				float ST = rhFloat("Stamina");
				float HPR = rhFloat("HPregen");
				float STR = rhFloat("StaminaRegen");
				Stats stats = new Stats(HP, ST, HPR, STR);

				if (ID.equals("PLAYER")) {
					newE = new Player(ID, null, renderer, ID, stats);
				} else if (ID.equals("FLOATER")) {
					newE = new FloaterEnemy(ID, null, renderer, ID, stats);
				} else if (ID.equals("BOUNCER")) {
					newE = new ShardSlimeEnemy(ID, null, renderer, ID, stats);
				} else if (ID.equals("CRAWLER")) {
					newE = new CrawlerEnemy(ID, null, renderer, ID, stats);
				}
			}

			else if (readMode == ReadMode.INTERACTABLE) {
				int STATE = rhInt("State");
				int TIME_ON = rhInt("TimeOn");
				float ACT_DIST = rhFloat("ActivationDistance");

				if (ID.equals("BUTTON")) {
					newE = new Button(ID, null, renderer, ID, STATE, TIME_ON, ACT_DIST, null);
				}
			}

			else if (readMode == ReadMode.STATIC) {
				if (ID.equals("ENTRANCE")) {
					// Without configuration, the default value of every entrance id is -1.
					newE = new Entrance(ID, null, renderer, ID, new Vector2f(30, 30), -1);
				}
			}

			else if (readMode == ReadMode.PROP) {
				if (ID.equals("PROP")) {
					// New renderer, bois
					GrassRenderer propRend = new GrassRenderer(GrassShader.genShader("grassShader"));

					newE = new Prop(ID, null, propRend, ID);
				}
			}

			if (newE == null) {
				new Exception("Enemy cannot be found").printStackTrace();
				System.exit(1);
			}

			entityHash.put(ID, newE);
		}

		// Also load templates
		loadTemplates("assets/Maps/Templates");

		return entityHash;
	}

	private static int rhInt(String str) {
		return Integer.parseInt(activeDataHash.get(str));
	}

	private static float rhFloat(String str) {
		return Float.parseFloat(activeDataHash.get(str));
	}

	@SuppressWarnings("unused")
	private static String rhStr(String str) {
		return activeDataHash.get(str);
	}

	// TODO: Rewrite this function
	public static ArrayList<Entity> loadEntities(Document doc, HashMap<String, Entity> entityHash, int tileSize) {
		Element layerE = (Element) doc.getElementsByTagName("layer").item(0);

		@SuppressWarnings("unused")
		int width = Integer.parseInt(layerE.getAttribute("width"));
		int height = Integer.parseInt(layerE.getAttribute("height"));

		Element layerO = (Element) doc.getElementsByTagName("objectgroup").item(0);
		NodeList objects = (layerO).getElementsByTagName("object");
		int entitynum = objects.getLength();
		ArrayList<Entity> entities = new ArrayList<Entity>();

		for (int i = 0; i < entitynum; i++) {
			Element entity = (Element) objects.item(i);

			HashMap<String, String> propVals = new HashMap<>();
			Element propPar = retrieveElement(entity, "properties");

			if (propPar != null) {
				// Write to a hashmap
				NodeList propList = propPar.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++) {
					Element ele = (Element) propList.item(j);
					String name = ele.getAttribute("name");
					String val = ele.getAttribute("value");

					propVals.put(name, val);
				}
			}

			String ID;
			float eTileW;
			float eTileH;
			float xTPos;
			float yTPos;
			String template = entity.getAttribute("template");

			xTPos = Float.parseFloat((entity).getAttribute("x")) / GameManager.tileSpriteSize;
			yTPos = Float.parseFloat((entity).getAttribute("y")) / GameManager.tileSpriteSize;

			if (!template.isEmpty()) {
				String path = shearFileDirectory(template);

				path = path.substring(0, path.length() - 3);

				// Load data from template
				Template t = templates.get(path);

				ID = t.properties.get("type");
				eTileW = Float.parseFloat(t.properties.get("width")) / GameManager.tileSpriteSize;
				eTileH = Float.parseFloat(t.properties.get("height")) / GameManager.tileSpriteSize;
			}

			else {
				// Load data from what is given
				ID = (entity).getAttribute("type");

				// Loading data in tile cords
				eTileW = Float.parseFloat((entity).getAttribute("width")) / GameManager.tileSpriteSize;
				eTileH = Float.parseFloat((entity).getAttribute("height")) / GameManager.tileSpriteSize;
			}

			yTPos += eTileH;
			yTPos = height - yTPos;

			Entity baseE = entityHash.get(ID);
			Entity e = null;

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
					addEnt = false;
				}
			} else if (baseE instanceof Interactive) {
				e = ((Button) baseE).createNew(newX, yTPos * newY, GameManager.player);
			} else if (baseE instanceof Entrance) {
				int entId = Integer.parseInt(propVals.get("entrId"));
				e = ((Entrance) baseE).createNew(newX, newY, newW, newH, entId);
			} else {
				e = baseE.createNew(newX, newY);
			}

			if (addEnt)
				entities.add(e);
		}
		return entities;

	}

	static HashMap<String, Template> templates;

	private static void loadTemplates(String fDir) {
		templates = new HashMap<String, Template>();
		File dir = new File(fDir);

		File[] files = dir.listFiles();

		for (File f : files) {
			Document doc = readDoc(f);

			Element template = (Element) doc.getElementsByTagName("template").item(0);

			Element obj = (Element) template.getElementsByTagName("object").item(0);

			// Just dump the info in
			NamedNodeMap attribList = obj.getAttributes();
			HashMap<String, String> data = new HashMap<String, String>();
			for (int i = 0; i < attribList.getLength(); i++) {
				Node n = attribList.item(i);
				data.put(n.getNodeName(), n.getNodeValue());
			}

			templates.put(obj.getAttribute("name"), new Template(data));
		}
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
			e.printStackTrace();
		} catch (IOException e) {
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
			e.printStackTrace();
		} catch (IOException e) {
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
}
