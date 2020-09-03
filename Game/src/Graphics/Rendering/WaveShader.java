package Graphics.Rendering;

public class WaveShader extends SpriteShader {

	public WaveShader(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	protected void initUniforms() throws Exception {
		super.initUniforms();
		
		createUniform("Time");
	}
}
