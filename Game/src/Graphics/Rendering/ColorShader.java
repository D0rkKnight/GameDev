package Graphics.Rendering;

import static org.lwjgl.opengl.GL20.glBindAttribLocation;

public class ColorShader extends Shader {

	public ColorShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {
		glBindAttribLocation(program, 0, "vertices");
	}

	@Override
	protected void initUniforms() {
		try {
			createUniform("MVP");
			createUniform("Color");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
