#version 330

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;

void main() {
	fragColor = texture(ourTexture, TexCord);
}