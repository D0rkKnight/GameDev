package GameController;

import java.util.ArrayList;

import Debug.Debug;
import Entities.Entity;
import Tiles.Tile;
import Wrappers.Vector2;

/*
 * Calls shaders to render themselves.
 */
public class Drawer {
	private float tileWidth;
	
	Drawer() {
		tileWidth = 16f;
	}
	
	public void draw(Map map, ArrayList<Entity> entities) {
		//calls render function of every tile of map within distance of player, and entities within certain distance
  		Tile[][] grid = map.getGrid();
  		
  		for (int i=0; i<grid.length; i++) {
  			for (int j=0; j<grid[0].length; j++) {
  				Tile tile = grid[i][j];
  				if (tile == null) continue;
  				
  				tile.render(new Vector2(i*tileWidth, j*tileWidth), tileWidth);
  			}
  		}
  		
  		for (Entity ent : entities) {
  			ent.render();
  		}
  		
  		//Overlay debug elements
  		Debug.renderDebug();
	}
}
