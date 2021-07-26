package Entities.Framework;

import org.joml.Vector2f;
import org.joml.Vector2i;

import Collision.Collidable;
import Collision.Collider;
import Collision.Shapes.Shape;
import Entities.PlayerPackage.Player;
import GameController.EntityData;
import GameController.EntranceData;
import GameController.GameManager;
import GameController.World;
import GameController.procedural.WorldGate;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Entrance extends Entity implements Collidable {

	private Collider hb;

	private boolean hasBeenConnected = false;
	private EntranceData dest;

	public boolean isActive = true;
	private int exclusionRadius = 100;

	public Vector2i localMapPos;
	public WorldGate.GateDir dir;

	public Entrance(String ID, Vector2f position, String name, Vector2f dims, int mapX, int mapY,
			WorldGate.GateDir dir) {
		super(ID, position, name);

		dim = dims;
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		hb = new Collider(this, dim.x, dim.y);

		this.localMapPos = new Vector2i(mapX, mapY);
		this.dir = dir;
	}

	public void setDest(EntranceData dest) {
		this.dest = dest;

		hasBeenConnected = true;
	}

	public EntranceData getDest() {
		return dest;
	}

	@Override
	public void onColl(Collider otherHb) {
		if (otherHb.owner instanceof Player && isActive && !GameManager.roomChanging) {
			if (!hasBeenConnected) {
				new Exception("Entrance not yet configured.").printStackTrace();
				System.exit(1);
			}

			GameManager.roomChanging = true;
			GameManager.switchTimer = new Timer(200, new TimerCallback() {

				@Override
				public void invoke(Timer timer) {
					World.switchMap(dest);
					GameManager.switchTimer = null;
					GameManager.roomChanging = false;
				}
			});

		}
	}

	@Override
	public Collider getColl() {
		return hb;
	}

	@Override
	public void setColl(Collider hb) {
		this.hb = hb;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		// TODO Auto-generated method stub
		WorldGate.GateDir dir = WorldGate.GateDir.valueOf(vals.str("dir"));

		return new Entrance(vals.str("type"), pos, vals.str("name"), dims, vals.in("mapX"), vals.in("mapY"), dir);
	}

	@Override
	public void calculate() {
		// Check that the player has moved out of its radius before activating again
		if (!isActive) {
			float distToPlayer = new Vector2f(GameManager.player.getPosition()).distance(position);

			if (distToPlayer > exclusionRadius)
				isActive = true;
		}
	}

}
