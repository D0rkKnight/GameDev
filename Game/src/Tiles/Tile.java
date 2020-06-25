package Tiles;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;

import GameController.Map;
import Shaders.Shader;
import Wrappers.Position;

/**
 * Tile
 * @author Benjamin
 *
 */
public abstract class Tile implements Cloneable{
	protected int ID;
	protected BufferedImage sprite;
	protected Map map;
	protected Shader shader;
	
	public Tile(int ID, BufferedImage sprite, Shader shader) {
		this.ID = ID;
		this.sprite = sprite;
		this.shader = shader;
	}
	
	//renders tile in 
	/*
	 * TODO: Remove jank and deprecated GL_QUADS drawing
	 */
	public void render(Position pos, float dim) {
		//shader.render(g, pos, sprite);
		shader.bind();
		
		glBegin(GL_QUADS);
			glVertex2f(pos.x, pos.y);
			glVertex2f(pos.x + dim, pos.y);
			glVertex2f(pos.x + dim, pos.y + dim);
			glVertex2f(pos.x, pos.y + dim);
		glEnd();
	}
	public int getID() {
		return ID;
	}
	public BufferedImage getImage() {
		return sprite;
	}
	public abstract Tile clone();
}
