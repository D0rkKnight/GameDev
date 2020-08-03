#version 330

out vec4 fragColor;

in vec2 TexCord;
in vec3 vertexColor;

uniform sampler2D ourTexture;

void main() {
	vec4 secondCol = vec4(vertexColor, 1.0);
	vec4 texCol = texture(ourTexture, TexCord);
	float blend = 0.5;
	
 	fragColor = (texCol * (1-blend)) + (secondCol * blend);
}