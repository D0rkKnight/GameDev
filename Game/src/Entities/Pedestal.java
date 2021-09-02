package Entities;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import Entities.PlayerPackage.Player;
import GameController.EntityData;
import GameController.GameManager;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Pedestal extends Entity implements Interactive {

	public Powerup powerup;

	public Pedestal(String ID, Vector2f position, String name) {
		super(ID, position, name);

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 48, 48);
		GeneralRenderer rend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		rend.init(new ProjectedTransform(position), new Vector2f(48, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				tAtlas.genSubTex(1, 0));
		rend.spr = tAtlas.tex;
		this.renderer = rend;

		this.powerup = genPowerup();
	}

	private Powerup genPowerup() {
		// Generate a spinning emblem above me
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 48, 48);
		Powerup powerup = new DoubleJumpPowerup(new Vector2f(position).add(0, 48), tAtlas.tex, tAtlas.genSubTex(0, 2));
		GameManager.subscribeEntity(powerup);

		return powerup;
	}

	@Override
	public void interact(Player p) {
		if (powerup == null)
			return;

		// TODO Auto-generated method stub
		System.out.println("You gained a powerup!");
		powerup.invoke(p);

		powerup.Destroy();
		powerup = null; // Release object
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Pedestal(vals.str("type"), pos, vals.str("name"));
	}
}
