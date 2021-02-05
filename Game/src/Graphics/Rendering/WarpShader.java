package Graphics.Rendering;

public class WarpShader extends TimedShader {

	protected WarpShader(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initUniforms() throws Exception {
		super.initUniforms();

		createUniform("viewport");
	}

	public static WarpShader genShader(String filename) {
		return (WarpShader) cacheShader(filename, (fname) -> {
			return new WarpShader(fname);
		});
	}
}
