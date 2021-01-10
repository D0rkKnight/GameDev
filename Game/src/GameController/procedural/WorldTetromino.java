package GameController.procedural;

import java.util.LinkedHashMap;

import org.joml.Vector2i;

public class WorldTetromino {

	int w;
	int h;
	int[][] cells;

	public static enum GateDir {
		UP, DOWN, LEFT, RIGHT;
	}

	LinkedHashMap<Vector2i, GateDir> gates;

	public WorldTetromino(int[][] cellMask, boolean flip) {
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
		gates = new LinkedHashMap<>();
	}

	public void addEntrance(int x, int y, GateDir dir) {
		gates.put(new Vector2i(x, y), dir);
	}
}
