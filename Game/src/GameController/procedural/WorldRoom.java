package GameController.procedural;

import java.util.ArrayList;

import org.joml.Vector2i;

import GameController.Map;

public class WorldRoom {
	public WorldTetromino tetromino;
	public WorldGate[] gates;

	public static enum RoomStatus {
		ENTRANCE, EXIT, NONE
	}

	RoomStatus roomStatus;
	Vector2i pos;
	public Map map;

	public WorldRoom(WorldTetromino tetromino, Vector2i pos, RoomStatus roomStatus) {
		this.tetromino = tetromino;
		this.roomStatus = roomStatus;
		this.pos = pos;

		gates = new WorldGate[tetromino.gates.length];
		for (int i = 0; i < gates.length; i++) {
			gates[i] = new WorldGate(tetromino.gates[i], this);
		}
	}

	/*
	 * has time complexity of n
	 */
	public ArrayList<WorldGate> getGatesAtWorldPos(int x, int y) {
		Vector2i roomSpacePos = new Vector2i(x, y).sub(pos);

		ArrayList<WorldGate> out = new ArrayList<>();

		for (WorldGate gate : gates) {
			if (gate.localPos.equals(roomSpacePos)) {
				out.add(gate);
			}
		}

		return out;
	}
}
