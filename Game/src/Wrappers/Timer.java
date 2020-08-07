package Wrappers;

import GameController.GameManager;

/**
 * Just loops on a repeat
 * NOTE: This is in game time, so all times in a single frame are going to be the same.
 * @author Hanzen Shou
 *
 */

public class Timer {
	private long currTime;
	private long loopTime;
	private long loopLength;
	
	private TimerCallback cb;
	
	public Timer(long loopLength, TimerCallback cb) {
		this.currTime = GameManager.getFrameTime();
		this.loopLength = loopLength;
		
		this.loopTime = this.currTime + loopLength;
		
		this.cb = cb;
	}
	
	//Returns whether the time threshold is crossed.
	public boolean update() {
		currTime = GameManager.getFrameTime();
		if (currTime > loopTime) {
			loopTime = currTime + loopLength;
			
			cb.invoke();
			
			return true;
		}
		return false;
	}
}
