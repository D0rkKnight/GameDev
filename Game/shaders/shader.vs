#version 330

attribute vec3 vertices;

uniform mat4 MVP;

void main() {
	gl_Position = MVP * vec4(vertices, 1.0);
}