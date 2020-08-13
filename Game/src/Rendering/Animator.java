package Rendering;

import Wrappers.Timer;
import Wrappers.TimerCallback;

public class Animator {
	
	private Timer timer;
	private long frameDelta;
	private int fps;
	
	private Animation[] anims;
	private Animation currentAnim;
	
	private SpriteRenderer rend;
	
	public Animator(Animation[] anims, int fps, SpriteRenderer rend) {
		this.fps = fps;
		this.frameDelta = 1000/fps;
		
		this.anims = anims;
		this.currentAnim = anims[0];
		
		this.rend = rend;
		
		TimerCallback cb = new TimerCallback() {
			
			public void invoke() {
				currentAnim.nextFrame();
				rend.spr = currentAnim.getFrame();
			}
			
		};
		this.timer = new Timer(frameDelta, cb);
	}
	
	public void update() {
		timer.update();
	}
}
