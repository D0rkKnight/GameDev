package GameController;
import java.util.ArrayList;

import org.joml.Vector2f;

import Entities.Entity;
import Tiles.Tile;
public class Map {
	private Tile[][] grid; //[x][y]
	/*
	 * entrance coordinates, contains list of positions [topleft, topright, botleft, botright]
	 */
	private Vector2f[][] entrances; 
	/*
	 * entrance info, contains list of entrance info [entranceID, entrancethatitlinkstoID]
	 */
	private int[][] entranceInfo;
	/*
	 * list of entities in the room. does not include player, accessed by GameManager to determine collisions
	 */
	private ArrayList<Entity> entities;
	public Map(Tile[][] mapData, Vector2f[][] entrances, int[][] entranceInfo, ArrayList<Entity> entities) {
		grid = mapData;
		this.setEntrances(entrances);
		this.setEntranceInfo(entranceInfo);
		this.setEntities(entities);
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
	public Vector2f[][] getEntrances() {
		return entrances;
	}
	private void setEntrances(Vector2f[][] entrances) {
		this.entrances = entrances;
	}
	public int[][] getEntranceInfo() {
		return entranceInfo;
	}
	private void setEntranceInfo(int[][] entranceInfo) {
		this.entranceInfo = entranceInfo;
	}
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}
}
