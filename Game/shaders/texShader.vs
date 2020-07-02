#version 330

attribute vec3 vertices;
attribute vec2 texCords;

out vec2 TexCord;

void main() {
	gl_Position = vec4(vertices, 1.0);
	TexCord = texCords;
}