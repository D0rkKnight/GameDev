package Graphics.particles;

import java.util.Arrays;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Wrappers.Color;

public class ParticleSystem {

	private Texture tex;
	private Vector2f[] vertexPos;
	private Vector2f[] uvs;
	private Particle[] particles;

	private int particleLimit;
	private Shape particleShape;
	private int pvCount;

	private GeneralRenderer rend;

	private boolean shouldRegenData = false;
	private int endOfData;

	public ParticleSystem(Texture tex, int particleLimit) {
		this.tex = tex;
		this.particleLimit = particleLimit;
		this.endOfData = particleLimit;

		particleShape = Shape.ShapeEnum.SQUARE.v;
		pvCount = particleShape.renderVertexCount();

		vertexPos = new Vector2f[this.particleLimit * pvCount];
		uvs = new Vector2f[this.particleLimit * pvCount];
		particles = new Particle[particleLimit];

		// Populate with particles
		for (int i = 0; i < this.particleLimit; i++) {
			particles[i] = new Particle(this, i, pvCount, (int) (Math.random() * 2000), particleShape, vertexPos, uvs);
			particles[i].init();
		}

		// Load in the data
		rend = new GeneralRenderer(SpriteShader.genShader("texShader"));

		rend.init(new Transformation(new Vector2f(100, 100)), vertexPos, uvs, new Color(1, 1, 1, 1));
		rend.spr = this.tex;
	}

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

	public void removeParticle(int index) {
		particles[index] = null;
		shouldRegenData = true;
	}
}
