package UI;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import GameController.Camera;
import GameController.GameManager;
import Graphics.Drawer;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.SpriteShader;
import Wrappers.Color;
import Wrappers.Stats;

public class UI {
	private static HashMap<CanvasEnum, UICanvas> canvases;

	public static enum CanvasEnum {
		NONE, RUNNING, PAUSED, DIALOGUE;
	}

	private static CanvasEnum currCanvas = CanvasEnum.NONE;

	private static UICanvas runningState;
	private static UIBarCanvas healthBar;
	private static UIBarCanvas staminaBar;

	private static UICanvas dialogueState;
	private static UITextBox dialogueBox;

	private static UICanvas pausedState;

	public static ArrayList<UIElement> elements;

	public static void init() {
		elements = new ArrayList<>();
		canvases = new HashMap<>();

		// Initializing the running game state
		runningState = new UICanvas(new Vector2f(), Camera.main.viewport);

		Stats pStats = GameManager.player.stats;
		healthBar = new UIBarCanvas(new GeneralRenderer(SpriteShader.genShader("texShader")), new Vector2f(10, 10),
				new Vector2f(200, 20), new Color(0.3f, 0.3f, 1, 1));
		healthBar.setAnchor(UIBoxElement.ANCHOR_UL);
		healthBar.setUpdateCb(() -> {
			healthBar.bar.fillRatio = (pStats.health) / (pStats.maxHealth);
		});
		runningState.addElement(healthBar);

		staminaBar = new UIBarCanvas(new GeneralRenderer(SpriteShader.genShader("texShader")), new Vector2f(10, 35),
				new Vector2f(200, 20), new Color(0.3f, 1, 1, 1));
		staminaBar.setAnchor(UIBoxElement.ANCHOR_UL);
		staminaBar.setUpdateCb(() -> {
			staminaBar.bar.fillRatio = (pStats.stamina) / (pStats.maxStamina);
		});
		runningState.addElement(staminaBar);

		UITextElement sampleText = new UITextElement("Testing", new Vector2f(10, 60), new Vector2f(300, 30));
		runningState.addElement(sampleText);

		canvases.put(CanvasEnum.RUNNING, runningState);

		// Initialize paused game state
		pausedState = new UICanvas(new Vector2f(), Camera.main.viewport);

		UIBoxElement box = new UIBoxElement(new Vector2f(Camera.main.viewport).div(2), new Vector2f(300, 400),
				new Color(0.5f, 0.5f, 0.5f, 1));
		box.setAnchor(UIBoxElement.ANCHOR_MID);

		pausedState.addElement(box);

		UIButtonElement exitButton = new UIButtonElement(new Vector2f(20, 20), new Vector2f(200, 50),
				new Color(0f, 0f, 0f, 1));
		exitButton.setClickCb(() -> {
			glfwSetWindowShouldClose(Drawer.window, true);
		});
		exitButton.setAnchor(UIBoxElement.ANCHOR_UL);

		box.addElement(exitButton);

		canvases.put(CanvasEnum.PAUSED, pausedState);

		changeCanvas(CanvasEnum.RUNNING);

		// Dialogue popup
		dialogueState = new UICanvas(new Vector2f(), Camera.main.viewport);

		Vector2f tbPos = new Vector2f(10, Camera.main.viewport.y - 300);
		Vector2f tbDims = new Vector2f(Camera.main.viewport.x - 20, Camera.main.viewport.y - tbPos.y - 10);
		dialogueBox = new UITextBox(tbPos, tbDims, "DIALOGUE BOX EMPTY");
		dialogueBox.setAnchor(UIBoxElement.ANCHOR_UL);
		dialogueState.addElement(dialogueBox);

		canvases.put(CanvasEnum.DIALOGUE, dialogueState);
	}

	public static void changeCanvas(CanvasEnum newId) {
		if (currCanvas != CanvasEnum.NONE)
			elements.remove(canvases.get(currCanvas));

		if (newId != CanvasEnum.NONE)
			elements.add(canvases.get(newId));

		currCanvas = newId;
	}

	public static CanvasEnum getCurrCanvas() {
		return currCanvas;
	}

	public static void render() {
		for (UIElement e : elements)
			e.update();
		for (UIElement e : elements)
			e.render();
	}

	public static void showTextBox(String text) {
		dialogueBox.text.updateText(text);

		changeCanvas(CanvasEnum.DIALOGUE);
	}

}
