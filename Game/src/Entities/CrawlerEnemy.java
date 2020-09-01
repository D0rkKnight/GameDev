package Entities;

import org.joml.Vector2f;
import org.joml.Vector2i;

import Collision.HammerShape;
import Collision.Hitbox;
import Debugging.Debug;
import GameController.GameManager;
import GameController.Map;
import Rendering.GeneralRenderer;
import Rendering.Renderer;
import Rendering.Transformation;
import Tiles.Tile;
import Wrappers.Color;
import Wrappers.Stats;

public class CrawlerEnemy extends Enemy {
	
	public Map.CompEdgeSegment attachedSegment;
	
	public CrawlerEnemy(int ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);
		// TODO Auto-generated constructor stub
		
		//Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;
		
		//Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);
		
		//Scan downwards to find a surface to attach to.
		if (position != null) {
			Vector2i tPos = new Vector2i ((int) position.x/GameManager.tileSize, (int) position.y/GameManager.tileSize);
			Tile[][] collGrid = GameManager.currmap.grids.get(GameManager.GRID_COLL);
			for (int i=tPos.y; i>=0; i--) {
				Tile t = collGrid[tPos.x][i];
				if (t == null) continue;
				
				if (!t.edgeSegs.isEmpty()) {
					attachedSegment = t.edgeSegs.get(0);
					
					break;
				}
			}
			
			if (attachedSegment == null) {
				new Exception("Entity "+name+" cannot find segment to attach to.").printStackTrace();
				System.exit(1);
			}
			
			this.position.set(new Vector2f(attachedSegment.edge.v1).mul(GameManager.tileSize));
		}
	}
	
	public Combatant createNew(float xPos, float yPos, Stats stats) {
		// TODO Auto-generated method stub
		return new CrawlerEnemy(ID, new Vector2f(xPos, yPos), renderer, name, stats);
	}
	
	public void calculate() {
		super.calculate();
		
		if (attachedSegment.nextSeg != null) {
			attachedSegment = attachedSegment.nextSeg;
			this.position.set(new Vector2f(attachedSegment.edge.v1).mul(GameManager.tileSize));
		}
	}

	@Override
	public void onHit(Hitbox otherHb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void die() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attack() {
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

}
