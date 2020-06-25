package Shaders;
import static org.lwjgl.opengl.GL46C.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
 * Only one copy of these should exist for every distinct shader.
 * TODO: Write vertex shader, write fragment shader, figure out how to pass in attributes and render.
 */
public class Shader {
	private int program;
	private int vs;
	private int fs;
	
	public Shader(String filename) {
		program = glCreateProgram();
		
		//Vertex shader
		vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, readFile(filename+".vs"));
		glCompileShader(vs);
		
		//Error detection
		if (glGetShaderi(vs, GL_COMPILE_STATUS) != 1) {
			System.err.println(glGetShaderInfoLog(vs));
			System.exit(1);
		}
		
		
		//Fragment shader
		fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, readFile(filename+".fs"));
		glCompileShader(fs);
		
		//Error detection
		if (glGetShaderi(fs, GL_COMPILE_STATUS) != 1) {
			System.err.println(glGetShaderInfoLog(fs));
			System.exit(1);
		}
		
		
		
		//Attach shaders to program
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		
		//Attributes
		//The integer is the attribute id and identifies what data will be sent.
		//The string is the semantic and identifies how the attribute will be referred to within the shader.
		glBindAttribLocation(program, 0, "vertices");
		
		
		//Link and validate
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) != 1) {
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		glValidateProgram(program);
		if (glGetProgrami(program, GL_VALIDATE_STATUS) != 1) {
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
	}
	
	public void bind() {
		glUseProgram(program);
	}
	
	/*
	 * Shader interpreter
	 */
	private String readFile(String filename) {
		StringBuilder string = new StringBuilder();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File("./shaders/"+filename)));
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
}
