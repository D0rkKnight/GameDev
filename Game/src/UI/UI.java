package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import Wrappers.Color;

public class UI {
	private static UIBarElement healthBar;
	
	public static ArrayList<UIElement> elements;
	
	public static void init() {
		elements = new ArrayList<>();
		
		healthBar = new UIBarElement(GameManager.renderer, new Vector2f(10, 10), new Vector2f(200, 20), new Color(0.3f, 0.3f, 1, 1));
		healthBar.rend.transform.pos.y += healthBar.dims.y;
		elements.add(healthBar);
	}
	
	public static void render() {
		healthBar.fillRatio = ((float)(GameManager.getFrameTime()%1000))/1000;
		
		for (UIElement e : elements) e.update();
		for (UIElement e : elements) e.render();
	}
}
