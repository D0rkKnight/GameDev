package Entities;

import org.joml.Vector2f;

import Collision.Hitbox;
import Debug.Debug;
import GameController.GameManager;
import Rendering.Renderer;
import Rendering.GeneralRenderer;
import Wrappers.Stats;

public class ShardSlimeEnemy extends BouncingEnemy{
	
	public ShardSlimeEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Entity clone(float xPos, float yPos) {
		try {
			return new ShardSlimeEnemy(ID, new Vector2f(xPos, yPos), renderer.clone(), name, stats.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLanding() {
		// TODO Auto-generated method stub
		for (int i=0; i<5; i++) {
			Vector2f pos = new Vector2f(position).add(new Vector2f(8, 10));
			
			Projectile proj = new Projectile(0, pos, GameManager.renderer, "Bullet"); //initializes bullet entity
			
			GeneralRenderer rend = (GeneralRenderer) proj.renderer;
			rend.spr = Debug.debugTex;
			
			Vector2f velo = new Vector2f(((float) Math.random() - 0.5f), (float) (Math.random()/2) + 0.3f);
			
			proj.pData.velo = new Vector2f(velo);
			proj.hasGravity = true;
			proj.alignment = alignment;
			
			GameManager.subscribeEntity(proj);
		}
	}

	@Override
	public void attack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void die() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controlledMovement() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Entity clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onBounce() {
		// TODO Auto-generated method stub
		
	}
}
