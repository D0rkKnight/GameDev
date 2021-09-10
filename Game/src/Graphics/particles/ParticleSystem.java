package Graphics.particles;

import java.lang.reflect.Array;

import org.joml.Vector2f;

import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;

public abstract class ParticleSystem<T extends Particle<?>> {

	protected Texture tex;

	// Single pool of data that particles write in and out of (optimized)
	protected Vector2f[] vertexPos;
	protected Vector2f[] uvs;
	protected T[] particles;
	private final Class<T> type;

	protected GeneralRenderer rend;

	public ParticleSystem(Class<T> type, Texture tex, int particleLimit) {
		this.tex = tex;

		this.type = type;
		particles = (T[]) Array.newInstance(type, particleLimit);
	}

	// Children should set their renderers in an init() call that is not inherited

	public void update() {
		// Update particles
		for (Particle<?> p : particles) {
			if (p != null)
				p.update();
		}

		onUpdate();

		packData();
	}

	protected abstract void packData();

	// Use this to calculate things before data is packed
	protected void onUpdate() {

	}

	public abstract void render();

}
