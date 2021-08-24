package GameController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import Entities.Framework.Entity;
import Entities.PlayerPackage.PlayerFramework;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Elements.TileGFX;
import Tiles.Tile;
import Wrappers.Color;

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

	public static Document readDoc(String url) throws Exception {
		return readDoc(new File(url));
	}

	private static Element retrieveElement(Element e, String name) {
		return (Element) e.getElementsByTagName(name).item(0);
	}

	private static Element retrieveElement(Document doc, String name) {
		return (Element) doc.getElementsByTagName(name).item(0);
	}

	public static void loadTileHash(String fdir, String fname, HashMap<Integer, Tile> tileMap) throws Exception {

		Document doc = readDoc(fdir + fname);
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
			String gfxName = "None"; // Default name
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
					gfxName = val;
				}
			}

			// Create and submit tile
			int id = Integer.parseInt(e.getAttribute("id"));

			// Creating the tile
			int row = id / tilesWide;
			int column = id % tilesWide;
			TileGFX tGFX = new TileGFX(gfxName);

			Tile t = new Tile(hs, tileSheet.genSubTex(column, row), tGFX, new Vector2f(),
					new Vector2f(GameManager.tileSize));

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
				grid[x][y] = new Tile(t, new Vector2f(i * GameManager.tileSize, j * GameManager.tileSize),
						new Vector2f(GameManager.tileSize));
			}
		}

		return grid;
	}

	// TODO: Rewrite this function
	@SuppressWarnings("unchecked")
	public static ArrayList<Entity> loadEntities(Document doc, int tileSize) {
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
			EntityData propVals = new EntityData();

			Float eTileW = null;
			Float eTileH = null;
			float xTPos;
			float yTPos;
			String template = entity.getAttribute("template");

			xTPos = Float.parseFloat(entity.getAttribute("x")) / GameManager.tileSpriteSize;
			yTPos = Float.parseFloat(entity.getAttribute("y")) / GameManager.tileSpriteSize;

			if (!template.isEmpty()) {
				String path = shearFileDirectory(template);

				path = path.substring(0, path.length() - 3);

				// Load data from template
				Template t = templates.get(path);

				eTileW = t.properties.fl("width") / GameManager.tileSpriteSize;
				eTileH = t.properties.fl("height") / GameManager.tileSpriteSize;

				// Load in base data to propVals
				propVals = new EntityData(t.properties);
			}

			// Crummy way of doing overrides TODO: Fix this later
			// Loading data in tile cords
			if (entity.hasAttribute("width")) {
				eTileW = Float.parseFloat(entity.getAttribute("width")) / GameManager.tileSpriteSize;
				eTileH = Float.parseFloat(entity.getAttribute("height")) / GameManager.tileSpriteSize;
			}

			// Overrides
			Element propPar = retrieveElement(entity, "properties");

			if (propPar != null) {
				// Write to a hashmap
				NodeList propList = propPar.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++) {
					Element ele = (Element) propList.item(j);
					processDOMProperty(propVals, ele, false);
				}
			}

			yTPos += eTileH;
			yTPos = height - yTPos;

			// Converting to world cords
			Vector2f newPos = new Vector2f(xTPos, yTPos).mul(GameManager.tileSize);
			Vector2f newDims = new Vector2f(eTileW, eTileH).mul(GameManager.tileSize);

			// Get class to generate the entity from
			if (!propVals.d.containsKey("class")) {
				System.err.println("No class property defined");
				System.exit(1);
			}

			Entity ent = null;

			try {
				String className = propVals.str("class");

				Class<Entity> clazz = (Class<Entity>) Class.forName(className);

				// Don't reconstruct the player
				if (clazz.isAssignableFrom(PlayerFramework.class) && GameManager.player != null) {
					// Do nothing.
				}

				else {
					// Get factory
					Method factory = clazz.getMethod("createNew", EntityData.class, Vector2f.class, Vector2f.class);
					ent = (Entity) factory.invoke(null, propVals, newPos, newDims);

					// We know for certain there is no prior player, so we can safely assign it to
					// the GM singleton
					if (ent instanceof PlayerFramework)
						GameManager.player = (PlayerFramework) ent;
				}

			} catch (Exception e) {
				if (e instanceof InvocationTargetException) {
					System.err.println("Error within invoked method");
					e.getCause().printStackTrace();
				} else {
					e.printStackTrace();
				}

				System.exit(1);
			}

			if (ent != null) {
				entities.add(ent);
			}
		}

		return entities;

	}

	static HashMap<String, Template> templates;

	static void loadTemplates(String fDir) {
		templates = new HashMap<String, Template>();
		File dir = new File(fDir);

		File[] files = dir.listFiles();

		for (File f : files) {
			Document doc = readDoc(f);

			Element template = (Element) doc.getElementsByTagName("template").item(0);

			Element obj = (Element) template.getElementsByTagName("object").item(0);

			// Just dump the info in
			NamedNodeMap attribList = obj.getAttributes();
			EntityData data = new EntityData();

			for (int i = 0; i < attribList.getLength(); i++) {
				Node n = attribList.item(i);
				String key = n.getNodeName();
				Object val = n.getNodeValue();

				if (key.equals("width") || key.equals("height"))
					val = Float.parseFloat((String) val);

				data.d.put(key, val);
			}

			// Insert property data
			NodeList props = template.getElementsByTagName("property");
			for (int i = 0; i < props.getLength(); i++) {
				Element e = (Element) props.item(i);
				processDOMProperty(data, e, true);
			}

			Template t = new Template(data);
			templates.put(obj.getAttribute("name"), t);

			if (!obj.hasAttribute("name")) {
				System.err.println("Name not set for template " + f.getName());
				System.exit(1);
			}
		}
	}

	private static void processDOMProperty(EntityData data, Element ele, boolean watchForOverrides) {
		String name = ele.getAttribute("name");
		String valStr = ele.getAttribute("value");

		Object out = valStr;

		if (watchForOverrides) {
			if (data.d.containsKey(name)) {
				System.err.println("Data overriden!");
				System.exit(1);
			}
		}

		if (ele.hasAttribute("type")) {
			String typeStr = ele.getAttribute("type");

			if (typeStr.equals("int") || typeStr.equals("object"))
				out = Integer.parseInt(valStr);
			if (typeStr.equals("boolean"))
				out = Boolean.parseBoolean(valStr);
			if (typeStr.equals("float"))
				out = Float.parseFloat(valStr);
			if (typeStr.equals("color")) {
				out = new Color();
				if (!valStr.isEmpty()) {
					out = new Color(valStr, Color.hexFormat.ARGB);
				}
			}
		}

		data.d.put(name, out);
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
