#version 330

attribute vec3 vertices;

uniform mat4 MVP;
uniform vec4 Color;

out vec4 col;

void main() {
	gl_Position = MVP * vec4(vertices, 1.0);
	col = Color;
}