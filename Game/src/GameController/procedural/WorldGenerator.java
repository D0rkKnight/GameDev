package GameController.procedural;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.joml.Vector2i;

public class WorldGenerator {

	//@formatter:off
	public static WorldTetromino[] tetrominos = new WorldTetromino[] {
		new WorldTetromino(
			new int[][] {
				{1, 1},
				{1, 0}
			}, true, 
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(0, 1, WorldGate.GateDir.DOWN)
			}),
		new WorldTetromino(
			new int[][] {
				{1}
			}, true, 
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(0, 0, WorldGate.GateDir.DOWN),
				new WorldGate(0, 0, WorldGate.GateDir.RIGHT)
			}),
		new WorldTetromino(
			new int[][] {
				{1, 1}
			}, true,
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(1, 0, WorldGate.GateDir.RIGHT)
			}),
		new WorldTetromino(
			new int[][] {
				{1, 1},
				{0, 1},
				{1, 1},
				{1, 0}
			}, true,
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.UP),
				new WorldGate(0, 3, WorldGate.GateDir.RIGHT)
			}),
		new WorldTetromino(
			new int[][] {
				{1, 1, 1, 1},
				{1, 0, 0, 1},
				{1, 0, 0, 1},
				{1, 1, 1, 1}
			}, true,
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(2, 0, WorldGate.GateDir.UP),
				new WorldGate(3, 2, WorldGate.GateDir.RIGHT)
			})
	};
	//@formatter:on

	static Random random;
	static long seed;

	public static void init() {
		seed = new Random().nextLong();
		// seed = 4648919169339757557L;

		random = new Random(seed);

	}

	public static void genWorld() {
		WorldRoom[][] board = new WorldRoom[20][20];

		// Generate start
		WorldRoom start = new WorldRoom(WorldTetromino.CapTet.CapFromDir(WorldGate.GateDir.RIGHT).tet,
				new Vector2i(2, 2), WorldRoom.RoomStatus.ENTRANCE);

		// Begin growing out
		board = growMap(board, null, start);

		printBoard(board);

		System.out.println("Seed: " + seed);

		// For Testing
		System.exit(0);
	}

	/**
	 * Returns a map state. If it's referencing the same map passed in, the
	 * operation failed.
	 * 
	 * @param parentState
	 * @param x
	 * @param y
	 * @param roomToInsert
	 * @return
	 */
	public static WorldRoom[][] growMap(WorldRoom[][] parentState, WorldRoom lastRoom, WorldRoom roomToInsert) {
		// 2D array copy
		WorldRoom[][] localState = new WorldRoom[parentState.length][parentState[0].length];
		for (int i = 0; i < localState.length; i++) {
			localState[i] = parentState[i].clone();
		}

		// Perform local operations
		// Begin by trying to place next room
		boolean success = insertRoom(localState, roomToInsert);

		System.out.println("\n\n\nBoard after room insertion:");
		printBoard(localState);

		// Place caps on last room as part of this operation
		if (lastRoom != null) {
			WorldGate[] lastRoomGates = lastRoom.tetromino.gates;
			for (WorldGate g : lastRoomGates) {
				Vector2i gateExit = new Vector2i(lastRoom.pos).add(g.pos);
				Vector2i capPos = new Vector2i(gateExit).add(g.dir.getFaceDelta());

				// Check bounds
				if (capPos.x < 0 || capPos.x >= localState.length || capPos.y < 0 || capPos.y >= localState[0].length)
					continue;

				// Check if any tiles are already connected
				if (localState[capPos.x][capPos.y] != null) {
					WorldRoom facingRoom = localState[capPos.x][capPos.y];

					boolean isAlreadyLinked = false;
					for (WorldGate fg : facingRoom.tetromino.gates) {
						Vector2i fgLoc = new Vector2i(facingRoom.pos).add(fg.pos);

						if (fgLoc.equals(capPos) && fg.dir == g.dir.getOpposing()) {
							// Issok, gates are linked properly here
							isAlreadyLinked = true;
							break;
						}
					}

					if (isAlreadyLinked)
						continue; // Ignore this gate if it's already linked
				}

				// Attempt to cap the openings of the last room.
				WorldTetromino cap = WorldTetromino.CapTet.CapFromDir(g.dir.getOpposing()).tet;
				WorldRoom capRoom = new WorldRoom(cap, capPos, WorldRoom.RoomStatus.NONE);
				boolean capInserted = insertRoom(localState, capRoom);

				// If failure, backtrack
				if (!capInserted) {
					System.out.println("Can't place cap");
					System.out.println("Gate position: " + g.pos);

					success = false;
					break;
				}
			}
		}

		System.out.println("\n\n\nBoard after cap insertion:");
		printBoard(localState);

		if (!success) {
			System.out.println("Failure");

			// Return old map
			return parentState;
		}

		// Exit condition! (If pressed to right wall)
		boolean shouldExit = roomToInsert.pos.x + roomToInsert.tetromino.w >= parentState.length;
		if (shouldExit) {
			System.out.println("Natural termination");
			return localState;
		}

		// Call subsequent operations
		// Get all exits and match
		WorldGate[] insertedRoomGates = roomToInsert.tetromino.gates;
		Integer[] gateIndexes = createRandomIndexing(insertedRoomGates.length);

		// Maybe all this goes into another function...
		for (int i = 0; i < gateIndexes.length; i++) {
			WorldGate entrance = insertedRoomGates[gateIndexes[i]];
			Vector2i exitCellPos = new Vector2i(roomToInsert.pos).add(entrance.pos).add(entrance.dir.getFaceDelta());

			// Check bounds
			if (exitCellPos.x < 0 || exitCellPos.x >= localState.length || exitCellPos.y < 0
					|| exitCellPos.y >= localState[0].length)
				continue;

			// Generate random indexing of array
			Integer[] roomIndexes = createRandomIndexing(tetrominos.length);

			// Try every piece, now that they have been randomly ordered
			for (int j = 0; j < roomIndexes.length; j++) {
				int ind = roomIndexes[j];
				WorldTetromino tet = tetrominos[ind];

				// Find a gate to align the tetromino against
				Integer[] exitIndexes = createRandomIndexing(tet.gates.length);
				for (int k = 0; k < exitIndexes.length; k++) {
					WorldGate exit = tet.gates[exitIndexes[k]];
					if (entrance.dir.getOpposing() != exit.dir)
						continue; // Skip if not aligned

					Vector2i newRoomAnchor = new Vector2i(exitCellPos).sub(exit.pos);
					// Check bounds
					if (newRoomAnchor.x < 0 || newRoomAnchor.x >= localState.length || newRoomAnchor.y < 0
							|| newRoomAnchor.y >= localState[0].length)
						continue;

					WorldRoom newRoom = new WorldRoom(tetrominos[ind], newRoomAnchor, WorldRoom.RoomStatus.NONE);

					WorldRoom[][] out = growMap(localState, roomToInsert, newRoom);

					// Check if success or not
					if (out != localState) {
						System.out.println("Success, passing up");
						return out; // Single pathway generation
					}
				}
			}
		}

		System.out.println("No valid exit, throwing");
		return parentState;
	}

	public static boolean insertRoom(WorldRoom[][] mapState, WorldRoom room) {
		WorldTetromino tetro = room.tetromino;
		int[][] cellMask = tetro.cells;

		int x = room.pos.x;
		int y = room.pos.y;

		// Check if everything is in the right range
		if (x + cellMask.length > mapState.length || x < 0)
			return false;
		if (y + cellMask[0].length > mapState[0].length || y < 0)
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
		HashMap<WorldTetromino, Character> labeler = new HashMap<>();
		char currLabel = '1';

		for (WorldTetromino t : tetrominos) {
			labeler.put(t, currLabel);
			currLabel++;
		}

		for (WorldTetromino.CapTet cap : WorldTetromino.CapTet.values()) {
			labeler.put(cap.tet, currLabel);
			currLabel++;
		}

		for (int j = 0; j < board[0].length; j++) {
			for (int i = 0; i < board.length; i++) {
				WorldRoom r = board[i][j];

				// Generate character to print
				char c = '_';
				if (r != null) {
					WorldTetromino t = r.tetromino;
					c = labeler.get(t);
				}

				System.out.print(c);
			}

			System.out.println();
		}
	}

	/**
	 * Creates an array of indexes to randomly access another array.
	 * 
	 * @param length
	 * @return
	 */
	public static Integer[] createRandomIndexing(int length) {
		Integer[] indexes = new Integer[length];
		for (int i = 0; i < indexes.length; i++)
			indexes[i] = i;
		List<Integer> indexesList = Arrays.asList(indexes);
		Collections.shuffle(indexesList, random);
		indexes = (Integer[]) indexesList.toArray();

		return indexes;
	}
}
