package Graphics.particles;

import org.joml.Vector2f;

import GameController.Time;

public class ClothParticle extends Particle<ClothParticleSystem> {

	public Vector2f pos;
	public Vector2f lastPos;
	public Vector2f anchorPos;

	public static class Constraint {
		public ClothParticle p1;
		public ClothParticle p2;
		public float d;

		public Constraint(ClothParticle p1, ClothParticle p2, float d) {
			this.p1 = p1;
			this.p2 = p2;
			this.d = d;
		}
	}

	public ClothParticle(ClothParticleSystem master, Vector2f pos) {
		super(master);

		this.pos = new Vector2f(pos);
		this.lastPos = new Vector2f(pos);
	}

	// Advance physics simulation
	@Override
	public void update() {

		if (anchorPos != null) {
			pos.set(anchorPos);
		} else {

			// Fun wind map!
			// float windStrength = (float) (Math.sin(pos.x * 0.1) + Math.sin(pos.y * 0.1))
			// + 2;
			float windStrength = 2f;

			// Update position using Verlet integration
			Vector2f a = new Vector2f(windStrength * 100, -0.5f * 1000); // Gravity
			float dt = Time.deltaT() / 1000f;

			Vector2f temp = new Vector2f(pos);

			// x' = 2x' - x + a*dt^2
			// >>> x += x-oldx+a*fTimeStep*fTimeStep;

			float dragCoef = 0.99f;
			Vector2f ls = new Vector2f(pos).sub(lastPos).mul(dragCoef);
			// pos.add(pos).sub(lastPos).add(new Vector2f(a).mul(dt * dt));
			pos.add(ls).add(new Vector2f(a).mul(dt * dt));

			lastPos.set(temp);
		}
	}

	public void setAnchor(Vector2f p) {
		this.anchorPos = new Vector2f(p);
	}

	public void setAnchor() {
		this.setAnchor(pos);
	}

	public boolean isAnchored() {
		return !(anchorPos == null);
	}
}
