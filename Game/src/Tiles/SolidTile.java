package Tiles;

import java.awt.image.BufferedImage;

import Collision.Collidable;
import Rendering.SpriteRenderer;
import Wrappers.Hitbox;

/**
 * Example class for possible types of tiles
 * @author Benjamin
 *
 */
public abstract class SolidTile extends Tile{

	
	public SolidTile(int ID, BufferedImage sprite, SpriteRenderer renderer) {
		super(ID, sprite, renderer);
		// TODO Auto-generated constructor stub
	}
	
}
