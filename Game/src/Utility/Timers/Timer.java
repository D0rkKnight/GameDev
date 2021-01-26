package Utility.Timers;

import java.util.ArrayList;

import GameController.Time;

/**
 * Just loops on a repeat NOTE: This is in game time, so all times in a single
 * frame are going to be the same.
 * 
 * @author Hanzen Shou
 *
 */

public class Timer {
	private long currTime;
	private long loopTime;
	private long loopLength;

	private boolean isPaused;

	protected ArrayList<TimerCallback> cbs;

	public ArrayList<Timer> subTimers;

	public Timer(long loopLength, TimerCallback... cbs) {
		this.currTime = Time.getFrameTime();
		this.loopLength = loopLength;

		this.loopTime = this.currTime + loopLength;

		this.cbs = new ArrayList<>();
		for (TimerCallback cb : cbs)
			this.cbs.add(cb); // Dump callbacks into arraylist

		subTimers = new ArrayList<>();
	}

	// Returns whether the time threshold is crossed.
	public boolean update() {
		// Short circuit
		if (isPaused)
			return false;

		currTime = Time.getFrameTime();
		if (currTime > loopTime) {
			loopTime = currTime + loopLength;

			for (TimerCallback cb : cbs)
				cb.invoke(this);

			return true;
		}

		// Update child timers too
		for (Timer t : subTimers)
			t.update(); // Note: child timers have to be freed by deletion from this array.

		return false;
	}

	public void pause() {
		isPaused = true;

		// Pause children timers
		for (Timer t : subTimers)
			t.pause();
	}

	public void resume() {
		isPaused = false;

		// Advance the time
		currTime = Time.getFrameTime();
		while (currTime > loopTime) {
			loopTime += loopLength;
		}

		// Update children
		for (Timer t : subTimers)
			t.resume();
	}
}
