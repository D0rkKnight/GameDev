package Rendering;

import static org.lwjgl.opengl.GL20.glBindAttribLocation;

public class SpriteShader extends Shader {

	public SpriteShader(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void bindAttributes() {
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "texCords");
		glBindAttribLocation(program, 2, "color");
	}
}
