package Collision;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import Collision.HammerShapes.HammerShape;
import Entities.Framework.Entity;
import GameController.GameManager;
import Utility.Transformation;

public class Hitbox {
	public Vector2f position;
	public float height;
	public float width;
	public Entity owner;
	
	public HammerShape shape;
	public Transformation transform;
	private Vector2f[] verts; //Encapsulated in genVerts
	
	public Hitbox(Entity owner, float width, float height) {
		this(owner, width, height, GameManager.hammerLookup.get(HammerShape.HAMMER_SHAPE_SQUARE));
	}
	
	public Hitbox(Entity owner, float width, float height, HammerShape shape) {
		this.height = height;
		this.width = width;
		this.owner = owner;
		this.shape = shape;
		
		//Local transformation, applied to vertices for collision.
		transform = new Transformation();
		verts = new Vector2f[shape.vertices.length];
		if (owner.getPosition() != null) this.position = owner.getPosition();
		
		//Any class with a hitbox MUST implement Collidable
		if (!(owner instanceof Collidable)) {
			new Exception("Owner does not implement Collidable.").printStackTrace();
			System.exit(1);
		}
	}
	
	public Hitbox(Hitbox hb, Entity owner) {
		this(owner, hb.width, hb.height);
	}
	
	public void update() {
		//Pull data
		position = new Vector2f(owner.getPosition());
		transform.rot.set(owner.transform.rot);
	}
	
	/**
	 * Special kinds of hitboxes may not propagate the hit to its owner
	 * @param hb
	 */
	public void hitBy(Hitbox hb) {
		((Collidable) owner).onHit(hb);
	}
	
	//Generates vertices in world space
	public Vector2f[] genWorldVerts() {
		transform.pos.set(position);
		transform.scale.scaling(width, height, 1);
		Matrix4f model = transform.genModel();
		
		for (int i=0; i<verts.length; i++) {
			Vector4f transed = new Vector4f(shape.vertices[i], 0, 1).mul(model);
			verts[i] = new Vector2f(transed.x, transed.y);
		}
		
		return verts;
	}
}
