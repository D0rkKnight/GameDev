package Entities;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import Entities.PlayerPackage.PlayerFramework;
import GameController.EntityData;
import GameController.Input;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Button extends Entity implements Interactive {
	public int state;
	public boolean changed;
	long timeOn;
	float activationDistance;
	Timer onTimer;
	PlayerFramework player;

	public Button(String ID, Vector2f position, String name, int state, long timeOn, float activationDistance) {
		super(ID, position, name);
		this.state = state;
		this.timeOn = timeOn;
		this.activationDistance = activationDistance;
		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		this.renderer = rend;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Button(vals.str("type"), pos, vals.str("name"), vals.in("state"), vals.in("timeOn"),
				vals.fl("activationDistance"));
	}

	@Override
	public void calculate() {
		if (onTimer != null) {
			onTimer.update();
		}

		if (state == 1) {
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
