package Entities.Framework;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Hitbox;
import Entities.Behavior.EntityFlippable;
import Entities.Framework.StateMachine.ECB;
import Entities.Framework.StateMachine.StateID;
import Entities.Framework.StateMachine.StateTag;
import GameController.GameManager;
import GameController.Time;
import Utility.Arithmetic;
import Utility.Pathfinding;
import Utility.Timers.Timer;
import Wrappers.FrameData;
import Wrappers.FrameData.FrameSegment;
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

	@Override
	protected void assignFD() {
		super.assignFD();

		addFD(StateID.STUNNED, genSTUNNED());
	}

	protected ECB genMOVETag(EntityFlippable flip) {
		return genMOVETag(flip, 1, 700, 100);
	}

	/**
	 * Generates a move with a delayed turnaround
	 */
	protected ECB genMOVETag(EntityFlippable flip, float maxVelo, long switchDelay, int pursueThresh) {
		return () -> {

			// Pursue player
			if (target == null)
				findTarget();

			if (target != null) {
				Vector2f tVec = new Vector2f(target.globalCenter()).sub(globalCenter());

				// Handle pauses when switching side faced
				int newSideFacing = Arithmetic.sign(tVec.x);
				if (newSideFacing != flip.sideFacing && flip.sideSwitchTimer == null) {
					flip.sideSwitchTimer = new Timer(switchDelay, (t) -> {
						flip.sideSwitchTimer = null;
						flip.sideFacing = newSideFacing;
					});
				} else if (newSideFacing == flip.sideFacing)
					flip.sideSwitchTimer = null; // Short circuit if facing the right side

				if (flip.sideSwitchTimer != null)
					flip.sideSwitchTimer.update();

				if (Math.abs(tVec.x) > pursueThresh && flip.sideSwitchTimer == null) {
					Vector2f v = pData.velo;

					v.x = Arithmetic.lerp(v.x, maxVelo * flip.sideFacing, 3f * Time.deltaT() / 1000f);
				} else if (Math.abs(tVec.x) <= pursueThresh && flip.sideSwitchTimer == null) {
					// Attack
					// TODO: I should probably just write this as a state machine
				}
			}
		};
	}

	protected FrameData genMOVE() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(getTagCB(StateTag.MOVEABLE)));

		FrameData fd = new FrameData(segs, null, true);
		fd.onEntry = () -> anim.switchAnim(StateTag.IDLE);

		return fd;
	}

	protected FrameData genSTUNNED() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(50, 0));

		FrameData fd = new FrameData(segs, null, false);
		fd.onEnd = () -> setEntityFD(StateID.MOVE);
		fd.onEntry = () -> {
			if (anim != null)
				anim.switchAnim(StateTag.IDLE); // No stunned animation yet
		};

		return fd;
	}

	@Override
	public void hurtBy(Hitbox other) {
		Aligned otherOwner = (Aligned) other.owner;
		if (Combatant.getOpposingAlignment(otherOwner.getAlign()) == alignment) {
			setEntityFD(StateID.STUNNED);
		}
	}
}
