package Tiles;

import java.awt.image.BufferedImage;

import Collision.HammerShape;
import GameController.GameManager;
import GameController.Map;
import Rendering.SpriteRenderer;
import Wrappers.Color;
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
	protected SpriteRenderer renderer;
	protected HammerShape hammerState; //NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when loaded in maps)
	
	public static final int CORNER_NULL = -1;
	public static final int CORNER_UL = 0;
	public static final int CORNER_UR = 1;
	public static final int CORNER_BL = 2;
	public static final int CORNER_BR = 3;
	
	public Tile(int ID, BufferedImage sprite, SpriteRenderer renderer) {
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
		if (hammerState == null) {
			System.out.println("Hammer state not specified, capitulating to default.");
			hammerState = GameManager.hammerLookup.get(HammerShape.HAMMER_SHAPE_SQUARE);
		}
		
		this.renderer.init(pos, rect, hammerState.shapeId, new Color(1, 1, 1));
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
	public void setHammerState(HammerShape hammerState) {
		this.hammerState = hammerState;
	}
	public HammerShape getHammerState() {
		return hammerState;
	}
	
	@Override
	public Tile clone() throws CloneNotSupportedException {
		Tile t =  (Tile) super.clone();
		//New renderer please
		t.renderer = this.renderer.clone();
		
		return t;
	}
}
