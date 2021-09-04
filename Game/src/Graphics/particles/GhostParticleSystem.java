package Graphics.particles;

import org.joml.Vector2f;

import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Utility.Timers.Timer;
import Utility.Transformations.ProjectedTransform;

public class GhostParticleSystem extends DiscreteParticleSystem {
	protected Timer timer;
	public SubTexture activeSubTex;
	public ProjectedTransform activeTransform;

	public GhostParticleSystem(Texture tex, int particleLimit, Vector2f dims) {
		super(DiscreteParticle.class, tex, particleLimit);

		activeSubTex = new SubTexture(tex, 0, 0, 1, 1);

		timer = new Timer(10, (t) -> {

			if (endOfData < particleLimit) {
				GhostParticle p = new GhostParticle(this, pvCount, 200, particleShape, vertexPos, uvs, activeSubTex,
						activeTransform, dims);
				p.init();

				addParticle(p);
			}
		});
	}

	@Override
	public void update() {
		super.update();

		timer.update();
	}

	public void pauseParticleGeneration() {
		timer.pause();
	}

	public void resumeParticleGeneration() {
		timer.resume();
	}

}
