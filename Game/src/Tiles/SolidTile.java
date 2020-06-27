package Tiles;

import java.awt.image.BufferedImage;

import Rendering.RectRenderer;

/**
 * Example class for possible types of tiles
 * @author Benjamin
 *
 */
public abstract class SolidTile extends Tile {

	public SolidTile(int ID, BufferedImage sprite, RectRenderer renderer) {
		super(ID, sprite, renderer);
		// TODO Auto-generated constructor stub
	}

}
