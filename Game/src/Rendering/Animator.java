package Rendering;

import Wrappers.Timer;
import Wrappers.TimerCallback;

public class Animator {
	
	private Timer timer;
	private long frameDelta;
	public int fps;
	
	public Animation[] anims;
	private Animation currentAnim;
	
	private GeneralRenderer rend;
	
	public Animator(Animation[] anims, int fps, GeneralRenderer rend) {
		this.fps = fps;
		this.frameDelta = 1000/fps;
		
		this.anims = anims;
		this.currentAnim = anims[0];
		
		this.rend = rend;
		
		TimerCallback cb = new TimerCallback() {
			
			public void invoke(Timer timer) {
				currentAnim.nextFrame();
				rend.spr = currentAnim.getFrame();
			}
			
		};
		this.timer = new Timer(frameDelta, cb);
	}
	
	public Animator(Animator anim, GeneralRenderer rend) {
		this(anim.anims, anim.fps, rend);
	}
	
	public void update() {
		timer.update();
	}
	
	public void switchAnim(int animId) {
		//Reset current anim
		currentAnim.resetCurrFrame();
		currentAnim = anims[animId];
	}
}
