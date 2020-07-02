#version 330

out vec4 fragColor;

in vec2 TexCord;

uniform sampler2D ourTexture;

void main() {
	fragColor = texture(ourTexture, TexCord);
}