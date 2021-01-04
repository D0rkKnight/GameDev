package Graphics.Rendering;

/**
 * Pretty much does nothing TODO: Organize these some time
 * 
 * @author Hanzen Shou
 *
 */

public class SimpleShader extends Shader {

	protected SimpleShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {

	}

	@Override
	protected void initUniforms() throws Exception {
	}

	public static SimpleShader genShader(String filename) {
		return (SimpleShader) cacheShader(filename, (fname) -> {
			return new SimpleShader(fname);
		});
	}
}
