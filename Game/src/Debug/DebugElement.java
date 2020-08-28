package Debug;

import Rendering.Shader;
import Wrappers.Color;

public abstract class DebugElement {
	public int lifespan;
	public Color col;
	
	public abstract void render(Shader shader);
}
