package GameController;

import Tiles.Tile;
import Wrappers.Position;

/*
 * Calls shaders to render themselves.
 */
public class Renderer {
	private float tileWidth;
	
	Renderer() {
		tileWidth = 0.1f;
	}
	
	public void draw(Map map) {
		//calls render function of every tile of map within distance of player, and entities within certain distance
  		Tile[][] grid = map.getGrid();
  		
  		for (int i=0; i<grid.length; i++) {
  			for (int j=0; j<grid[0].length; j++) {
  				Tile tile = grid[i][j];
  				if (tile == null) continue;
  				
  				tile.render(new Position(i*tileWidth, j*tileWidth), tileWidth);
  			}
  		}
	}
}
