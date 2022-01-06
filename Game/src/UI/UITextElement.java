package UI;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Graphics.text.Font;
import Graphics.text.Text;
import Utility.Rect;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class UITextElement extends UIDrawElement {

	private Font font;
	private Color col;

	private Vector2f textDims;

	boolean childrenCentered = true;

	public Vector2f getTextDims() {
		return textDims;
	}

	public UITextElement(String text, Vector2f pos, Vector2f dims) {
		super(pos, dims);

		this.font = Text.mainFont;

		// Generate vertices
		ArrayList<Vector2f> points = new ArrayList<>();
		ArrayList<Vector2f> uvs = new ArrayList<>();

		Text.generateString(text, font, points, uvs);

		Vector2f[] pointArr = new Vector2f[points.size()];
		Vector2f[] uvArr = new Vector2f[uvs.size()];
		for (int i = 0; i < points.size(); i++)
			pointArr[i] = points.get(i);
		for (int i = 0; i < uvs.size(); i++)
			uvArr[i] = uvs.get(i);

		// Get dimensions
		textDims = Rect.getDimsFromPointCollection(points);
		System.out.println(textDims);

		// Build renderer
		col = new Color(1, 1, 1, 1);

		GeneralRenderer genRend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		genRend.init(new ProjectedTransform(new Vector2f(), ProjectedTransform.MatrixMode.SCREEN), pointArr, uvArr,
				col);
		genRend.spr = font.tex;
		rend = genRend;
	}

	public void updateText(String text) {
		// Generate vertices
		ArrayList<Vector2f> points = new ArrayList<>();
		ArrayList<Vector2f> uvs = new ArrayList<>();

		Text.generateString(text, font, points, uvs);

		Vector2f[] pointArr = new Vector2f[points.size()];
		Vector2f[] uvArr = new Vector2f[uvs.size()];

		for (int i = 0; i < points.size(); i++) {
			pointArr[i] = points.get(i);
		}
		for (int i = 0; i < uvs.size(); i++)
			uvArr[i] = uvs.get(i);

		GeneralRenderer genRend = (GeneralRenderer) rend;
		genRend.rebuildMesh(pointArr, uvArr, col);
	}

	public Matrix4f genChildL2WMat() {
		Matrix4f mat = super.genChildL2WMat();
		if (childrenCentered) {
			float shift = (dims.x - textDims.x) / 2;
			mat.translate(shift, 0, 0);
		}

		mat.translate(0, -font.ascent, 0);

		return mat;
	}
}
