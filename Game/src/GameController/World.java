package GameController;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import Debugging.Debug;
import Entities.Player;
import Entities.Framework.Entity;
import Entities.Framework.Entrance;
import Graphics.Rendering.Drawer;
import Tiles.Tile;

public class World {

	// Storage for tiles
	private static HashMap<Integer, Map> maps;
	public static Map currmap;

	public static void init() {
		maps = new HashMap<>();

		Map map0 = genMap("assets/Maps/", "test.tmx");
		map0.setEntranceLink(0, new int[] { 1, 0 }); // Entrance 0 is to exit 0 of map 1

		maps.put(0, map0);

		Map map1 = genMap("assets/Maps/", "test2.tmx");
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
		// Tile chunks
		Drawer.initTileChunks(map.grids.get("ground"));

		// Load entities
		ArrayList<Entity> ents = map.retrieveEntities();
		GameManager.loadEntities(ents);

		return ents;
	}

	public static void switchMap(int mapId, int entranceId) {
		Debug.clearElements();

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

		// Reset time once operations are done
		Time.reset();
	}
}
