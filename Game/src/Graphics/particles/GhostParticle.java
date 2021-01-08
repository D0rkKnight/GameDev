package Graphics.particles;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Graphics.Elements.SubTexture;

public class GhostParticle extends Particle {

	private SubTexture subTex;
	public Vector2f pos;
	public int size;

	public GhostParticle(ParticleSystem master, int stride, int life, Shape particleShape, Vector2f[] masterVertexPos,
			Vector2f[] masterUV, SubTexture subTex, Vector2f pos, int size) {
		super(master, stride, life, particleShape, masterVertexPos, masterUV);

		this.subTex = subTex;
		this.pos = pos;
		this.size = size;
	}

	@Override
	public Vector2f[] genUV() {
		return subTex.genSubUV(particleShape);
	}

	@Override
	protected Vector2f[] genPos() {
		// TODO Auto-generated method stub
		Vector2f[] rendVerts = particleShape.getRenderVertices(new Vector2f(size));
		for (Vector2f v : rendVerts)
			v.add(pos);

		return rendVerts;
	}
}
