package Entities;

import java.util.HashMap;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import Entities.Framework.Projectile;
import Entities.Framework.StateMachine.StateTag;
import GameController.EntityData;
import GameController.GameManager;
import Graphics.Animation.Animation;
import Graphics.Animation.Animator;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Wrappers.Stats;

public class ShardSlimeEnemy extends BouncingEnemy {

	public ShardSlimeEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("assets/Sprites/ChargingSlime.png"), 32, 32);
		Animation a1 = new Animation(tAtlas.genSubTexSet(0, 0, 15, 0));
		HashMap<StateTag, Animation> aMap = new HashMap<StateTag, Animation>();
		aMap.put(StateTag.IDLE, a1);
		anim = new Animator(aMap, 24, (GeneralRenderer) this.renderer, Shape.ShapeEnum.SQUARE.v);
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new ShardSlimeEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	public void onLanding() {
		for (int i = 0; i < 5; i++) {
			Vector2f pos = new Vector2f(position).add(new Vector2f(8, 10));

			// TODO: Retrieve this from the lookup
			Projectile proj = new Projectile("SHARD", pos, "Bullet"); // initializes bullet entity

			Vector2f velo = new Vector2f(((float) Math.random() - 0.5f), (float) (Math.random() / 2) + 0.3f);

			proj.pData.velo = new Vector2f(velo);
			proj.hasGravity = true;
			proj.setAlign(alignment);

			GameManager.subscribeEntity(proj);
		}
	}

	@Override
	public void onBounce() {
	}
}
