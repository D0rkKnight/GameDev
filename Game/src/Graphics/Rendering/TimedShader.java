package Graphics.Rendering;

public class TimedShader extends SpriteShader {

	public TimedShader(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	protected void initUniforms() throws Exception {
		super.initUniforms();
		
		createUniform("Time");
	}
}
