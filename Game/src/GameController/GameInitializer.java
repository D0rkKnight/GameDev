package GameController;
import java.awt.Canvas;

import javax.swing.JFrame;
public class GameInitializer {
	private JFrame frame;
	private Canvas canvas;
	
	public GameInitializer() {
		
	}
	
	public void initgame() {
		frame = new JFrame();
		
		canvas = new Drawing();
		canvas.setSize(1280, 720);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);
		
	}
}
