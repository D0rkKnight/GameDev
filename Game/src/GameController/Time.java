package GameController;

import Debugging.Debug;

public class Time {
	// TODO: Split into multiple timer tracks

	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	private static long startTime;

	private static long pauseTime = 0;
	private static long pauseBuffer = 0;
	private static boolean isPaused = false;

	public static void initTime() {
		reset();

		startTime = currTime;
	}

	public static void updateTime() {
		lastTime = currTime;
		currTime = getT();

		deltaTime = currTime - lastTime;
		deltaTime = Math.max(1, deltaTime);

	}

	public static long deltaT() {
		if (Debug.frameWalk) {
			return (long) Debug.frameDelta;
		}

		return (long) (deltaTime * Debug.timeScale);
	}

	public static long getFrameTime() {
		return currTime;
	}

	// Ignores time when game is paused
	public static long timeSinceStart() {
		if (!isPaused)
			return (currTime - startTime - pauseBuffer);
		else {
			return (pauseTime - startTime - pauseBuffer);
		}
	}

	public static void reset() {
		currTime = getT();
		lastTime = currTime;
	}

	private static long getT() {
		return System.nanoTime() / 1000000;
	}

	public static void beginPause() {
		pauseTime = getT();
		isPaused = true;
	}

	public static void endPause() {
		pauseBuffer += currTime - pauseTime;
		isPaused = false;
	}
}
