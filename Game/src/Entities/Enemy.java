package Entities;

import Rendering.RectRenderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;
/**
 * TODO frameworks
 * @author Benjamin
 *
 */
public abstract class Enemy extends Combatant{

	public Enemy(int ID, Vector2 position, Sprites sprites, RectRenderer renderer, Stats stats) {
		super(ID, position, sprites, renderer, stats);
		// TODO Auto-generated constructor stub
	}

	
	
}
