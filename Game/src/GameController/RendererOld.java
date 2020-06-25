package GameController;
import java.awt.Canvas;
import java.awt.Graphics;

import Tiles.Tile;
import Wrappers.Position;


/*
 * TODO: Change over to OpenGL for rendering, it's faster.
 * DEPRECATED
 */
public class RendererOld extends Canvas{
	
	private int tileWidth;
	private Map map;
	
	RendererOld(Map map) {
		super();
		
		tileWidth = 16;
		this.map = map;
	}
	
	/*
	 * Renders stuff, tiles for now.
	 */
	@Override
	public void paint(Graphics g) {
        g.fillOval(100, 100, 200, 200);
        
        //calls render function of every tile of map within distance of player, and entities within certain distance
  		Tile[][] grid = map.getGrid();
  		
  		for (int i=0; i<grid.length; i++) {
  			for (int j=0; j<grid[0].length; j++) {
  				Tile tile = grid[i][j];
  				if (tile == null) continue;
  				
  				//tile.render(g, new Position(i*tileWidth, j*tileWidth));
  			}
  		}
    }
}
