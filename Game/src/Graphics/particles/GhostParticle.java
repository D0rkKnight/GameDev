package Graphics.particles;

import org.joml.Vector2f;
import org.joml.Vector4f;

import Collision.Shapes.Shape;
import Graphics.Elements.SubTexture;
import Utility.Transformation;

public class GhostParticle extends Particle {

	private SubTexture subTex;
	public Vector2f dims;
	public Transformation trans;

	public GhostParticle(ParticleSystem master, int stride, int life, Shape particleShape, Vector2f[] masterVertexPos,
			Vector2f[] masterUV, SubTexture subTex, Transformation trans, Vector2f dims) {
		super(master, stride, life, particleShape, masterVertexPos, masterUV);

		this.subTex = subTex;
		this.trans = trans;
		this.dims = dims;
	}

	@Override
	public Vector2f[] genUV() {
		return subTex.genSubUV(particleShape);
	}

	@Override
	protected Vector2f[] genPos() {
		// TODO Auto-generated method stub
		Vector2f[] rendVerts = particleShape.getRenderVertices(dims);
		for (Vector2f v : rendVerts) {
			Vector4f vFour = new Vector4f(v.x, v.y, 0, 1).mul(trans.genModel());
			v.set(vFour.x, vFour.y);
		}

		return rendVerts;
	}
}
