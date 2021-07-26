package Entities;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import Collision.Hitbox;
import Collision.Shapes.Shape;
import Entities.Framework.Enemy;
import Entities.Framework.Entity;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Map;
import GameController.World;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class CrawlerEnemy extends Enemy {

	public Map.CompEdgeSegment attachedSegment;
	public Vector2f anchorOffset; // This just offsets and rotates the model.
	public float ang;

	public CrawlerEnemy(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Configure the renderer real quick
		dim = new Vector2f(96f, 96f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());
		rend.spr = Texture.getTex("assets/Sprites/circle_saw.png");
		this.renderer = rend;

		// Configure hitbox
		coll = new Hitbox(this, dim.x, dim.y);

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

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new CrawlerEnemy(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
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

			Matrix4f rot = localTrans.rot;
			rot.identity();
			rot.translate(new Vector3f(-anchorOffset.x, -anchorOffset.y, 0));
			rot.setRotationXYZ(0, 0, ang);
			rot.translate(new Vector3f(anchorOffset, 0));
		}
	}
}
