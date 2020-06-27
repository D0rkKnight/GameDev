package Entities;

import Rendering.Shader;
import Wrappers.Position;
import Wrappers.Sprites;
import Wrappers.Stats;
/**
 * TODO frameworks
 * @author Benjamin
 *
 */
public abstract class Enemy extends Combatant{

	public Enemy(int ID, Position position, Sprites sprites, Shader shader, Stats stats) {
		super(ID, position, sprites, shader, stats);
		// TODO Auto-generated constructor stub
	}

}
