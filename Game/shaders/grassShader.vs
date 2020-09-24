#version 330

attribute vec3 vertices;
attribute vec2 texCords;
attribute vec4 color;

uniform mat4 MVP;
uniform float Time;
uniform vec2 WPos;

out vec2 TexCord;
out vec4 vertexColor;

float random (vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))*
        43758.5453123);
}

void main() {
	vec3 pos = vertices;
	vec2 tC = texCords;
	
	float r = random(WPos);
	float rx = random(WPos + vec2(10, 10));
	float ry = random(WPos + vec2(-10, -10));
	
	pos.x += sin(Time/1000 + (r * 1000)) * pos.y /5;
	
	pos.x *= 0.9 + (rx * 0.2);
	pos.y *= 0.9 + (ry * 0.2);

	gl_Position = MVP * vec4(pos, 1.0);
	TexCord = tC;
	vertexColor = color;
}