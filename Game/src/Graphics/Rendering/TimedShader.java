package Graphics.Rendering;

public class TimedShader extends SpriteShader {

	public TimedShader(String filename) {
		super(filename);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("Time");
	}
}
