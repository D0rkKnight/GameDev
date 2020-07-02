package Tiles;

import java.awt.image.BufferedImage;

import GameController.Map;
import Rendering.RectRenderer;
import Wrappers.Rect;
import Wrappers.Vector2;

/**
 * Tile
 * @author Benjamin
 *
 */
public abstract class Tile implements Cloneable{
	protected int ID;
	protected BufferedImage sprite;
	protected Map map;
	protected RectRenderer renderer;
	protected int hammerState; //NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when loaded in maps)
	
	public Tile(int ID, BufferedImage sprite, RectRenderer renderer) {
		this.ID = ID;
		this.sprite = sprite;
		
		//Create shallow copy
		try {
			this.renderer = renderer.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init(Vector2 pos, Rect rect) {
		this.renderer.init(pos, rect);
	}
	
	/**
	 * 
	 * @param pos: position at which to render
	 * @param dim: dimensions of tile
	 */
	public void render(Vector2 pos, float dim) {
		//shader.render(g, pos, sprite);
		renderer.linkPos(pos);;
		renderer.rect = new Rect(dim, dim);
		renderer.render();
	}
	public int getID() {
		return ID;
	}
	public BufferedImage getImage() {
		return sprite;
	}
	public void setHammerState(int hammerState) {
		this.hammerState = hammerState;
	}
	public int getHammerState() {
		return hammerState;
	}
	public abstract Tile clone();
}
