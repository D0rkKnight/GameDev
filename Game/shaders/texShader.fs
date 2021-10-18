#version 330

layout(location = 0) out vec4 fragColor;

in vec2 texCord;
in vec4 vertexColor;

uniform sampler2D tex;

void main() {
	vec4 texCol = texture(tex, texCord);

//	float blend = 0.5;
//	vec4 secondCol = vec4(vertexColor, 1.0) * blend + (vec4(1, 1, 1, 1) * (1-blend));
//
// 	fragColor = texCol * secondCol;

	//Use a more aggressive blend
    float blend = 0.5;
	fragColor = texCol + (vertexColor * blend);
	fragColor.a = texCol.a;
}
