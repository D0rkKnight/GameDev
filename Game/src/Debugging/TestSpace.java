package Debugging;

import GameController.ArenaController;
import Graphics.particles.GhostParticleSystem;

public class TestSpace {

	public static boolean ffExecuted = false;

	public static GhostParticleSystem pSys;

	public static void init() {
	}

	public static void firstFrame() {
		ArenaController.waves.get(0).release();
	}

	public static void draw() {

	}
}
