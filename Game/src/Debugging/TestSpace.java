package Debugging;

import Graphics.particles.ParticleSystem;

public class TestSpace {

	public static ParticleSystem pSys;

	public static void init() {

		pSys = new ParticleSystem(Debug.debugTex, 100);
	}

	public static void draw() {
		pSys.update();
		pSys.render();
	}
}
