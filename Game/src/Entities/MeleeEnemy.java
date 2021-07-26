package Entities;

import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Hurtbox;
import Collision.Behaviors.PGBGroundFriction;
import Collision.Shapes.Shape;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import GameController.EntityData;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Animation.Animator.ID;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class MeleeEnemy extends Enemy {

	public MeleeEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);
		// TODO Auto-generated constructor stub

		// Configure the renderer real quick
		dim = new Vector2f(64, 64);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		coll = new Hurtbox(this, dim.x, dim.y * 1.5f);
		rendOffset.y = dim.y * 0.5f;

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("assets/Sprites/bell_enemy.png"), 32, 32);
		Animation a1 = new Animation(tAtlas.genSubTexSet(0, 0, 5, 0));
		HashMap<ID, Animation> aMap = new HashMap<ID, Animation>();
		aMap.put(Animator.ID.IDLE, a1);
		anim = new Animator(aMap, 12, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);

		hasGravity = true;
	}

	@Override
	protected void initPhysicsBehavior() {
		super.initPhysicsBehavior();
		generalBehaviorList.add(new PGBGroundFriction(10f));
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new MeleeEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}
}
