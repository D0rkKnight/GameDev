package Tiles;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import GameController.Map;
import Shaders.Shader;
import Wrappers.Position;

/**
 * Tile
 * @author Benjamin
 *
 */
public abstract class Tile {
	private int ID;
	private BufferedImage sprite;
	private Map map;
	private Shader shader;
	
	public Tile(int ID, BufferedImage sprite, Shader shader) {
		this.ID = ID;
		this.sprite = sprite;
		this.shader = shader;
	}
	
	//renders tile in 
	public void render(Graphics g, Position pos) {
		shader.render(g, pos, sprite);
	}
	public int getID() {
		return ID;
	}
	public BufferedImage getImage() {
		return sprite;
	}
}
