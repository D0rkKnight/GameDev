package Graphics.particles;

import java.util.Arrays;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Wrappers.Color;

public abstract class ParticleSystem {

	protected Texture tex;

	// Single pool of data that particles write in and out of (optimized)
	protected Vector2f[] vertexPos;
	protected Vector2f[] uvs;
	protected Particle[] particles;

	protected int particleLimit;
	protected Shape particleShape;
	protected int pvCount;

	protected GeneralRenderer rend;

	private boolean shouldRegenData = false;
	int endOfData;

	public ParticleSystem(Texture tex, int particleLimit) {
		this.tex = tex;
		this.particleLimit = particleLimit;
		this.endOfData = 0;

		particleShape = Shape.ShapeEnum.SQUARE.v;
		pvCount = particleShape.renderVertexCount();

		vertexPos = new Vector2f[this.particleLimit * pvCount];
		uvs = new Vector2f[this.particleLimit * pvCount];
		particles = new Particle[particleLimit];
	}

	// Children should set their renderers in an init() call that is not inherited

	public void update() {
		// Update particles
		for (Particle p : particles) {
			if (p != null)
				p.update();
		}

		// Pack data to the front
		if (shouldRegenData) {

			int j = particles.length - 1; // Secondary tracker
			for (int i = 0; i < j; i++) {
				if (particles[i] == null) {
					// Search for item to pack forwards

					Particle replacer = null;
					while (j > 0) {
						if (particles[j] != null) {
							replacer = particles[j];
							particles[j] = null;
							break;
						}

						j--;
					}

					if (replacer != null) {

						// Reassign indexing
						replacer.setIndex(i);
						particles[i] = replacer;

						// Pack replacer data into empty data too
						for (int k = 0; k < pvCount; k++) {
							int vi = i * pvCount + k;
							int vj = j * pvCount + k;

							vertexPos[vi] = vertexPos[vj];
							uvs[vi] = uvs[vj];
						}
					}
				}
			}

			endOfData = j;
			shouldRegenData = false;
		}
	}

	public void render() {
		// Enqueue new data
		Vector2f[] rendPos = Arrays.copyOfRange(vertexPos, 0, endOfData * pvCount);
		Vector2f[] rendUV = Arrays.copyOfRange(uvs, 0, endOfData * pvCount);

		rend.rebuildMesh(rendPos, rendUV, new Color());

		rend.render();
	}

	public void addParticle(Particle p) {
		if (endOfData == particles.length)
			System.err.println("Max particles already!");

		particles[endOfData] = p;
		endOfData++;
	}

	public void removeParticle(int index) {
		particles[index] = null;
		shouldRegenData = true;
	}
}
