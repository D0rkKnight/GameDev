package Entities;

import org.joml.Vector2f;
import org.joml.Vector3f;

import Entities.Framework.SpinningEmblem;
import Entities.PlayerPackage.Player;
import GameController.Time;
import Graphics.Drawer;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;

public abstract class Powerup extends SpinningEmblem {

	// New strategy: render to a buffer and use that buffer for outlines.
	// TODO: Move this to Drawer.java so this isn't stupid.

	public Powerup(String ID, Vector2f position, String name, Texture tex, SubTexture subtex) {
		super(ID, position, name, tex, subtex);
	}

	public abstract void invoke(Player p);

	@Override
	public void calculate() {
		super.calculate();

		Vector3f wave = new Vector3f();
		wave.y = (float) (Math.sin(Time.timeSinceStart() / 1000f) * 10);
		localTrans.trans.setTranslation(wave);
	}

	@Override
	public void render() {
		super.render();

		Drawer.setCurrBuff(Drawer.DBEnum.OUTLINE);
		renderer.render();
	}
}
