package Graphics.Rendering;

import static org.lwjgl.opengl.GL20.glBindAttribLocation;

public class ColorShader extends Shader {

	public ColorShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {
		// TODO Auto-generated method stub
		glBindAttribLocation(program, 0, "vertices");
	}

	@Override
	protected void initUniforms() {
		// TODO Auto-generated method stub
		try {
			createUniform("MVP");
			createUniform("Color");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
