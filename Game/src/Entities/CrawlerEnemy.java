package Entities;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import Collision.Hitbox;
import Collision.HammerShapes.HammerShape;
import Entities.Framework.Combatant;
import Entities.Framework.Enemy;
import GameController.GameManager;
import GameController.Map;
import GameController.World;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Tiles.Tile;
import Utility.Transformation;
import Wrappers.Color;
import Wrappers.Stats;

public class CrawlerEnemy extends Enemy {

	public Map.CompEdgeSegment attachedSegment;
	public Vector2f anchorOffset; // This just offsets and rotates the model.
	public float ang;

	public CrawlerEnemy(String ID, Vector2f position, Renderer renderer, String name, Stats stats) {
		super(ID, position, renderer, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(96f, 96f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HShapeEnum.SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Texture.getTex("assets/Sprites/circle_saw.png");

		// Configure hitbox
		hitbox = new Hitbox(this, dim.x, dim.y);

		anchorOffset = new Vector2f(dim).div(-2);

		// Scan downwards to find a surface to attach to.
		if (position != null) {
			Vector2i tPos = new Vector2i((int) position.x / GameManager.tileSize,
					(int) position.y / GameManager.tileSize);
			Tile[][] collGrid = World.currmap.grids.get(GameManager.Grid.COLL.name);
			for (int i = tPos.y; i >= 0; i--) {
				Tile t = collGrid[tPos.x][i];
				if (t == null)
					continue;

				if (!t.edgeSegs.isEmpty()) {
					attachedSegment = t.edgeSegs.get(0);

					break;
				}
			}

			if (attachedSegment == null) {
				new Exception("Entity " + name + " cannot find segment to attach to.").printStackTrace();
				System.exit(1);
			}

			this.position.set(new Vector2f(attachedSegment.v1).mul(GameManager.tileSize));
		}

		baseInvulnState = true;
		isInvuln = baseInvulnState;
	}

	@Override
	public Combatant createNew(float xPos, float yPos, Stats stats) {
		return new CrawlerEnemy(ID, new Vector2f(xPos, yPos), renderer, name, stats);
	}

	@Override
	public void calculate() {
		super.calculate();

		if (attachedSegment.nextSeg != null) {
			attachedSegment = attachedSegment.nextSeg; // Do this in a smarter way

			position.set(new Vector2f(attachedSegment.v1).mul(GameManager.tileSize));
			position.add(anchorOffset);

			// Do a little hack and rotate around a point. Don't forget that these are done
			// right to left.
//			Vector2f n = attachedSegment.edge.normal;
//			float ang = (float) (Math.atan(n.y/n.x) - Math.PI/2);
//			if (n.x < 0) ang += Math.PI;

			ang += 0.5;

			Matrix4f rot = transform.rot;
			rot.identity();
			rot.translate(new Vector3f(-anchorOffset.x, -anchorOffset.y, 0));
			rot.setRotationXYZ(0, 0, ang);
			rot.translate(new Vector3f(anchorOffset, 0));
		}
	}
}
