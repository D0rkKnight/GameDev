package Graphics.Rendering;

public class TimedShader extends SpriteShader {

	protected TimedShader(String filename) {
		super(filename);
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("Time");
	}

	public static TimedShader genShader(String filename) {
		return (TimedShader) cacheShader(filename, (fname) -> {
			return new TimedShader(fname);
		});
	}
}
