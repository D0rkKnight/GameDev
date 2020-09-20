package Graphics.Elements;

import org.joml.Vector2f;

import GameController.GameManager;
import Graphics.Drawer;

public class DrawOrderTileLayer extends DrawOrderElement {

	private TileRenderLayer trl;

	public DrawOrderTileLayer(int z, TileRenderLayer trl) {
		super(z);

		this.trl = trl;
	}

	public void tryRender(int xMin, int xMax, int yMin, int yMax, int chunkGridW) {
		if (!trl.isActive)
			return;

		// Draw all of the textures
		for (int i = xMin; i < xMax; i++) {
			for (int j = yMin; j < yMax; j++) {
				DrawBuffer dBuff = trl.usedDrawBuffers[i + (j * chunkGridW)];
				if (!dBuff.isActive)
					continue;

				Vector2f pos = new Vector2f(i, j).mul(GameManager.tileSize).mul(Drawer.CHUNK_SIZE);
				dBuff.renderAt(pos);
			}
		}
	}

}
