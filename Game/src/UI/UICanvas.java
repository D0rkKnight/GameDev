package UI;

import java.util.ArrayList;

import org.joml.Vector2f;

public class UICanvas extends UIElement{
	
	ArrayList<UIElement> children;
	
	public UICanvas(Vector2f pos) {
		super(pos);
		children = new ArrayList<>();
		// TODO Auto-generated constructor stub
	}
	
	public void render() {
		super.render();
		for (UIElement c : children) c.render();
	}
	
	public void update() {
		super.update();
		for (UIElement c : children) c.update();
	}
}
