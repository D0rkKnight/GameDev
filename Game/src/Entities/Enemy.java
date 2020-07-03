package Entities;

import Rendering.Renderer;
import Wrappers.Vector2;
import Wrappers.Sprites;
import Wrappers.Stats;
/**
 * TODO frameworks
 * @author Benjamin
 *
 */
public abstract class Enemy extends Combatant{

	public Enemy(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats) {
		super(ID, position, sprites, renderer, name, stats);
		// TODO Auto-generated constructor stub
	}

	
	
}
