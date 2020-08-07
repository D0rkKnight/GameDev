package Debug;

import Rendering.Shader;

public abstract class DebugElement {
	public int lifespan;
	
	public abstract void render(Shader shader);
}
