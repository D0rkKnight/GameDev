import java.util.Arrays;

import Tiles.Tile;
public class Map {
	private Tile[][] grid; //[x][y]
	public Map(Tile[][] mapData) {
		grid = mapData;
	}
	public Tile[][] getGrid(){
		return grid;
	}
	public Tile[][] getSurroundingArea(int xLeft, int yTop, int xRight, int yBot){
		if(xLeft < 0) {
			xLeft = 0;
		}
		if(yTop < 0) {
			yTop = 0;
		}
		if(xRight >= grid.length) {
		xRight = grid.length - 1;
		}
		if(yBot > grid[0].length) {
			yBot = grid[0].length;
		}
		Tile[][] subgrid = new Tile[xRight - xLeft][yBot - yTop];
		for(int x = 0; xLeft < xRight; xLeft++, x++ ) {
			for(int y = 0;yTop < yBot; yTop++, y++) {
				subgrid[x][y] = grid[xLeft][yTop];
			}
		}
		return subgrid;
	}
}
