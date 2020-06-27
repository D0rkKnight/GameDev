package Tiles;

import java.awt.image.BufferedImage;

import Rendering.Shader;

/**
 * Example class for possible types of tiles
 * @author Benjamin
 *
 */
public abstract class SolidTile extends Tile {

	public SolidTile(int ID, BufferedImage sprite, Shader shader) {
		super(ID, sprite, shader);
		// TODO Auto-generated constructor stub
	}

}
