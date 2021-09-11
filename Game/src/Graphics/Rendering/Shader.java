package Graphics.Rendering;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import Wrappers.Color;

/*
 * Only one copy of these should exist for every distinct shader.
 * TODO: Retain a singleton design, like Texture. Should check against bank of shaders when trying to create a new one.
 */
public abstract class Shader {
	protected int program;
	private int vs;
	private int fs;
	protected Map<String, Integer> uniforms;

	private static HashMap<String, Shader> shaderCache; // Key: filename Value: Shader object

	static {
		shaderCache = new HashMap<>();
	}

	/**
	 * Logs shader into cache system to prevent repeated compilations
	 * 
	 * @param fname
	 * @param cons
	 * @return
	 */
	protected static Shader cacheShader(String fname, ShaderCacheConstructor cons) {
		if (shaderCache.containsKey(fname)) {
			return shaderCache.get(fname);
		} else {
			Shader newShader = cons.invoke(fname);

			shaderCache.put(fname, newShader);
			return newShader;
		}
	}

	public static Shader genShader(String filename) {
		System.err.println("genShader() override not implemented");
		return null;
	}

	protected Shader(String filename) {
		program = glCreateProgram();

		// Vertex shader
		vs = glCreateShader(GL_VERTEX_SHADER);

		String raw = readFile(filename + ".vs");

		glShaderSource(vs, raw);
		glCompileShader(vs);

		// Error detection
		if (glGetShaderi(vs, GL_COMPILE_STATUS) != 1) {
			new Exception(glGetShaderInfoLog(vs)).printStackTrace();
			System.err.println(filename);
			System.exit(1);
		}

		// Fragment shader
		fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, readFile(filename + ".fs"));
		glCompileShader(fs);

		// Error detection
		if (glGetShaderi(fs, GL_COMPILE_STATUS) != 1) {
			new Exception(glGetShaderInfoLog(fs)).printStackTrace();
			System.err.println(filename);
			System.exit(1);
		}

		// Attach shaders to program
		glAttachShader(program, vs);
		glAttachShader(program, fs);

		// Attributes
		// The integer is the attribute id and identifies what data will be sent.
		// The string is the semantic and identifies how the attribute will be referred
		// to within the shader.
		// i.e: glBindAttribLocation(program, 0, "vertices");
		bindAttributes();

		// Link and validate
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) != 1) {
			new Exception(glGetProgramInfoLog(program)).printStackTrace();
			System.err.println(filename);
			System.exit(1);
		}

		glValidateProgram(program);
		if (glGetProgrami(program, GL_VALIDATE_STATUS) != 1) {
			new Exception(glGetProgramInfoLog(program)).printStackTrace();
			System.err.println(filename);
			System.exit(1);
		}

		uniforms = new HashMap<String, Integer>();
		try {
			initUniforms();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void bind() {
		glUseProgram(program);
	}

	protected abstract void bindAttributes();

	protected abstract void initUniforms() throws Exception;

	/*
	 * Shader interpreter
	 */
	private String readFile(String filename) {
		StringBuilder string = new StringBuilder();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File("./shaders/" + filename)));
			String line;
			while ((line = br.readLine()) != null) {
				string.append(line);
				string.append("\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return string.toString();
	}

	public int getId() {
		return program;
	}

	public void createUniform(String name) throws Exception {
		int loc = glGetUniformLocation(program, name);
		if (loc < 0) {
			throw new Exception("Could not find uniform, is it used in the shader?: " + name);
		}

		uniforms.put(name, loc);
	}

	public void setUniform(String name, Matrix4f val) {
		bind();

		try (MemoryStack stack = MemoryStack.stackPush()) {

			FloatBuffer fb = stack.mallocFloat(16);
			val.get(fb);

			glUniformMatrix4fv(uniforms.get(name), false, fb);
		}

	}

	public void setUniform(String name, float f) {
		glUniform1f(uniforms.get(name), f);
	}

	public void setUniform(String name, Color col) {
		glUniform4f(uniforms.get(name), col.r, col.g, col.b, col.a);
	}

	public void setUniform(String name, Vector2f vec2) {
		glUniform2f(uniforms.get(name), vec2.x, vec2.y);
	}

	public void renderStart(Renderer rend) {
		bind();
	}
}
