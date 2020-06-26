package GameController;
import Tiles.Tile;
import Wrappers.Position;
public class Map {
	private Tile[][] grid; //[x][y]
	/*
	 * entrance coordinates, contains list of positions [topleft, topright, botleft, botright]
	 */
	private Position[][] entrances; 
	/*
	 * entrance info, contains list of entrance info [entranceID, entrancethatitlinkstoID]
	 */
	private int[][] entranceInfo;
	public Map(Tile[][] mapData, Position[][] entrances, int[][] entranceInfo) {
		grid = mapData;
		this.entrances = entrances;
		this.entranceInfo = entranceInfo;
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
