package Entities;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Debugging.Debug;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import Entities.PlayerPackage.Player;
import GameController.EntityData;
import Graphics.Rendering.GeneralRenderer;
import Utility.Timers.Timer;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Switch extends Entity implements Interactive {
	public int state;
	public int statenum;
	public boolean changed;
	float activationDistance;
	public Timer onTimer;

	public Switch(String ID, Vector2f position, String name, int state, int statenum, float activationDistance) {
		super(ID, position, name);
		this.state = state;
		this.activationDistance = activationDistance;
		// Configure the renderer real quick
		dim = new Vector2f(30f, 30f);
		GeneralRenderer rendTemp = (GeneralRenderer) this.renderer; // Renderer has been duplicated by now
		rendTemp.init(new ProjectedTransform(position), dim, Shape.ShapeEnum.SQUARE, new Color());

		rendTemp.spr = Debug.debugTex;
		renderer = rendTemp;
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Switch(vals.str("type"), pos, vals.str("name"), vals.in("state"), vals.in("statenum"),
				vals.fl("activationDistance"));
	}

	@Override
	public void calculate() {
		if (onTimer != null) {
			onTimer.update();
		}

		calcFrame();
		changed = false;
	}

	@Override
	public void interact(Player p) {
		state++;
		if (state >= statenum) {
			state = 0;
		}
		changed = true;
	}
}
