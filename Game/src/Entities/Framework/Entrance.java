package Entities.Framework;

import org.joml.Vector2f;

import Collision.Collidable;
import Collision.Hitbox;
import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Entities.Player;
import GameController.GameManager;
import GameController.World;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;

public class Entrance extends Entity implements Collidable {

	private Hitbox hb;

	private boolean hasBeenConnected = false;
	public int entranceId;
	private int targetMap;
	private int targetEntranceId;

	public boolean isActive = true;
	private int exclusionRadius = 100;

	public Entrance(String ID, Vector2f position, Renderer renderer, String name, Vector2f dims, int entranceId) {
		super(ID, position, renderer, name);

		dim = dims;
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;

		// Configure hitbox
		hb = new Hitbox(this, dim.x, dim.y);

		this.entranceId = entranceId;
	}

	public void setData(int targetMap, int targetEntranceId) {
		this.targetMap = targetMap;
		this.targetEntranceId = targetEntranceId;

		hasBeenConnected = true;
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
					World.switchMap(targetMap, targetEntranceId);
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

	@Override
	public Entrance createNew(float xPos, float yPos) {
		return createNew(xPos, yPos, 30, 30, -1);
	}

	public Entrance createNew(float xPos, float yPos, float width, float height, int entranceId) {
		return new Entrance(ID, new Vector2f(xPos, yPos), renderer, name, new Vector2f(width, height), entranceId);
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
