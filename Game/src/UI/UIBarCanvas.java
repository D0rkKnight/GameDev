package UI;

import org.joml.Vector2f;

import Rendering.Renderer;
import Wrappers.Color;

public class UIBarCanvas extends UICanvas{
	
	public UIBoxElement bg;
	public UIBarElement bar;
	
	public UIBarCanvas(Renderer rend, Vector2f pos, Vector2f dims, Color col) {
		super(pos);
		
		bg = new UIBoxElement(rend, pos, dims, new Color(0.1f, 0.1f, 0.1f, 1));
		bar = new UIBarElement(rend, pos, dims, col);
		
		children.add(bg);
		children.add(bar);
	}
	
	public void update() {
		super.update();
		
		bg.pos = this.pos;
		bar.pos = this.pos;
	}
}
