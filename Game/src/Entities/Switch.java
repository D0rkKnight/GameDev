package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Input;
import Graphics.Rendering.GeneralRenderer;
import Utility.Transformation;
import Utility.Timers.Timer;
import Wrappers.Color;

public class Switch extends Entity implements Interactive {
	public int state;
	public int statenum;
	public boolean changed;
	float activationDistance;
	public Timer onTimer;
	Player player;

	public Switch(String ID, Vector2f position, String name, int state, int statenum, float activationDistance,
			Player player) {
		super(ID, position, name);
		this.state = state;
		this.activationDistance = activationDistance;
		this.player = player;
		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; // Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Switch(vals.str("type"), pos, vals.str("name"), vals.in("state"), vals.in("statenum"),
				vals.fl("activationDistance"), GameManager.player);
	}

	@Override
	public void calculate() {
		if (onTimer != null) {
			onTimer.update();
		}

		if (mouseHovered() && Input.primaryButtonDown && getMouseDistance() <= activationDistance) {
			interact();
		}
		calcFrame();
		changed = false;
	}

	@Override
	public void interact() {
		state++;
		if (state >= statenum) {
			state = 0;
		}
		changed = true;
	}

	@Override
	public float getMouseDistance() {
		Vector2f playerpos = new Vector2f(player.getPosition());
		playerpos.x += player.dim.x / 2;
		playerpos.y += player.dim.y / 2;// center of player
		return Math.sqrt((playerpos.x - position.x) * (playerpos.x - position.x)
				+ (playerpos.y - position.y) * (playerpos.y - position.y));
	}

	@Override
	public boolean mouseHovered() {
		try {
			if (Input.mouseWorldPos.x > position.x && Input.mouseWorldPos.x < position.x + dim.x
					&& Input.mouseWorldPos.y > position.y && Input.mouseWorldPos.y < position.y + dim.y) {
				return true;
			}
			return false;
		} catch (NullPointerException E) {
			System.err.println("Nullpointer Error in Button, First cycle?");
			return false;
		}
	}

}
