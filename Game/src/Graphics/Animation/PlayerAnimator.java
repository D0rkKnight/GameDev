package Graphics.Animation;

import Collision.Shapes.Shape;
import Entities.PlayerPackage.Player;
import Graphics.Rendering.GeneralRenderer;

/**
 * Dunno how good this implementation is, it can probably be generalized to
 * other combatants I don't feel like designing a general solution quite yet
 * though TODO
 * 
 * @author Hanzen Shou
 *
 */

public class PlayerAnimator extends Animator {

	private Player player;

	public static final int ANIM_ACCEL = 1;
	public static final int ANIM_MOVING = 2;
	public static final int ANIM_DASHING = 3;

	public PlayerAnimator(Animation[] anims, int fps, GeneralRenderer rend, Player player, Shape shape) {
		super(anims, fps, rend, shape);
		this.player = player;

		anims[ANIM_ACCEL].setCb(new AnimationCallback() {

			@Override
			public void onLoopEnd() {
				switchAnim(ANIM_MOVING);
			}

		});
	}

	@Override
	public void update() {
		super.update();

		if (player.pData.grounded && Math.abs(player.pData.velo.x) > 0 && currentAnimId == ANIM_IDLE) {
			switchAnim(ANIM_ACCEL);
		}

		else if (player.pData.grounded && Math.abs(player.pData.velo.x) == 0 && currentAnimId != ANIM_IDLE)
			switchAnim(ANIM_IDLE);
	}
}
