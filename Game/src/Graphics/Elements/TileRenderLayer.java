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
import Graphics.Rendering.GeneralRenderer;
import Tiles.Tile;
import Utility.Transformation;
import Wrappers.Color;

public class TileRenderLayer {
	public ArrayList<Tile[][]> chunkRendGrid;

	private ArrayList<DrawBuffer> masterBufferList;
	public DrawBuffer[] usedDrawBuffers;
	public boolean isActive = false;

	public TileRenderLayer() {
		chunkRendGrid = new ArrayList<>();

		masterBufferList = new ArrayList<>();
	}

	public void populateChunks(HashMap<String, Tile[][]> g, ArrayList<String> renderedLayers, GeneralRenderer rend) {
		loadLayers(g, renderedLayers);
		if (isActive)
			drawChunks(rend);
	}

	public void loadLayers(HashMap<String, Tile[][]> g, ArrayList<String> renderedLayers) {
		clearGrids();
		isActive = false;

		for (String str : renderedLayers) {
			appendSingleGrid(g.get(str));
		}
	}

	public void appendSingleGrid(Tile[][] grid) {
		chunkRendGrid.add(grid);
		isActive = true;
	}

	public void clearGrids() {
		chunkRendGrid.clear();
	}

	public void drawChunks(GeneralRenderer rend) {
		Tile[][] tArr = chunkRendGrid.get(0);

		int w = tArr.length;
		int h = tArr[0].length;

		int cw = (int) Math.ceil(((float) w) / Drawer.CHUNK_SIZE);
		int ch = (int) Math.ceil(((float) h) / Drawer.CHUNK_SIZE);

		// TODO: There's still some leakage if the draw buffer is resized smaller.
		usedDrawBuffers = new DrawBuffer[cw * ch];

		// Populate master buffer list up to necessary size
		int chunkDims = Drawer.CHUNK_SIZE * GameManager.tileSize;
		while (masterBufferList.size() < cw * ch)
			masterBufferList.add(DrawBuffer.genEmptyBuffer(chunkDims, chunkDims, rend));

		for (int i = 0; i < cw; i++) {
			for (int j = 0; j < ch; j++) {
				int index = i + (j * cw);

				// Now pull from the master list to piece together the necessary buffers for
				// this map.
				DrawBuffer dBuff = masterBufferList.get(index);

				usedDrawBuffers[index] = dBuff;
				glBindFramebuffer(GL_FRAMEBUFFER, dBuff.fbuff);

				// Clear the buffer
				Color.setGLClear(new Color(0, 0, 0, 0)); // Completely transparent
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

				// Now iterate over every tile in this grid that lies within the chunk
				for (Tile[][] layer : chunkRendGrid) {

					int aMax = Math.min((i + 1) * Drawer.CHUNK_SIZE, w);
					for (int a = i * Drawer.CHUNK_SIZE; a < aMax; a++) {

						int bMax = Math.min((j + 1) * Drawer.CHUNK_SIZE, h);
						for (int b = j * Drawer.CHUNK_SIZE; b < bMax; b++) {
							Tile t = layer[a][b];
							if (t == null)
								continue;

							Transformation oldTrans = t.renderer.transform;

							Transformation newTrans = new Transformation(new Vector2f(oldTrans.pos),
									Transformation.MatrixMode.STATIC);
							t.renderer.transform = newTrans;

							float offsetX = i * GameManager.tileSize * Drawer.CHUNK_SIZE;
							float offsetY = j * GameManager.tileSize * Drawer.CHUNK_SIZE;

							Vector2f camOffset = new Vector2f(Camera.main.viewport).div(2);

							// Need to center the image so that the texture is drawn to from the bottom left
							newTrans.view.setTranslation(-offsetX - camOffset.x, -offsetY - camOffset.y, 0);

							float x = a * GameManager.tileSize;
							float y = b * GameManager.tileSize;

							t.render(new Vector2f(x, y), GameManager.tileSize);
							dBuff.isActive = true; // Draw buffer gets activated

							t.renderer.transform = oldTrans;
						}
					}
				}
			}
		}

		// Free the frame buffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
}
