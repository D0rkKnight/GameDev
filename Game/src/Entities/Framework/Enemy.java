package Entities.Framework;

import org.joml.Vector2f;

import Collision.Hitbox;
import Entities.Player;
import GameController.GameManager;
import Graphics.Rendering.Renderer;
import Utility.Pathfinding;
import Utility.Vector;
import Wrappers.Stats;

public abstract class Enemy extends Combatant {

	protected Combatant target;
	protected Pathfinding ai;

	public Enemy(String ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);

		alignment = ALIGNMENT_ENEMY;
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

	@Override
	public void onHit(Hitbox otherHb) {
		super.onHit(otherHb);

		if (otherHb.owner instanceof Player) {
			Player p = (Player) otherHb.owner;

			if (!p.isInvuln()) {
				p.hit(10);
				p.knockback(Vector.dirTo(position, p.position), 2f, 1f);
				p.invuln();
			}
		}
	}
}
