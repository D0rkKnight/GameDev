package Entities.Framework;

import org.joml.Vector2f;
import org.joml.Vector2i;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.Shapes.Shape;
import Entities.Player;
import GameController.EntranceData;
import GameController.GameManager;
import GameController.World;
import GameController.procedural.WorldGate;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;

public class Entrance extends Entity implements Collidable {

	private Hitbox hb;

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
		rend.init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;

		// Configure hitbox
		hb = new Hitbox(this, dim.x, dim.y);

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
	public void onHit(Hitbox otherHb) {
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
	public Hitbox getHb() {
		return hb;
	}

	@Override
	public void setHb(Hitbox hb) {
		this.hb = hb;
	}

	// TODO: Rethink this system?
	@Override
	public Entrance createNew(float xPos, float yPos) {
		return createNew(xPos, yPos, 30, 30, 0, 0, "UP");
	}

	public Entrance createNew(float xPos, float yPos, float width, float height, int mapX, int mapY, String dirStr) {
		WorldGate.GateDir dir = WorldGate.GateDir.valueOf(dirStr);

		return new Entrance(ID, new Vector2f(xPos, yPos), name, new Vector2f(width, height), mapX, mapY, dir);
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
