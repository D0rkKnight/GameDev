package GameController.procedural;

import org.joml.Vector2i;

public class WorldRoom {
	public WorldTetromino tetromino;
	public WorldGate[] gates;

	public static enum RoomStatus {
		ENTRANCE, EXIT, NONE
	}

	RoomStatus roomStatus;
	Vector2i pos;

	public WorldRoom(WorldTetromino tetromino, Vector2i pos, RoomStatus roomStatus) {
		this.tetromino = tetromino;
		this.roomStatus = roomStatus;
		this.pos = pos;

		gates = new WorldGate[tetromino.gates.length];
		for (int i = 0; i < gates.length; i++) {
			gates[i] = new WorldGate(tetromino.gates[i], this);
		}
	}
}
