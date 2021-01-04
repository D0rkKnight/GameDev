package Graphics.Rendering;

public class GrassShader extends TimedShader {

	protected GrassShader(String filename) {
		super(filename);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("WPos");
	}

	public static GrassShader genShader(String filename) {
		return (GrassShader) cacheShader(filename, (fname) -> {
			return new GrassShader(fname);
		});
	}
}
