package Shaders;

import java.awt.Graphics;
import java.awt.Image;

import Wrappers.Position;

/*
 * Only one copy of these should exist for every distinct shader.
 * Its job is to render the object, given what to render and the graphics object.
 */
public abstract class Shader {
	public abstract void render(Graphics g, Position pos, Image spr);
}
