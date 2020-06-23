package Shaders;

import java.awt.Graphics;
import java.awt.Image;

import Wrappers.Position;

/*
 * Renders a sprite at a location on screen.
 */
public class SpriteShader extends Shader{
	
	public void render(Graphics g, Position pos, Image spr) {
		// TODO Auto-generated method stub
		System.out.println("rendering");
		
		g.drawImage(spr,  pos.x,  pos.y, null);
	}
}
