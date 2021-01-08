package Graphics.particles;

import org.joml.Vector2f;

import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Utility.Timers.Timer;
import Wrappers.Color;

public class GhostParticleSystem extends ParticleSystem {
	protected Timer timer;
	public SubTexture activeSubTex;
	public Transformation activeTransform;

	public GhostParticleSystem(Texture tex, int particleLimit, Vector2f dims) {
		super(tex, particleLimit);
		// TODO Auto-generated constructor stub

		activeSubTex = new SubTexture(tex, 0, 0, 1, 1);

		timer = new Timer(50, (t) -> {

			if (endOfData < particleLimit) {
				GhostParticle p = new GhostParticle(this, pvCount, 1000, particleShape, vertexPos, uvs, activeSubTex,
						activeTransform, dims);
				p.init();

				addParticle(p);
			}
		});
	}

	public void init() {
		// Does not populate with particles
		// Load in the data
		rend = new GeneralRenderer(SpriteShader.genShader("texShader"));

		// Or, send in nothing
		rend.init(new Transformation(), new Vector2f[] {}, new Vector2f[] {}, new Color(1, 1, 1, 1));
		rend.spr = this.tex;
	}

	@Override
	public void update() {
		super.update();

		timer.update();
	}
}
