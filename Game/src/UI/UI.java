package UI;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2f;

import GameController.ArenaController;
import GameController.Camera;
import GameController.GameManager;
import Graphics.Drawer;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Renderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Wrappers.Color;
import Wrappers.Stats;

public class UI implements NewWaveListener {
	private static HashMap<CEnum, UICanvas> canvases;

	public static enum CEnum {
		NONE, RUNNING, PAUSED, DIALOGUE;

		public UICanvas state;
	}

	private static CEnum currCanvas = CEnum.NONE;

	private static UIBarCanvas healthBar;
	private static UIBarCanvas staminaBar;

	private static UITextBox dialogueBox;
	private static UITextElement waveLabel;

	public static ArrayList<UIElement> elements;

	public static void init() {
		new UI().subscribeSelf();

		elements = new ArrayList<>();

		// Initializing the running game state
		CEnum.RUNNING.state = new UICanvas(new Vector2f(), Camera.main.viewport);

		Stats pStats = GameManager.player.stats;
		healthBar = new UIBarCanvas(new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader")),
				new Vector2f(10, 10), new Vector2f(200, 20), new Color(0.3f, 0.3f, 1, 1));
		healthBar.setAnchor(UIBoxElement.ANCHOR_UL);
		healthBar.setUpdateCb(() -> {
			healthBar.bar.fillRatio = (pStats.health) / (pStats.maxHealth);

			Renderer rend = healthBar.bg.rend;
			System.out.println("\n________________________\n");
			System.out.println("Loc: " + rend.localTrans.genModel());
			System.out.println("L2W: " + rend.genL2WMat());
			System.out.println("W2S: " + rend.worldToScreen.genMVP());

			System.out.println(Camera.main.viewport.x);
		});
		CEnum.RUNNING.state.addElement(healthBar);

		staminaBar = new UIBarCanvas(new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader")),
				new Vector2f(10, 35), new Vector2f(200, 20), new Color(0.3f, 1, 1, 1));
		staminaBar.setAnchor(UIBoxElement.ANCHOR_UL);
		staminaBar.setUpdateCb(() -> {
			staminaBar.bar.fillRatio = (pStats.stamina) / (pStats.maxStamina);
		});
		CEnum.RUNNING.state.addElement(staminaBar);

		waveLabel = new UITextElement("Current Wave: ", new Vector2f(Camera.main.viewport.x / 2, 10),
				new Vector2f(300, 30));
		// waveLabel.relPos.sub(waveLabel.getTextDims().x / 2, 0);

		CEnum.RUNNING.state.addElement(waveLabel);

		// Initialize paused game state
		CEnum.PAUSED.state = new UICanvas(new Vector2f(), Camera.main.viewport);

		UIBoxElement box = new UIBoxElement(new Vector2f(Camera.main.viewport).div(2), new Vector2f(300, 400),
				new Color(0.5f, 0.5f, 0.5f, 1));
		box.setAnchor(UIBoxElement.ANCHOR_MID);

		CEnum.PAUSED.state.addElement(box);

		UIButtonElement exitButton = new UIButtonElement(new Vector2f(20, 20), new Vector2f(200, 50),
				new Color(0f, 0f, 0f, 1));
		exitButton.setClickCb(() -> {
			glfwSetWindowShouldClose(Drawer.window, true);
		});
		exitButton.setAnchor(UIBoxElement.ANCHOR_UL);

		box.addElement(exitButton);

		changeCanvas(CEnum.RUNNING);

		// Dialogue popup
		CEnum.DIALOGUE.state = new UICanvas(new Vector2f(), Camera.main.viewport);

		Vector2f tbPos = new Vector2f(10, Camera.main.viewport.y - 300);
		Vector2f tbDims = new Vector2f(Camera.main.viewport.x - 20, Camera.main.viewport.y - tbPos.y - 10);
		dialogueBox = new UITextBox(tbPos, tbDims, "DIALOGUE BOX EMPTY");
		dialogueBox.setAnchor(UIBoxElement.ANCHOR_UL);
		CEnum.DIALOGUE.state.addElement(dialogueBox);
	}

	private void subscribeSelf() {
		ArenaController.newWaveSubList.add(this);
	}

	public static void changeCanvas(CEnum newId) {
		if (currCanvas != CEnum.NONE)
			elements.remove(currCanvas.state);

		if (newId != CEnum.NONE)
			elements.add(newId.state);

		currCanvas = newId;
	}

	public static CEnum getCurrCanvas() {
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

		changeCanvas(CEnum.DIALOGUE);
	}

	@Override
	public void onNewWave() {
		waveLabel.updateText("Current Wave: " + (ArenaController.currWave + 1));
	}

}
