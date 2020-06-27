package Tiles;

import java.awt.image.BufferedImage;

import Rendering.RectRenderer;

public class SquareTile extends SolidTile{

	public SquareTile(int ID, BufferedImage sprite, RectRenderer renderer) {
		super(ID, sprite, renderer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Tile clone() {
		// TODO Auto-generated method stub
		SquareTile out = new SquareTile(ID, sprite, renderer);
		return out;
	}

}
