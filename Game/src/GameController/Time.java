package GameController;

import Debugging.Debug;

public class Time {
	private static long deltaTime = 0;
	private static long currTime = 0;
	private static long lastTime = 0;
	private static long startTime;

	public static void initTime() {
		currTime = System.nanoTime() / 1000000;
		lastTime = currTime;

		startTime = currTime;
	}

	public static void updateTime() {
		lastTime = currTime;
		currTime = System.nanoTime() / 1000000;

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

	public static long timeSinceStart() {
		return (currTime - startTime);
	}
}
