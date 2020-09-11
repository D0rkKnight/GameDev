package Graphics.Elements;

import Graphics.Drawer;

public class TileGFX {
	public String name;

	public TileGFX(String name) {
		this.name = name;
	}

	public void writeToBuffer(int buffW, int buffH) {

		DrawBuffer dBuff = Drawer.requestGFXLayer(name, buffW, buffH);

		// TODO: Bake into draw buffer, render that buffer, and then do cool effects.
		// Also learn how to use a depth mask or whatever that's called.

	}
}
