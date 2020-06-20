package GameController;
import java.awt.Canvas;
import java.awt.Graphics;

public class Drawing extends Canvas{
	
	public void paint(Graphics g) {
        g.fillOval(100, 100, 200, 200);
    }
	
	
	public void render() {
		//calls render function of every tile of map within distance of player, and entities within certain distance
	}
}
