package GameController;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import Debugging.Debug;
import Entities.Player;
import Entities.Framework.Entity;
import Entities.Framework.Entrance;
import Graphics.Drawer;
import Tiles.Tile;

public class World {

	// Storage for tiles
	private static HashMap<Integer, Map> maps;
	public static Map currmap;

	public static void init() {
		maps = new HashMap<>();

		Map map0 = genMap("assets/Maps/Forest/", "forestE.tmx");
		map0.setEntranceLink(0, new int[] { 1, 0 }); // Entrance 0 is to exit 0 of
		// map 1

		maps.put(0, map0);

		Map map1 = genMap("assets/Maps/Forest/", "forest1.tmx");
		map1.setEntranceLink(0, new int[] { 0, 0 });

		maps.put(1, map1);

		currmap = maps.get(0);
		loadMap(currmap);
	}

	/**
	 * Initializes a map, and assigns it to currmap.
	 * 
	 * @param fileDir
	 * @param fileName
	 */
	private static Map genMap(String fileDir, String fileName) {
		Document mapFile = null;
		try {
			mapFile = Serializer.readDoc(fileDir, fileName);
		} catch (Exception e) {
			System.err.println("File not found");
			e.printStackTrace();
		}

		HashMap<String, Tile[][]> mapData = null;
		try {
			mapData = Serializer.loadTileGrids(mapFile, GameManager.tileLookup);
		} catch (Exception e) {
			System.err.println("map error");
			e.printStackTrace();
		}

		Map out = new Map(mapData, mapFile);
		return out;
	}

	private static ArrayList<Entity> loadMap(Map map) {

		// TODO: Deal with all the todos
		// TODO: Layers within BG and FG aren't ordered, because HashMaps aren't
		// ordered.

		// FG_... means foreground.
		// Grab foreground and background layers
		ArrayList<String> rlBG = new ArrayList<>();
		ArrayList<String> rlFG = new ArrayList<>();
		ArrayList<String> rlGround = new ArrayList<>();

		for (String key : map.grids.keySet()) {
			String[] parts = key.split("_");
			String head = parts[0].toLowerCase();

			// Ground also goes to the background.
			if (head.equals("bg"))
				rlBG.add(key);
			if (head.equals("ground"))
				rlGround.add(key);
			if (head.equals("fg"))
				rlFG.add(key);
		}
		// TESTING
		Drawer.generateLayerVertexData(map.grids, rlBG, Drawer.LayerEnum.BG);
		Drawer.generateLayerVertexData(map.grids, rlGround, Drawer.LayerEnum.GROUND);
		Drawer.generateLayerVertexData(map.grids, rlFG, Drawer.LayerEnum.FG);

		// Load entities
		ArrayList<Entity> ents = map.retrieveEntities();
		GameManager.loadEntities(ents);

		return ents;
	}

	public static void switchMap(int mapId, int entranceId) {
		Debug.clearElements();
		Drawer.clearScreenBuffer(); // This gets called before the draw call anyways
		Drawer.onSceneChange();

		// Dump entities
		for (Entity e : GameManager.entities)
			if (!(e instanceof Player))
				GameManager.unsubscribeEntity(e);
		GameManager.entityWaitingList.clear();
		GameManager.updateEntityList();

		currmap = maps.get(mapId);
		ArrayList<Entity> ents = loadMap(currmap);

		for (Entity e : ents) {
			if (e instanceof Entrance) {
				Entrance enter = (Entrance) e;
				if (enter.entranceId == entranceId) {
					GameManager.player.getPosition().set(enter.getPosition());
					enter.isActive = false;
					break;
				}
			}
		}

		// Reset time & snap camera once operations are done
		Time.reset();
		Camera.main.snapToTarget(GameManager.player.getPosition());
	}
}
