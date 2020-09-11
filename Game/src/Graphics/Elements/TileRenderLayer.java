package Graphics.Elements;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import GameController.Camera;
import GameController.GameManager;
import Graphics.Drawer;
import Tiles.Tile;
import Utility.Transformation;

public class TileRenderLayer {
	public ArrayList<Tile[][]> chunkRendGrid;
	public DrawBuffer[] tileChunkDrawBuffers;

	public TileRenderLayer() {
		chunkRendGrid = new ArrayList<>();
	}

	public void populateChunks(HashMap<String, Tile[][]> g, String[] renderedLayers) {
		chunkRendGrid.clear();

		for (String str : renderedLayers) {
			chunkRendGrid.add(g.get(str));
		}

		Tile[][] tArr = chunkRendGrid.get(0);

		int w = tArr.length;
		int h = tArr[0].length;

		if (w % Drawer.CHUNK_SIZE != 0 || h % Drawer.CHUNK_SIZE != 0) {
			new Exception("Bad size!").printStackTrace();
		}

		int cw = w / Drawer.CHUNK_SIZE;
		int ch = h / Drawer.CHUNK_SIZE;

		// TODO: There's still some leakage if the draw buffer is resized smaller.
		DrawBuffer[] newTCDBuff = new DrawBuffer[cw * ch];

		for (int i = 0; i < cw; i++) {
			for (int j = 0; j < ch; j++) {
				int index = i + (j * cw);
				DrawBuffer dBuff;

				// Don't create new buffers if it's not necessary
				if (tileChunkDrawBuffers != null && index < tileChunkDrawBuffers.length) {
					dBuff = tileChunkDrawBuffers[index];
					if (dBuff == null) {
						new Exception("Draw Buffer Array has empty index?").printStackTrace();
						System.exit(1);
					}
				} else {
					int chunkDims = Drawer.CHUNK_SIZE * GameManager.tileSize;
					dBuff = DrawBuffer.genEmptyBuffer(chunkDims, chunkDims);
				}

				newTCDBuff[index] = dBuff;
				glBindFramebuffer(GL_FRAMEBUFFER, dBuff.fbuff);

				// Clear the buffer
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

				// Now iterate over every tile in this grid that lies within the chunk
				for (Tile[][] layer : chunkRendGrid) {
					for (int a = i * Drawer.CHUNK_SIZE; a < (i + 1) * Drawer.CHUNK_SIZE; a++) {
						for (int b = j * Drawer.CHUNK_SIZE; b < (j + 1) * Drawer.CHUNK_SIZE; b++) {
							Tile t = layer[a][b];
							if (t == null)
								continue;

							Transformation oldTrans = t.renderer.transform;

							Transformation newTrans = new Transformation(new Vector2f(oldTrans.pos),
									Transformation.MATRIX_MODE_STATIC);
							t.renderer.transform = newTrans;

							float offsetX = i * GameManager.tileSize * Drawer.CHUNK_SIZE;
							float offsetY = j * GameManager.tileSize * Drawer.CHUNK_SIZE;

							Vector2f camOffset = new Vector2f(Camera.main.viewport).div(2);
							newTrans.view.setTranslation(-offsetX - camOffset.x, -offsetY - camOffset.y, 0);

							float x = a * GameManager.tileSize;
							float y = b * GameManager.tileSize;
							t.render(new Vector2f(x, y), GameManager.tileSize);

							t.renderer.transform = oldTrans;
						}
					}
				}
			}
		}

		// Assign back to TCDBuff
		tileChunkDrawBuffers = newTCDBuff;

		// Free the frame buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
}
