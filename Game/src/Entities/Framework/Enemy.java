package Entities.Framework;


import org.joml.Vector2f;

import GameController.GameManager;
import Graphics.Elements.Sprites;
import Graphics.Rendering.Renderer;
import Utility.Pathfinding;
import Wrappers.Stats;
/**
 * TODO frameworks
 * @author Benjamin
 *
 */
public abstract class Enemy extends Combatant {
	
	protected Combatant target;
	protected Pathfinding ai;
	
	public Enemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		// TODO Auto-generated constructor stub
		
		alignment = ALIGNMENT_ENEMY;
		findTarget();
	}
	
	/**
	 * TODO add actual aggro detection, using seperating axis theorem
	 * @return
	 */
	public boolean findTarget() {
		target = GameManager.player;
		return true;
	}
}
