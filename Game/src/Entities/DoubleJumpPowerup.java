package Entities;

import org.joml.Vector2f;

import Entities.PlayerPackage.Player;
import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;

public class DoubleJumpPowerup extends Powerup {

	public DoubleJumpPowerup(String ID, Vector2f position, String name, Texture tex, SubTexture subtex) {
		super(ID, position, name, tex, subtex);
	}

	public DoubleJumpPowerup(Vector2f position, Texture tex, SubTexture subtex) {
		this("DOUBLE_JUMP_PUP", position, "Double Jump Powerup", tex, subtex);
	}

	@Override
	public void invoke(Player p) {
		p.maxJumps++;
	}

}
