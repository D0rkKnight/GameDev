package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import GameController.Input;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Utility.Transformation;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;

public class Button extends Entity implements Interactive {
	public int state;
	public boolean changed;
	long timeOn;
	float activationDistance;
	Timer onTimer;
	Player player;

	public Button(String ID, Vector2f position, Renderer renderer, String name, int state, long timeOn,
			float activationDistance, Player player) {
		super(ID, position, renderer, name);
		this.state = state;
		this.timeOn = timeOn;
		this.activationDistance = activationDistance;
		this.player = player;
		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		((GeneralRenderer) this.renderer).init(new Transformation(position), dim, Shape.ShapeEnum.SQUARE,
				new Color());
		((GeneralRenderer) this.renderer).spr = Debug.debugTex;
	}

	public Button createNew(float xPos, float yPos, Player player) {
		return new Button(ID, new Vector2f(xPos, yPos), renderer, name, state, timeOn, activationDistance, player);
	}

	@Override
	public Button createNew(float xPos, float yPos) {
		return createNew(xPos, yPos, player);
	}

	@Override
	public void calculate() {
		if (onTimer != null) {
			onTimer.update();
		}

		if (state == 1) {
		}

		if (mouseHovered() && Input.primaryButtonDown && getMouseDistance() <= activationDistance) {
			interact();
		}
		calcFrame();
		changed = false;
	}

	@Override
	public void interact() {
		state = 1;
		changed = true;
		onTimer = new Timer(timeOn, new TimerCallback() {
			@Override
			public void invoke(Timer timer) {
				state = 0;
				changed = true;
				onTimer = null;

			}
		});
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
