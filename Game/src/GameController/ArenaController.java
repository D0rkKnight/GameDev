package GameController;

import java.util.ArrayList;

import org.joml.Vector2f;

import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import UI.NewWaveListener;

/**
 * Handles enemy spawns in an arena setting
 * 
 * @author Hanzen Shou
 *
 */

public class ArenaController {

	public static class SpawnLocation extends Entity {

		public int id;

		public SpawnLocation(String ID, Vector2f position, String name, int id) {
			super(ID, position, name);

			this.id = id;
		}

		/**
		 * Needs to be serializable from Tiled2D
		 */
		public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
			System.out.println("Hello");

			return new SpawnLocation(vals.str("type"), pos, vals.str("name"), vals.in("id"));
		}
	}

	public static class SpawnData {
		public EntityData data;
		public int loc;

		public SpawnData(EntityData data, int loc) {
			this.data = data;
			this.loc = loc;
		}

		public static SpawnData genNew(String templateName, int locId) {
			EntityData data = Serializer.templates.get(templateName).properties;

			return new SpawnData(data, locId);
		}
	}

	public static class Wave {
		private ArrayList<SpawnData> spawns;

		public Wave(ArrayList<SpawnData> spawns) {
			this.spawns = spawns;
		}

		public void release() {
			for (SpawnData s : spawns) {

				// Retrieve spawn location from id
				SpawnLocation loc = null;
				for (Entity e : GameManager.entities) {
					if (e instanceof SpawnLocation) {
						SpawnLocation l = (SpawnLocation) e;

						if (l.id == s.loc) {
							loc = l;
						}
					}
				}
				if (loc == null) {
					new Exception("No spawnlocation specified to spawn entity").printStackTrace();
					System.exit(1);
				}

				Vector2f p = new Vector2f(loc.getPosition());
				Entity e = Serializer.createEntityAt(s.data, p, null);
				GameManager.subscribeEntity(e);
			}
		}
	}

	public static ArrayList<Wave> waves;
	public static int currWave = -1;

	public static void init() {
		waves = new ArrayList<>();

		// Lazy init
		for (int i = 0; i < 5; i++) {
			ArrayList<SpawnData> spawns = new ArrayList<>();
			spawns.add(SpawnData.genNew("Bell.tx", 0));

			Wave w = new Wave(spawns);
			waves.add(w);
		}
	}

	public static ArrayList<NewWaveListener> newWaveSubList = new ArrayList<>();

	public static void sendWave() {
		System.out.println("Sending new wave");
		currWave++;

		Wave w = waves.get(currWave);
		w.release();

		for (NewWaveListener l : newWaveSubList) {
			l.onNewWave();
		}
	}

	public static void update() {
		if (currWave < waves.size() - 1) {
			boolean enemyLeft = false;

			for (Entity e : GameManager.entities) {
				if (e instanceof Enemy)
					enemyLeft = true;
			}

			if (!enemyLeft) {
				sendWave();
			}
		}
	}

}
