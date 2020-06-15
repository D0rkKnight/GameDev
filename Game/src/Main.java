import java.awt.Canvas;

import javax.swing.JFrame;

public class Main {
	
	static JFrame frame;
	static Canvas canvas;
	
	public static void main(String[] args) {
		System.out.println("hellow rold");
		
		frame = new JFrame();
		
		canvas = new Drawing();
		canvas.setSize(1280, 720);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);
		
	}
}
