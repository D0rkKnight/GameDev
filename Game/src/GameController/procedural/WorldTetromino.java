package GameController.procedural;

public class WorldTetromino {

	int w;
	int h;
	int[][] cells;

	WorldGate[] gates;

	public static enum CapTet {

		UP(WorldGate.GateDir.UP), DOWN(WorldGate.GateDir.DOWN), LEFT(WorldGate.GateDir.LEFT),
		RIGHT(WorldGate.GateDir.RIGHT);

		public WorldTetromino tet;

		CapTet(WorldGate.GateDir dir) {
			// Create a 1x1 tetromino that faces the inputted direction.
			tet = new WorldTetromino(new int[][] { { 1 } }, false, new WorldGate[] { new WorldGate(0, 0, dir) });
		}

		public static CapTet CapFromDir(WorldGate.GateDir dir) {
			switch (dir) {
			case UP:
				return UP;
			case DOWN:
				return DOWN;
			case LEFT:
				return LEFT;
			case RIGHT:
				return RIGHT;
			default:
				return null;
			}
		}
	}

	public WorldTetromino(int[][] cellMask, boolean flip, WorldGate[] gates) {
		cells = null;

		// Rotate mask coming in
		if (flip) {
			cells = new int[cellMask[0].length][cellMask.length];
			for (int i = 0; i < cellMask.length; i++) {
				for (int j = 0; j < cellMask[0].length; j++) {
					cells[j][i] = cellMask[i][j]; // Flip
				}
			}
		} else {
			cells = cellMask;
		}

		// Search for gates
		this.w = cells.length;
		this.h = cells[0].length;

		this.gates = gates;
	}
}
