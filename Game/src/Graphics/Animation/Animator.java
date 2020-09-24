package Graphics.Animation;

import Graphics.Rendering.GeneralRenderer;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;

public class Animator {

	private Timer timer;
	private long frameDelta;
	public int fps;

	private Animation[] anims;
	public Animation currentAnim;
	protected int currentAnimId;

	private GeneralRenderer rend;

	public static final int ANIM_IDLE = 0;

	public Animator(Animation[] anims, int fps, GeneralRenderer renderer) {
		this.fps = fps;
		this.frameDelta = 1000 / fps;

		this.anims = anims;
		this.currentAnimId = 0;
		this.currentAnim = anims[0];

		this.rend = renderer;

		TimerCallback cb = new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				currentAnim.nextFrame();
				rend.spr = currentAnim.getFrame();
			}

		};
		this.timer = new Timer(frameDelta, cb);
	}

	public void update() {
		timer.update();
	}

	public void switchAnim(int animId) {
		if (currentAnimId == animId) {
			System.err.println("Animation already active");
			return;
		}

		// Reset current anim
		currentAnim.resetCurrFrame();
		currentAnim = anims[animId];
		currentAnimId = animId;
	}
}
