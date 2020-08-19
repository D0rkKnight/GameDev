package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

import GameController.GameManager;
import Wrappers.Color;
import Wrappers.Stats;

public class UI {
	private static UIBarElement healthBar;
	private static UIBarElement staminaBar;
	
	public static ArrayList<UIElement> elements;
	
	public static void init() {
		elements = new ArrayList<>();
		
		healthBar = new UIBarElement(GameManager.renderer, new Vector2f(10, 10), new Vector2f(200, 20), new Color(0.3f, 0.3f, 1, 1));
		healthBar.rend.transform.pos.y += healthBar.dims.y;
		elements.add(healthBar);
		staminaBar = new UIBarElement(GameManager.renderer, new Vector2f(10, 35), new Vector2f(200, 20), new Color(0.3f, 1, 1, 1));
		staminaBar.rend.transform.pos.y += staminaBar.dims.y;
		elements.add(staminaBar);
	}
	
	public static void render() {
		Stats pStats = GameManager.player.stats;
		healthBar.fillRatio = ((float)pStats.health) / ((float)pStats.maxHealth);
		staminaBar.fillRatio = ((float)pStats.stamina) / ((float)pStats.maxStamina);
		
		
		for (UIElement e : elements) e.update();
		for (UIElement e : elements) e.render();
	}
}
