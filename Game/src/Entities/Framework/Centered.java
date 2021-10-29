package Entities.Framework;

import org.joml.Vector2f;

import Utility.Center;

public interface Centered {
	public Vector2f globalCenter();

	public Center center();
}
