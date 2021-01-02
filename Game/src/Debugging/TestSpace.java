package Debugging;

import Graphics.Elements.SubTexture;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;

public class TestSpace {
	public static void test() {
		TextureAtlas tAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/props.png"), 32, 48);

		for (SubTexture st : tAtlas.genSubTexSet(0, 0, 2, 2)) {
			System.out.println(st.x + "\n" + st.y + "\n" + st.w + "\n" + st.h + "\n");
		}
	}
}
