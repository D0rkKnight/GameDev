package Entities.SpiritBoss;

import org.joml.Vector2f;

import Collision.Collider.CODCircle;
import Collision.Hitbox;
import Entities.Framework.Aligned;
import Entities.Framework.Entity;
import Entities.Framework.PhysicsEntity.Alignment;

public class SpiritPulse extends Entity implements Aligned {

	public float r;
	public Hitbox hb;
	private Alignment align;

	public SpiritPulse(String ID, Vector2f position, String name, float radius) {
		super(ID, position, name);

		addColl(new Hitbox(this, new CODCircle(radius)));
		dim = new Vector2f(radius * 2); // Treated differently because circle collider (centered on center of circle)

		align = Alignment.ENEMY;
	}

	@Override
	public Alignment getAlign() {
		return align;
	}

	@Override
	public void setAlign(Alignment align) {
		this.align = align;
	}
}
