package GameController.procedural;

import org.joml.Vector2i;

public class WorldRoom {
	public WorldTetromino tetromino;

	public static enum RoomStatus {
		ENTRANCE, EXIT, NONE
	}

	RoomStatus roomStatus;
	Vector2i pos;

	public WorldRoom(WorldTetromino tetromino, Vector2i pos, RoomStatus roomStatus) {
		this.tetromino = tetromino;
		this.roomStatus = roomStatus;
		this.pos = pos;
	}
}
