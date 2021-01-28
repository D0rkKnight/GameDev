#version 330

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;
uniform float Time;

void main() {
	fragColor = texture( ourTexture, TexCord + 0.005*vec2( sin(Time/1000+1024.0*TexCord.x),cos(Time/1000+768.0*TexCord.y)) );


}
