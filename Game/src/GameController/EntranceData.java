package GameController;

import org.joml.Vector2i;

import GameController.procedural.WorldGate;

public class EntranceData {
	Vector2i mapPos;
	WorldGate.GateDir dir;
	Map map;

	public EntranceData(Map map, Vector2i mapPos, WorldGate.GateDir dir) {
		this.mapPos = mapPos;
		this.dir = dir;
		this.map = map;
	}
}
