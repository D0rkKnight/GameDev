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
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Accessories.Accessory;
import Collision.HammerShape;
import Entities.Button;
import Entities.Entity;
import Entities.FloaterEnemy;
import Entities.Interactive;
import Entities.Player;
import Entities.ShardSlimeEnemy;
import Rendering.GeneralRenderer;
import Rendering.Texture;
import Tiles.Tile;
import Wrappers.Stats;

public class Serializer {
	
	public static Document readDoc(String fdir, String fname) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new File(fdir+fname));
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
		
		
		//Grab textures
		Element tilesetE = (Element) doc.getElementsByTagName("tileset").item(0);
		Element srcE = (Element) doc.getElementsByTagName("image").item(0);
		int tw = Integer.parseInt(tilesetE.getAttribute("tilewidth"));
		int th = Integer.parseInt(tilesetE.getAttribute("tileheight"));
		
		String src = srcE.getAttribute("source");
		Texture[] tileSheet = Texture.unpackSpritesheet(fdir+src, tw, th);
		
		for (int i=0; i<nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			Element props = (Element) e.getElementsByTagName("properties").item(0);
			NodeList propList = props.getElementsByTagName("property");
			
			//Grab properties
			HammerShape hs = null;
			for (int j=0; j<propList.getLength(); j++) {
				Element propE = (Element) propList.item(j);
				
				String type = propE.getAttribute("type");
				String name = propE.getAttribute("name");
				String val = propE.getAttribute("value");
				
				if (name.equals("HammerShape") && type.equals("int")) {
					int valInt = Integer.parseInt(val);
					
					hs = hsMap.get(valInt);
				}
			}
			
			//Check that properties were retrieved properly
			if (hs == null) throw new Exception("Hammershape not found!");
			
			//Create and submit tile
			int id = Integer.parseInt(e.getAttribute("id"));
			Texture tex = tileSheet[id];
			
			Tile t = new Tile(rend, tex, hs);
			tileMap.put(id, t);
		}
	}
	
	public static HashMap<String, Tile[][]> loadTileGrids(Document doc, HashMap<String, HashMap<Integer, Tile>> tileMap) {
		//Grab all gids
		ArrayList<Integer> gids = new ArrayList<>();
		ArrayList<String> tSetNames = new ArrayList<>();
		
		NodeList tilesets = doc.getElementsByTagName("tileset");
		for (int i=0; i<tilesets.getLength(); i++) {
			Element tilesetE = (Element) tilesets.item(i);
			gids.add(Integer.parseInt(tilesetE.getAttribute("firstgid"))); //This is an offset value
			tSetNames.add(tilesetE.getAttribute("source"));
		}
		
		NodeList layers = doc.getElementsByTagName("layer");
		HashMap<String, Tile[][]> grids = new HashMap<>();
		
		for (int i=0; i<layers.getLength(); i++) {
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
		
		//Decode data
		Element dataE = retrieveElement(layerE, "data");
		String encoding = dataE.getAttribute("encoding");
		String compression = dataE.getAttribute("compression");
		String d = trim(dataE.getTextContent());
		
		//Base 64 decode
		byte[] bytes = Base64.getDecoder().decode(d);
		
		IntBuffer intBuff = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		int[] intArr = new int[intBuff.remaining()];
		intBuff.get(intArr);
		
		//Now put in the tiles
		Tile[][] grid = new Tile[w][h];
		
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++) {
				int index = i*w + j;
				
				//Invert y only
				int x = j;
				int y = h-i-1;
				
				int id = intArr[index];
				if (id == 0) {
					grid[x][y] = null;
					continue;
				}
				
				//Get the right tileset data
				int a = gids.size()-1;
				for (int k=0; k<gids.size(); k++) {
					if (id < gids.get(k)) {
						a=k-1;
						break;
					}
				}
				
				//System.out.println(gids.get(1));
				
				int offset = gids.get(a);
				HashMap<Integer, Tile> tSet = tileMap.get(tSetNames.get(a));
				
				Tile t = tSet.get(id-offset);
				grid[x][y] = t.clone();
			}
		}
		
		return grid;
	}
	
	public static HashMap<Integer, Entity> loadEntityHash(String fileDir, String fileName, GeneralRenderer renderer) throws NumberFormatException, IOException{
		BufferedReader charFile = null;
		try {
			charFile = new BufferedReader(new FileReader(fileDir + fileName));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			e.printStackTrace();
		}
		int enemies = Integer.parseInt(charFile.readLine().split(":")[1]);
		HashMap<Integer, Entity> enemyHash = new HashMap<Integer, Entity>();
		for(int i = 0; i < enemies; i++) {
			String[] enemy = charFile.readLine().split(":");
			String name = enemy[0].split(",")[1];
			int ID = Integer.parseInt(enemy[1].split(",")[1]);
			float HP = Float.parseFloat(enemy[2].split(",")[1]);
			float ST = Float.parseFloat(enemy[3].split(",")[1]);
			float HPR = Float.parseFloat(enemy[4].split(",")[1]);
			float STR = Float.parseFloat(enemy[5].split(",")[1]);
			if(name.equals("Player")){
				enemyHash.put(i, new Player(ID, null, renderer, name, new Stats(HP, ST, HPR, STR)));
			}
			else if(name.equals("Floater")){
				enemyHash.put(i, new FloaterEnemy(ID, null, renderer, name, new Stats(HP, ST, HPR, STR)));
			}
			else if(name.equals("Bouncer")){
				enemyHash.put(i, new ShardSlimeEnemy(ID, null, renderer, name, new Stats(HP, ST, HPR, STR)));
			}
			else if(name.equals("Button")){
				enemyHash.put(i, new Button(ID, null, renderer, name, Integer.parseInt(enemy[6].split(",")[1]), Integer.parseInt(enemy[7].split(",")[1]), Float.parseFloat(enemy[8].split(",")[1]), null));
			}
			else {
				System.out.println("error, wrong enemy name");
			}
		}
		return enemyHash;
		
	}
	
	public static ArrayList<Entity> loadEntities(Document doc, HashMap<Integer, Entity> entityHash, int tileSize){
		Element layerE = (Element) doc.getElementsByTagName("layer").item(0);
		int width = Integer.parseInt(layerE.getAttribute("width"));
		int height = Integer.parseInt(layerE.getAttribute("height"));
		
		Element layerO = (Element) doc.getElementsByTagName("objectgroup").item(0);
		NodeList objects = (layerO).getElementsByTagName("object");
		int entitynum = objects.getLength();
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		for(int i = 0; i < entitynum; i++) {
			Element entity = (Element) objects.item(i);
			System.out.println(tileSize);
			int ID = Integer.parseInt((entity).getAttribute("type"));
			
			float xPos = Float.parseFloat((entity).getAttribute("x")) / GameManager.tileSpriteSize;
			float yPos = Float.parseFloat((entity).getAttribute("y")) / GameManager.tileSpriteSize;

			
			yPos += Float.parseFloat((entity).getAttribute("height")) / GameManager.tileSpriteSize;
			yPos = height - yPos;
			System.out.println(entityHash.size() + "s");
			
			Entity e;
			if (entityHash.get(ID) instanceof Player) {
				e = entityHash.get(ID).clone(xPos * GameManager.tileSize, yPos * GameManager.tileSize);
				GameManager.player = (Player) e;
			}
			else if(entityHash.get(ID) instanceof Interactive) {
				e = ((Button)entityHash.get(ID)).clone(xPos * GameManager.tileSize, yPos * GameManager.tileSize, GameManager.player);
			}
			else {
				e = entityHash.get(ID).clone(xPos * GameManager.tileSize, yPos * GameManager.tileSize);
			}
			
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
