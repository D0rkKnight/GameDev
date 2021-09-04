package Graphics.particles;

import java.util.Arrays;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Drawer;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class DiscreteParticleSystem extends ParticleSystem<DiscreteParticle> {

	protected int particleLimit;
	protected Shape particleShape;
	protected int pvCount;

	protected boolean shouldRegenData = false;

	int endOfData;

	public DiscreteParticleSystem(Class<DiscreteParticle> type, Texture tex, int particleLimit) {
		super(type, tex, particleLimit);
		this.endOfData = 0;

		this.particleLimit = particleLimit;
		particleShape = Shape.ShapeEnum.SQUARE.v;
		pvCount = particleShape.renderVertexCount();

		vertexPos = new Vector2f[this.particleLimit * pvCount];
		uvs = new Vector2f[this.particleLimit * pvCount];
	}

	public void init() {
		// Does not populate with particles
		// Load in the data
		rend = new GeneralRenderer(SpriteShader.genShader("texShader"));

		// Or, send in nothing
		rend.init(new ProjectedTransform(), new Vector2f[] {}, new Vector2f[] {}, new Color(1, 1, 1, 1));
		rend.spr = this.tex;
	}

	@Override
	public void render() {
		// Enqueue new data
		Vector2f[] rendPos = Arrays.copyOfRange(vertexPos, 0, endOfData * pvCount);
		Vector2f[] rendUV = Arrays.copyOfRange(uvs, 0, endOfData * pvCount);

		rend.rebuildMesh(rendPos, rendUV, new Color());

		// Bind main buffer
		Drawer.setCurrBuff(Drawer.DBEnum.MAIN);
		rend.render();
	}

	@Override
	protected void packData() {
		// Pack data to the front
		if (shouldRegenData) {

			int j = particles.length - 1; // Secondary tracker
			for (int i = 0; i < j; i++) {
				if (particles[i] == null) {
					// Search for item to pack forwards

					DiscreteParticle replacer = null;
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

	public void addParticle(DiscreteParticle p) {
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
