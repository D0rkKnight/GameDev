package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import Wrappers.Color;
import Wrappers.Stats;

public class UI {
	private static UIBarCanvas healthBar;
	private static UIBarCanvas staminaBar;
	
	public static ArrayList<UIElement> elements;
	
	public static void init() {
		elements = new ArrayList<>();
		
		healthBar = new UIBarCanvas(GameManager.renderer, new Vector2f(10, 10), new Vector2f(200, 20), new Color(0.3f, 0.3f, 1, 1));
		healthBar.pos.y += healthBar.bar.dims.y;
		elements.add(healthBar);
		staminaBar = new UIBarCanvas(GameManager.renderer, new Vector2f(10, 35), new Vector2f(200, 20), new Color(0.3f, 1, 1, 1));
		staminaBar.pos.y += staminaBar.bar.dims.y;
		elements.add(staminaBar);
	}
	
	public static void render() {
		Stats pStats = GameManager.player.stats;
		healthBar.bar.fillRatio = ((float)pStats.health) / ((float)pStats.maxHealth);
		staminaBar.bar.fillRatio = ((float)pStats.stamina) / ((float)pStats.maxStamina);
		
		
		for (UIElement e : elements) e.update();
		for (UIElement e : elements) e.render();
	}
}
