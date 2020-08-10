package Rendering;

import static org.lwjgl.opengl.GL21.*;

/**
 * This class isn't used for anything.
 * @author Hanzen Shou
 *
 */
public class ColorShader extends Shader{

	public ColorShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {
		// TODO Auto-generated method stub
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "color");
	}

	@Override
	protected void initUniforms() {
		// TODO Auto-generated method stub
		
	}

}
