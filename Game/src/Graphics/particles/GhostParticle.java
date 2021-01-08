package Graphics.particles;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.SubTexture;

public class GhostParticle extends Particle {

	private SubTexture subTex;

	public GhostParticle(ParticleSystem master, int index, int stride, int life, Shape particleShape,
			Vector2f[] masterVertexPos, Vector2f[] masterUV, SubTexture subTex) {
		super(master, index, stride, life, particleShape, masterVertexPos, masterUV);

		this.subTex = subTex;
	}

	@Override
	public Vector2f[] genUV() {
		return subTex.genSubUV(particleShape);
	}
}
