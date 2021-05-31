package GameController.procedural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.joml.Vector2i;

import GameController.EntranceData;
import GameController.Map;
import GameController.World;

public class WorldGenerator {

	//@formatter:off
	public static WorldTetromino[] tetrominos = new WorldTetromino[] {
		new WorldTetromino(
			new int[][] {
				{1, 1},
				{1, 1}
			}, true, 
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(1, 1, WorldGate.GateDir.RIGHT)
			}),
		new WorldTetromino(
			new int[][] {
				{1}
			}, true, 
			new WorldGate[]{
				new WorldGate(0, 0, WorldGate.GateDir.LEFT),
				new WorldGate(0, 0, WorldGate.GateDir.DOWN),
				new WorldGate(0, 0, WorldGate.GateDir.RIGHT)
			})//,
//		new WorldTetromino(
//			new int[][] {
//				{1},
//				{1}
//			}, true,
//			new WorldGate[]{
//				new WorldGate(0, 0, WorldGate.GateDir.UP),
//				new WorldGate(0, 1, WorldGate.GateDir.DOWN)
//			}),
//		new WorldTetromino(
//				new int[][] {
//					{1, 1}
//				}, true,
//				new WorldGate[]{
//					new WorldGate(0, 0, WorldGate.GateDir.UP),
//					new WorldGate(1, 0, WorldGate.GateDir.RIGHT)
//				}),
//		new WorldTetromino(
//				new int[][] {
//					{1, 1},
//					{1, 1}
//				}, true,
//				new WorldGate[]{
//					new WorldGate(0, 0, WorldGate.GateDir.LEFT),
//					new WorldGate(1, 0, WorldGate.GateDir.UP),
//					new WorldGate(1, 1, WorldGate.GateDir.RIGHT)
//				})
	};
	//@formatter:on

	static HashMap<WorldTetromino, String> tetrominoMapLookup;

	static Random random;
	static long seed;

	public static void init() {
		// Random generator
		seed = new Random().nextLong();
		// seed = 5779994054622002424L;
		random = new Random(seed);

		// Initiate tetromino map pairings, necessary because caps need to be mapped
		// too.
		tetrominoMapLookup = new HashMap<>();
		tetrominoMapLookup.put(tetrominos[0], "assets/Maps/Forest/forest1.tmx");
		tetrominoMapLookup.put(tetrominos[1], "assets/Maps/Forest/forest2.tmx");

		tetrominoMapLookup.put(WorldTetromino.CapTet.RIGHT.tet, "assets/Maps/Forest/forestE.tmx");
		tetrominoMapLookup.put(WorldTetromino.CapTet.LEFT.tet, "assets/Maps/Forest/forestX.tmx");
		tetrominoMapLookup.put(WorldTetromino.CapTet.UP.tet, "assets/Maps/Forest/forestUC.tmx");
		tetrominoMapLookup.put(WorldTetromino.CapTet.DOWN.tet, "assets/Maps/Forest/forestDC.tmx");

		genWorld();
	}

	public static void genWorld() {
		WorldRoom[][] board = new WorldRoom[10][10];

		// Generate start
		WorldRoom start = new WorldRoom(WorldTetromino.CapTet.CapFromDir(WorldGate.GateDir.RIGHT).tet,
				new Vector2i(0, 0), WorldRoom.RoomStatus.ENTRANCE);

		// Begin growing out
		board = growMap(board, null, start);

		populateWithMaps(board);

		// printBoard(board);

		System.out.println("Seed: " + seed);

		linkGates(board);

		// Load in board
		World.currmap = start.map;
	}

	/**
	 * Returns a map state. Returns null if operation failed.
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

		// Place caps on last room as part of this operation
		// TODO: Make sure dungeon has no loops
		if (lastRoom != null) {
			WorldRoom[][] capOutput = capRoom(localState, lastRoom);
			if (capOutput == null)
				success = false;
			else
				localState = capOutput;
		}

		if (!success) {
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
			return localState;
		}

		// Call subsequent operations
		// Get all exits and match
		WorldGate[] insertedRoomGates = roomToInsert.tetromino.gates;
		Integer[] gateIndexes = createRandomIndexing(insertedRoomGates.length);

		// Maybe all this goes into another function...
		for (int i = 0; i < gateIndexes.length; i++) {
			WorldGate entrance = insertedRoomGates[gateIndexes[i]];
			Vector2i exitCellPos = new Vector2i(roomToInsert.pos).add(entrance.localPos)
					.add(entrance.dir.getFaceDelta()); // TODO: Use WorldGate.getOpposingLoc() instead

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

					Vector2i newRoomAnchor = new Vector2i(exitCellPos).sub(exit.localPos);
					// Check bounds
					if (checkBounds(newRoomAnchor, localState))
						continue;

					WorldRoom newRoom = new WorldRoom(tetrominos[ind], newRoomAnchor, WorldRoom.RoomStatus.NONE);

					WorldRoom[][] out = growMap(localState, roomToInsert, newRoom);

					// Check if success or not
					if (out != null) {
						return out; // Single pathway generation
					}
				}
			}
		}

		return null;
	}

	/**
	 * Operates on given board
	 * 
	 * @param board
	 */
	public static void linkGates(WorldRoom[][] board) {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[0].length; y++) {
				// Pull relevant board
				WorldRoom room = board[x][y];

				if (room == null)
					continue;

				ArrayList<WorldGate> r1Gates = room.getGatesAtWorldPos(x, y);
				if (r1Gates.isEmpty())
					continue;

				for (WorldGate r1g : r1Gates) {
					Vector2i r1GOppLoc = r1g.getOpposingLoc();

					WorldRoom oppRoom = board[r1GOppLoc.x][r1GOppLoc.y];

					if (oppRoom == null) {
						System.err.println("Gate pointing towards nowhere.");
						System.exit(1);
					}

					ArrayList<WorldGate> r2Gates = oppRoom.getGatesAtWorldPos(r1GOppLoc.x, r1GOppLoc.y);
					if (r2Gates.isEmpty()) {
						System.err.println("Opposing room does now own gate at coordinate");
						System.exit(1);
					}

					// Find target gate
					WorldGate r2g = null;
					for (WorldGate tempG : r2Gates) {
						if (tempG.dir.getOpposing() == r1g.dir) {
							r2g = tempG;
							break;
						}
					}

					r1g.linkedGate = r2g;

					// Set link one way since it'll get linked the other way when the loop reaches
					// the opposing gate.
					EntranceData start = new EntranceData(room.map, r1g.localPos, r1g.dir);
					EntranceData end = new EntranceData(oppRoom.map, r2g.localPos, r2g.dir);
					room.map.setEntranceLink(start, end);
				}
			}
		}
	}

	public static void populateWithMaps(WorldRoom[][] board) {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[0].length; y++) {
				// Pull relevant board
				WorldRoom room = board[x][y];

				if (room == null || room.map != null)
					continue;

				String mapUrl = tetrominoMapLookup.get(room.tetromino);

				Map map = World.genMap(mapUrl);
				room.map = map;
			}
		}
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
			Vector2i gateExit = new Vector2i(room.pos).add(g.localPos);
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
					Vector2i fgLoc = new Vector2i(facingRoom.pos).add(fg.localPos);

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
		indexes = indexesList.toArray(new Integer[length]);

		return indexes;
	}

	public static boolean checkBounds(Vector2i v, Object[][] arr) {
		return checkBounds(v.x, v.y, arr);
	}

	public static boolean checkBounds(int x, int y, Object[][] arr) {
		return x < 0 || x >= arr.length || y < 0 || y >= arr[0].length;
	}
}
