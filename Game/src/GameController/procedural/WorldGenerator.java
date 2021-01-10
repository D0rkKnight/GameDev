package GameController.procedural;

import java.util.HashMap;

public class WorldGenerator {

	//@formatter:off
	public static WorldTetromino[] tetrominos = new WorldTetromino[] {
		new WorldTetromino(new int[][] {
			{1, 1},
			{1, 0}
		}, true),
		new WorldTetromino(new int[][] {
			{1}
		}, true),
		new WorldTetromino(new int[][] {
			{1, 1}
		}, true),
		new WorldTetromino(new int[][] {
			{1},
			{1}
		}, true),
		new WorldTetromino(new int[][] {
			{1, 1},
			{0, 1},
			{1, 1},
			{1, 0}
		}, true),
		new WorldTetromino(new int[][] {
			{1, 1, 0, 0},
			{0, 1, 1, 1}
		}, true),
		new WorldTetromino(new int[][] {
			{1, 1, 1, 1},
			{1, 0, 0, 1},
			{1, 0, 0, 1},
			{1, 1, 1, 1}
		}, true)
	};
	//@formatter:on

	public static void init() {

	}

	public static void genWorld() {
		WorldRoom[][] board = new WorldRoom[10][10];

		// Generate start
		WorldRoom start = new WorldRoom(tetrominos[0], WorldRoom.RoomStatus.ENTRANCE);
		insertRoom(board, 0, 0, start);

		// Begin growing out horizontally
		int rIndex = (int) (Math.random() * tetrominos.length);
		WorldRoom newRoom = new WorldRoom(tetrominos[rIndex], WorldRoom.RoomStatus.NONE);
		board = growMap(board, start.tetromino.cells.length, 0, newRoom);

		printBoard(board);
	}

	/**
	 * Returns a map state. If it's referencing the same map passed in, the
	 * operation failed.
	 * 
	 * @param mapState
	 * @param x
	 * @param y
	 * @param roomToInsert
	 * @return
	 */
	public static WorldRoom[][] growMap(WorldRoom[][] mapState, int x, int y, WorldRoom roomToInsert) {

		WorldRoom[][] newMap = mapState.clone();

		boolean success = insertRoom(newMap, x, y, roomToInsert);

		if (!success) {
			System.out.println("Failure");

			// Return old map
			return mapState;
		}

		else {
			System.out.println("Success");
			printBoard(newMap);
		}

		System.out.println(roomToInsert.tetromino.w + ", " + roomToInsert.tetromino.h);

		for (int i = 0; i < 4; i++)
			System.out.println();

		// Try next generation of operations
		// Pick a room and try to insert it
		int rIndex = (int) (Math.random() * tetrominos.length);
		WorldRoom newRoom = new WorldRoom(tetrominos[rIndex], WorldRoom.RoomStatus.NONE);
		newMap = growMap(newMap, x + roomToInsert.tetromino.w, y, newRoom);

		rIndex = (int) (Math.random() * tetrominos.length);
		newRoom = new WorldRoom(tetrominos[rIndex], WorldRoom.RoomStatus.NONE);
		newMap = growMap(newMap, x, y + roomToInsert.tetromino.h, newRoom);
//		newMap = growMap(newMap, x, y, newRoom);
//		newMap = growMap(newMap, x, y, newRoom);

		return newMap;
	}

	public static boolean insertRoom(WorldRoom[][] mapState, int x, int y, WorldRoom room) {
		WorldTetromino tetro = room.tetromino;
		int[][] cellMask = tetro.cells;

		// Check if everything is in the right range
		if (x + cellMask.length > mapState.length)
			return false;
		if (y + cellMask[0].length > mapState[0].length)
			return false;

		// Check for overlap
		for (int i = 0; i < cellMask.length; i++) {
			for (int j = 0; j < cellMask[0].length; j++) {
				if (cellMask[i][j] > 0) {
					if (mapState[x + i][y + j] != null)
						return false;
				}
			}
		}

		// Insert data
		for (int i = 0; i < cellMask.length; i++) {
			for (int j = 0; j < cellMask[0].length; j++) {
				if (cellMask[i][j] > 0) {
					mapState[x + i][y + j] = room;
				}
			}
		}

		return true;
	}

	public static void printBoard(WorldRoom[][] board) {
		HashMap<WorldRoom, Character> labeler = new HashMap<>();
		char currLabel = '1';

		for (int j = 0; j < board[0].length; j++) {
			for (int i = 0; i < board.length; i++) {
				WorldRoom r = board[i][j];

				// Generate character to print
				char c = '_';
				if (r != null) {
					if (labeler.containsKey(r))
						c = labeler.get(r);
					else {
						c = currLabel;
						labeler.put(r, currLabel);

						currLabel++;
					}
				}

				System.out.print(c);
			}

			System.out.println();
		}
	}
}
