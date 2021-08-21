package Entities.Framework;

import org.joml.Vector2f;

import GameController.GameManager;
import Utility.Pathfinding;
import Wrappers.Stats;

public abstract class Enemy extends Combatant {

	protected Combatant target;
	protected Pathfinding ai;

	public Enemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		alignment = Alignment.ENEMY;
		findTarget();
	}

	/**
	 * TODO add actual aggro detection, using seperating axis theorem
	 * 
	 * @return
	 */
	public boolean findTarget() {
		target = GameManager.player;
		return true;
	}
}
