package Graphics.text;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;

public class Text {

	public static Font mainFont;

	public static void init() {
		mainFont = Font.genFont("times.ttf");

		// SOME REFERENCE CODE (USE toArray() RATHER THAN FOR LOOPS)
//		ArrayList<Vector2f> points = new ArrayList<>();
//		ArrayList<Vector2f> uvs = new ArrayList<>();
//
//		generateString("Hello World!", testFont, points, uvs);
//		Vector2f[] pointArr = new Vector2f[points.size()];
//		Vector2f[] uvArr = new Vector2f[uvs.size()];
//
//		for (int i = 0; i < points.size(); i++)
//			pointArr[i] = points.get(i);
//		for (int i = 0; i < uvs.size(); i++)
//			uvArr[i] = uvs.get(i);
//
//		// Render for debugging
//		rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
//		rend.init(new Transformation(new Vector2f(500, 500), Transformation.MatrixMode.SCREEN), pointArr, uvArr,
//				new Color(1, 1, 1, 1));
//		rend.spr = testFont.tex;
	}

	public static void generateString(String str, Font font, ArrayList<Vector2f> points, ArrayList<Vector2f> uvs) {
		float currentPoint = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			TextChar tChar = font.characters[c - font.firstChar];

			// Generate vertex data
			Vector2f[] charPoints = Shape.ShapeEnum.SQUARE.v.getRenderVertices(new Vector2f(tChar.w, tChar.h));

			for (Vector2f v : charPoints)
				v.add(currentPoint + tChar.xOff, tChar.yOff);

			Vector2f[] charUVs = tChar.subTex.genSubUV(Shape.ShapeEnum.SQUARE.v);

			// Enqueue into array
			for (Vector2f v : charPoints)
				points.add(v);
			for (Vector2f v : charUVs)
				uvs.add(v);

			currentPoint += tChar.advance;
		}
	}
}
