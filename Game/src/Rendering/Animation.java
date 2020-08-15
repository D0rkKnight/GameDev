package Rendering;

public class Animation {
	
	private Texture[] frames;
	private int currFrame;
	
	public Animation(Texture[] frames) {
		this.frames = frames;
	}
	
	public Texture getFrame() {
		return frames[currFrame];
	}
	
	public void nextFrame() {
		currFrame ++;
		
		//Loop
		if (currFrame == frames.length) currFrame = 0;
	}
	
	public void resetCurrFrame() {
		setCurrFrame(0);
	}
	
	public void setCurrFrame(int newFrame) {
		currFrame = newFrame;
	}
}
