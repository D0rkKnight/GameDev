#version 330

out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;
in vec4 pos;

uniform sampler2D ourTexture;
uniform float Time;

void main() {
	vec4 texCol = texture(ourTexture, TexCord);
	
//	float blend = 0.5;
//	vec4 secondCol = vec4(vertexColor, 1.0) * blend + (vec4(1, 1, 1, 1) * (1-blend));
//	
// 	fragColor = texCol * secondCol;

	//Use a more aggressive blend
	float blend = 0.5;
	vec4 outCol = texCol + (vertexColor * blend);
	
	//Wavy stuff
	fragColor = outCol * (sin(Time/200 + pos.x*5)+1)/2;
}