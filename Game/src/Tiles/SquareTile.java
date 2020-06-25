package Tiles;

import java.awt.image.BufferedImage;

import Shaders.Shader;

public class SquareTile extends SolidTile{

	public SquareTile(int ID, BufferedImage sprite, Shader shader) {
		super(ID, sprite, shader);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Tile clone() {
		// TODO Auto-generated method stub
		SquareTile out = new SquareTile(ID, sprite, shader);
		return out;
	}

}
