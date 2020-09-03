package Graphics.Animation;

import Graphics.Elements.Texture;

public class Animation {
	
	private Texture[] frames;
	public int w;
	public int h;
	
	private int currFrame;
	private AnimationCallback cb; //These are configured by the animator, after construction
	
	public Animation(Texture[] frames) {
		this.frames = frames;
		
		this.w = frames[0].width;
		this.h = frames[0].height;
	}
	
	public Texture getFrame() {
		return frames[currFrame];
	}
	
	public void nextFrame() {
		currFrame ++;
		
		//Loop
		if (currFrame == frames.length) {
			if (cb != null) cb.onLoopEnd();
			
			setCurrFrame(0);
		}
	}
	
	public void resetCurrFrame() {
		setCurrFrame(0);
	}
	
	public void setCurrFrame(int newFrame) {
		currFrame = newFrame;
	}
	
	public void setCb(AnimationCallback cb) {
		this.cb = cb;
	}
}
