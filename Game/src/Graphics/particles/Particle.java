package Graphics.particles;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import GameController.Time;

public abstract class Particle {

	protected ParticleSystem master;

	protected Vector2f[] masterVertexPos;
	protected Vector2f[] masterUV;

	protected Shape particleShape;

	protected int index;
	protected int stride;
	protected int firstIndex;
	protected int life;

	public Particle(ParticleSystem master, int stride, int life, Shape particleShape, Vector2f[] masterVertexPos,
			Vector2f[] masterUV) {
		this.master = master;
		this.masterVertexPos = masterVertexPos;
		this.masterUV = masterUV;

		this.life = life;

		this.index = master.endOfData;
		this.stride = stride;
		this.firstIndex = index * stride;

		this.particleShape = particleShape;
	}

	/**
	 * Generate the position vectors of this particle, invoked in constructor
	 * 
	 * @return array of such vectors
	 */
	protected abstract Vector2f[] genPos();

	/**
	 * Generate the UV vectors of this particle, invoked in constructor
	 * 
	 * @return array of such vectors
	 */
	protected abstract Vector2f[] genUV();

	protected void init() {
		// Generate self
		Vector2f[] partPos = genPos();
		Vector2f[] partUV = genUV();

		// Store data
		for (int i = 0; i < stride; i++) {
			setVertexData(masterVertexPos, i, partPos[i]);
			setVertexData(masterUV, i, partUV[i]);
		}
	}

	public void update() {
		life -= Time.deltaT();

		// Self destruction
		if (life <= 0) {
			master.removeParticle(index);
		}
	}

	public void setIndex(int index) {
		this.index = index;
		this.firstIndex = index * stride;
	}

	Vector2f getVertexData(Vector2f[] data, int i) {
		return data[firstIndex + i];
	}

	void setVertexData(Vector2f[] data, int i, Vector2f v) {
		data[firstIndex + i] = v;
	}
}
