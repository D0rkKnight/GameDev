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
	public static Map currmap;

	public static void init() {
		loadMap(currmap);
	}

	/**
	 * Initializes a map
	 * 
	 * @param fileDir
	 * @param fileName
	 */
	public static Map genMap(String url) {
		Document mapFile = null;
		try {
			mapFile = Serializer.readDoc(url);
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

		Drawer.generateLayerVertexData(map.grids, rlBG, Drawer.LayerEnum.BG);
		Drawer.generateLayerVertexData(map.grids, rlGround, Drawer.LayerEnum.GROUND);
		Drawer.generateLayerVertexData(map.grids, rlFG, Drawer.LayerEnum.FG);

		// Load entities
		ArrayList<Entity> ents = map.retrieveEntities();

		GameManager.loadEntities(ents);

		return ents;
	}

	public static void switchMap(EntranceData dest) {
		Debug.clearElements();
		Drawer.clearScreenBuffer(); // This gets called before the draw call anyways
		Drawer.onSceneChange();

		// Dump entities
		for (Entity e : GameManager.entities)
			if (!(e instanceof Player))
				GameManager.unsubscribeEntity(e);
		GameManager.entityWaitingList.clear();
		GameManager.updateEntityList();

		currmap = dest.map;
		ArrayList<Entity> ents = loadMap(currmap);

		// Try to move player
		boolean success = false;
		for (Entity e : ents) {
			if (e instanceof Entrance) {
				Entrance enter = (Entrance) e;

				if (enter.localMapPos.equals(dest.mapPos) && enter.dir == dest.dir) {
					GameManager.player.getPosition().set(enter.getPosition());
					enter.isActive = false;
					success = true;

					break;
				}
			}
		}

		if (success == false) {
			System.err.println("Failed to move player");
		}

		// Reset time & snap camera once operations are done
		Time.reset();
		Camera.main.snapToTarget(GameManager.player.getPosition());
	}
}
