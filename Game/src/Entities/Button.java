package Entities;

import org.joml.Math;
import org.joml.Vector2f;

import Collision.HammerShape;
import Debug.Debug;
import GameController.Input;
import Wrappers.Color;
import Wrappers.Timer;
import Wrappers.TimerCallback;
import Rendering.GeneralRenderer;
import Rendering.Renderer;
import Rendering.Transformation;

public class Button extends Entity implements Interactive {
	public boolean state;
	public boolean changed;//state has changed, lasts for one frame 
	long timeOn;
	float activationDistance;
	Timer onTimer;
	Player player;

	public Button(int ID, Vector2f position, Renderer renderer, String name, boolean state, long timeOn,
			float activationDistance, Player player) {
		super(ID, position, renderer, name);
		this.state = state;
		this.timeOn = timeOn;
		this.activationDistance = activationDistance;
		this.player = player;
		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; // Renderer has been duplicated by now
		rendTemp.init(new Transformation(position), dim, HammerShape.HAMMER_SHAPE_SQUARE, new Color());

		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
	}

	@Override
	public void calculate() {
		if(onTimer != null) {
			onTimer.update();
		}
		
		if(state) {
			System.out.println("buttonon");
		}
		
		if (mouseHovered() && Input.primaryButtonDown && getMouseDistance() <= activationDistance) {
			interact();
		}
		calcFrame();
		changed = false;
	}

	@Override
	protected void calcFrame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void controlledMovement() {
		// buttons don't move silly

	}

	@Override
	public Entity clone() {
		try {
			return new Button(ID, new Vector2f(position), renderer.clone(), name, state, timeOn, activationDistance,
					player);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Entity clone(float xPos, float yPos) {
		try {
			return new Button(ID, new Vector2f(xPos, yPos), renderer.clone(), name, state, timeOn, activationDistance,
					player);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Entity clone(float xPos, float yPos, Player player) {
		try {
			return new Button(ID, new Vector2f(xPos, yPos), renderer.clone(), name, state, timeOn, activationDistance,
					player);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void interact() {
		state = true;
		changed = true;
		System.out.println("interact");
		onTimer = new Timer(timeOn, new TimerCallback() {
			@Override
			public void invoke(Timer timer) {
				state = false;
				changed = true;
				onTimer = null;
				
			}
		});
	}

	@Override
	public float getMouseDistance() {
		Vector2f playerpos = new Vector2f(player.getPosition());
		playerpos.x += player.dim.x / 2;
		playerpos.y += player.dim.y / 2;//center of player
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
			System.out.println("Nullpointer Error in Button, First cycle?");
			return false;
		}
	}

}
