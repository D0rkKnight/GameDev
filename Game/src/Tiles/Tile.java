package Tiles;

import GameController.Map;
import java.awt.Image;

/**
 * Tile
 * @author Benjamin
 *
 */
public abstract class Tile {
	private int ID;
	private Image sprite;
	private Map map;
	
	public Tile(int ID, Image sprite, Map map) {
		this.ID = ID;
		this.sprite = sprite;
		this.map = map;
	}
	
	//renders tile in 
	public abstract void render();
	public abstract int getID();
	public abstract Image getImage();
}
