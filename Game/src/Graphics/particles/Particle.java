package Graphics.particles;

public abstract class Particle<T extends ParticleSystem<?>> {

	protected T master;

	public Particle(T master) {
		this.master = master;
	}

	public abstract void update();

}
