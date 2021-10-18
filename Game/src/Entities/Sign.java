package Entities;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Entities.Framework.Entity;
import Entities.Framework.Interactive;
import Entities.PlayerPackage.Player;
import GameController.Dialogue;
import GameController.EntityData;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class Sign extends Entity implements Interactive {

	ArrayList<String> text;

	public Sign(String ID, Vector2f position, String name, ArrayList<String> text) {
		super(ID, position, name);
		this.text = text;

		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 48, 48);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(position), new Vector2f(48, 48), Shape.ShapeEnum.SQUARE, new Color(0, 0, 0, 0),
				tAtlas.genSubTex(0, 1));
		rend.spr = tAtlas.tex;
		this.renderer = rend;
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub

	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		// Pull out text by looking at attribute names
		ArrayList<String> text = new ArrayList<>();
		ArrayList<Integer> loadIndices = new ArrayList<>();
		for (String key : vals.d.keySet()) {
			if (key.contains("text")) {
				int index = Integer.parseInt(key.split("text")[1]);

				// Insert data in an ordered manner
				for (int i = 0; i < text.size() + 1; i++) {
					if (i >= text.size() || index < loadIndices.get(i)) {
						text.add(i, vals.str(key));
						loadIndices.add(i, index);

						break;
					}
				}
			}
		}

		return new Sign(vals.str("type"), pos, vals.str("name"), text);
	}

	@Override
	public void onGameLoad() {
	}

	@Override
	public void interact(Player p) {
		Dialogue.loadDialogue(text);
		p.canMove = false;
	}
}
