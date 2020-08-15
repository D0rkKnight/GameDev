package Rendering;

import static org.lwjgl.opengl.GL21.*;

public class ColorShader extends Shader{

	public ColorShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {
		// TODO Auto-generated method stub
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "color"); //TODO: Attribute not being used right now
	}

	@Override
	protected void initUniforms() {
		// TODO Auto-generated method stub
		try {
			createUniform("MVP");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
