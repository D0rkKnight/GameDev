package Graphics.Animation;

import java.util.HashMap;

import Collision.Shapes.Shape;
import Graphics.Rendering.GeneralRenderer;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;

public class Animator {

	private Timer timer;
	private long frameDelta;
	public int fps;

	private HashMap<ID, Animation> anims;
	public Animation currentAnim;
	protected Object currentAnimId; // This is expected to be some enumerated element owned by Animator or a child
									// class.

	private GeneralRenderer rend;

	public static enum ID {
		IDLE, ACCEL, MOVING, DASHING, DASH_ATK, JAB1, JAB2, LUNGE, TUMBLE, WINDUP
	}

	/**
	 * 
	 * @param anims
	 * @param fps
	 * @param renderer
	 * @param shape    needed to generate UVs
	 */
	public Animator(HashMap<ID, Animation> anims, int fps, GeneralRenderer renderer, Shape shape) {
		this.fps = fps;
		this.frameDelta = 1000 / fps;

		this.anims = anims;
		this.currentAnimId = 0;

		if (anims.get(ID.IDLE) == null) {
			new Exception("No idle animation specified for this entity").printStackTrace();
			System.exit(1);
		}

		this.currentAnim = anims.get(ID.IDLE);

		this.rend = renderer;

		TimerCallback cb = new TimerCallback() {

			@Override
			public void invoke(Timer timer) {
				currentAnim.nextFrame();
				rend.spr = currentAnim.getFrame().tex;
				rend.updateUVs(currentAnim.getFrame().genSubUV(shape));
			}

		};
		this.timer = new Timer(frameDelta, cb);
	}

	public void update() {
		timer.update();
	}

	public void switchAnim(Object animKey) {
		if (currentAnimId.equals(animKey)) {
			System.err.println("Animation already active");
			return;
		}

		// Reset current anim
		currentAnim.resetCurrFrame();
		currentAnim = anims.get(animKey);
		currentAnimId = animKey;
	}

	public Object getAnimID() {
		return currentAnimId;
	}
}
