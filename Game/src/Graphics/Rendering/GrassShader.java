package Graphics.Rendering;

public class GrassShader extends TimedShader {

	public GrassShader(String filename) {
		super(filename);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("WPos");
	}
}
