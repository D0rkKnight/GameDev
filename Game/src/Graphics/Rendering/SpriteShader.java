package Graphics.Rendering;

import static org.lwjgl.opengl.GL20.glBindAttribLocation;

public class SpriteShader extends Shader {

	protected SpriteShader(String filename) {
		super(filename);
	}

	@Override
	protected void bindAttributes() {
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "texCords");
		glBindAttribLocation(program, 2, "color");
	}

	@Override
	protected void initUniforms() throws Exception {
		createUniform("MVP");
	}

	public static SpriteShader genShader(String filename) {
		return (SpriteShader) cacheShader(filename, (fname) -> {
			return new SpriteShader(fname);
		});
	}
}
