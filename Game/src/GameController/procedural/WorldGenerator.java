package GameController.procedural;

import java.util.ArrayList;
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
				{1},
				{1}
			}, true,
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.UP),
				new WorldGate(0, 1, WorldGate.GateDir.DOWN)
			}),
		new WorldTetromino(
				new int[][] {
					{1, 1}
				}, true,
				new WorldGate[]{
					new WorldGate(0, 0, WorldGate.GateDir.UP),
					new WorldGate(1, 0, WorldGate.GateDir.RIGHT)
				}),
		new WorldTetromino(
				new int[][] {
					{1, 1},
					{1, 1}
				}, true,
				new WorldGate[]{
					new WorldGate(0, 0, WorldGate.GateDir.LEFT),
					new WorldGate(1, 0, WorldGate.GateDir.UP),
					new WorldGate(1, 1, WorldGate.GateDir.RIGHT)
				})
	};
	//@formatter:on

	static Random random;
	static long seed;

	public static void init() {
		seed = new Random().nextLong();
		// seed = 5189681989908125063L;

		random = new Random(seed);

	}

	public static void genWorld() {
		WorldRoom[][] board = new WorldRoom[10][10];

		// Generate start
		WorldRoom start = new WorldRoom(WorldTetromino.CapTet.CapFromDir(WorldGate.GateDir.RIGHT).tet,
				new Vector2i(2, 2), WorldRoom.RoomStatus.ENTRANCE);

		// Begin growing out
		board = growMap(board, null, start);

		printBoard(board);

		System.out.println("Seed: " + seed);
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
		WorldRoom[][] localState = duplicateBoard(parentState);

		// Perform local operations
		// Begin by trying to place next room
		boolean success = insertRoom(localState, roomToInsert);

		System.out.println("\n\n\nBoard after room insertion:");
		printBoard(localState);

		// Place caps on last room as part of this operation
		// TODO: Make sure dungeon has no loops
		if (lastRoom != null) {
			WorldRoom[][] capOutput = capRoom(localState, lastRoom);
			if (capOutput == null)
				success = false;
			else
				localState = capOutput;
		}

		System.out.println("\n\n\nBoard after cap insertion:");
		printBoard(localState);

		if (!success) {
			System.out.println("Failure");

			// Return failure
			return null;
		}

		// Exit condition! (If pressed to right wall)
		boolean atEdge = roomToInsert.pos.x + roomToInsert.tetromino.w >= parentState.length - 2
				|| roomToInsert.pos.y + roomToInsert.tetromino.h >= parentState[0].length - 2; // -2 creates padding on
																								// edge
		if (atEdge) {
			// Try to add caps
			ArrayList<WorldRoom> caps = new ArrayList<>();
			WorldRoom[][] capOutput = capRoom(localState, roomToInsert, caps);
			if (capOutput == null)
				// Failure to cap, throw
				return null;
			else
				localState = capOutput;

			// Pick one cap room to set as the exit
			int randIndex = (int) (random.nextFloat() * caps.size());
			WorldRoom exit = caps.get(randIndex);
			exit.roomStatus = WorldRoom.RoomStatus.EXIT;

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
			if (checkBounds(exitCellPos, localState))
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
					if (checkBounds(newRoomAnchor, localState))
						continue;

					WorldRoom newRoom = new WorldRoom(tetrominos[ind], newRoomAnchor, WorldRoom.RoomStatus.NONE);

					WorldRoom[][] out = growMap(localState, roomToInsert, newRoom);

					// Check if success or not
					if (out != null) {
						System.out.println("Success, passing up");
						return out; // Single pathway generation
					}
				}
			}
		}

		System.out.println("No valid exit, throwing");
		return null;
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

	public static WorldRoom[][] capRoom(WorldRoom[][] parentBoard, WorldRoom room) {
		return capRoom(parentBoard, room, null);
	}

	public static WorldRoom[][] capRoom(WorldRoom[][] parentBoard, WorldRoom room, ArrayList<WorldRoom> caps) {
		// Duplicate room
		WorldRoom[][] board = duplicateBoard(parentBoard);

		// TODO: Make sure dungeon has no loops
		WorldGate[] gates = room.tetromino.gates;
		boolean success = true;

		for (WorldGate g : gates) {
			Vector2i gateExit = new Vector2i(room.pos).add(g.pos);
			Vector2i capPos = new Vector2i(gateExit).add(g.dir.getFaceDelta());

			// Check bounds
			if (checkBounds(capPos, board)) {
				success = false;
				break;
			}

			// Check if any tiles are already connected
			if (board[capPos.x][capPos.y] != null) {
				WorldRoom facingRoom = board[capPos.x][capPos.y];

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
			boolean capInserted = insertRoom(board, capRoom);

			if (caps != null)
				caps.add(capRoom); // For outputting successful caps

			// If failure, backtrack
			if (!capInserted) {
				success = false;
				break;

			}
		}

		if (!success) {
			if (caps != null)
				caps.clear();
			return null;
		} else
			return board;
	}

	private static WorldRoom[][] duplicateBoard(WorldRoom[][] board) {
		WorldRoom[][] out = new WorldRoom[board.length][board[0].length];
		for (int i = 0; i < board.length; i++) {
			out[i] = board[i].clone();
		}

		return out;
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

					if (r.roomStatus == WorldRoom.RoomStatus.ENTRANCE)
						c = 'E';
					if (r.roomStatus == WorldRoom.RoomStatus.EXIT)
						c = 'X';
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

	public static boolean checkBounds(Vector2i v, Object[][] arr) {
		return checkBounds(v.x, v.y, arr);
	}

	public static boolean checkBounds(int x, int y, Object[][] arr) {
		return x < 0 || x >= arr.length || y < 0 || y >= arr[0].length;
	}
}
