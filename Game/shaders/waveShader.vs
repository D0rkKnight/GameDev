#version 330

attribute vec3 vertices;
attribute vec2 texCords;
attribute vec4 color;

uniform mat4 MVP;

out vec2 TexCord;
out vec4 vertexColor;
out vec4 pos;

void main() {
	gl_Position = MVP * vec4(vertices, 1.0);
	TexCord = texCords;
	vertexColor = color;
	
	pos = gl_Position;
}