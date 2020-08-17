package Entities;


import org.joml.Vector2f;

import GameController.GameManager;
import Math.AI;
import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;
/**
 * TODO frameworks
 * @author Benjamin
 *
 */
public abstract class Enemy extends Combatant{
	
	protected Combatant target;
	protected AI ai;
	
	public Enemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		// TODO Auto-generated constructor stub
		
		alignment = ALIGNMENT_ENEMY;
		findTarget();
	}
	
	public void findTarget() {
		target = GameManager.player;
	}
}
