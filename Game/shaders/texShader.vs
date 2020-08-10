#version 330

attribute vec3 vertices;
attribute vec2 texCords;
attribute vec3 color;

uniform mat4 MVP;

out vec2 TexCord;
out vec3 vertexColor;

void main() {
	gl_Position = MVP * vec4(vertices, 1.0);
	//gl_Position = vec4(vertices, 1.0); 
	TexCord = texCords;
	vertexColor = color;
}