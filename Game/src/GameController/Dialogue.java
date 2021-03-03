package GameController;

import java.util.ArrayList;

import UI.UI;

public class Dialogue {
	public static ArrayList<String> text;
	public static int index = 0;

	public static void loadDialogue(ArrayList<String> text) {
		Dialogue.text = text;
		index = 0;

		UI.showTextBox(text.get(0));
	}

	public static void advanceDialogue() {
		if (text == null) {
			System.err.println("No dialogue to advance");
			System.exit(1);
		}

		index++;

		if (index < text.size())
			UI.showTextBox(text.get(index));
		else {
			UI.changeCanvas(UI.CanvasEnum.RUNNING);

			text = null;
			index = 0;

			// Return player control
			GameManager.player.canMove = true;
		}
	}
}
