package Entities;

import org.joml.Vector2f;

import Debugging.Debug;
import Entities.Framework.Projectile;
import GameController.GameManager;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Wrappers.Stats;

public class ShardSlimeEnemy extends BouncingEnemy {

	public ShardSlimeEnemy(String ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
	}

	@Override
	public ShardSlimeEnemy createNew(float xPos, float yPos, Stats stats) {
		return new ShardSlimeEnemy(ID, new Vector2f(xPos, yPos), renderer, name, stats);
	}

	@Override
	public void onLanding() {
		for (int i = 0; i < 5; i++) {
			Vector2f pos = new Vector2f(position).add(new Vector2f(8, 10));

			// TODO: Retrieve this from the lookup
			Projectile proj = new Projectile("SHARD", pos, GameManager.renderer, "Bullet"); // initializes bullet entity

			GeneralRenderer rend = (GeneralRenderer) proj.renderer;
			rend.spr = Debug.debugTex;

			Vector2f velo = new Vector2f(((float) Math.random() - 0.5f), (float) (Math.random() / 2) + 0.3f);

			proj.pData.velo = new Vector2f(velo);
			proj.hasGravity = true;
			proj.alignment = alignment;

			GameManager.subscribeEntity(proj);
		}
	}

	@Override
	public void onBounce() {
	}
}
