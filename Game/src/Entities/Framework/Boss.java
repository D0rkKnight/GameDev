package Entities.Framework;

import org.joml.Vector2f;

import Wrappers.Stats;

/**
 * Bosses are enemies with linked health bars and are always unique
 * 
 * @author Hanzen Shou
 *
 */
public class Boss extends Enemy {

	public Boss(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
	}

}