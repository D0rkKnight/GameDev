package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Utility.Transformation;
import Wrappers.Color;
import text.Text;

public class UITextElement extends UIDrawElement {

	public UITextElement(String text, Vector2f pos, Vector2f dims) {
		super(pos, dims);

		// Generate vertices
		ArrayList<Vector2f> points = new ArrayList<>();
		ArrayList<Vector2f> uvs = new ArrayList<>();

		Text.generateString(text, Text.mainFont, points, uvs);

		Vector2f[] pointArr = new Vector2f[points.size()];
		Vector2f[] uvArr = new Vector2f[uvs.size()];
		for (int i = 0; i < points.size(); i++)
			pointArr[i] = points.get(i);
		for (int i = 0; i < uvs.size(); i++)
			uvArr[i] = uvs.get(i);

		// Build renderer
		GeneralRenderer genRend = new GeneralRenderer(SpriteShader.genShader("texShader"));
		genRend.init(new Transformation(pos, Transformation.MatrixMode.SCREEN), pointArr, uvArr, new Color(1, 1, 1, 1));
		genRend.spr = Text.mainFont.tex;
		rend = genRend;
	}

}
