package Debugging;

import GameController.procedural.WorldGenerator;
import Graphics.particles.GhostParticleSystem;

public class TestSpace {

	public static GhostParticleSystem pSys;

	public static void init() {

		WorldGenerator.genWorld();

	}

	public static void draw() {
	}
}
