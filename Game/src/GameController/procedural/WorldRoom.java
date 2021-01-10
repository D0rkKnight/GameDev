package GameController.procedural;

public class WorldRoom {
	public WorldTetromino tetromino;

	public static enum RoomStatus {
		ENTRANCE, EXIT, NONE
	}

	RoomStatus roomStatus;

	public WorldRoom(WorldTetromino tetromino, RoomStatus roomStatus) {
		this.tetromino = tetromino;
		this.roomStatus = roomStatus;
	}
}
