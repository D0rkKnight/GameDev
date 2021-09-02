#version 330

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;

void main() {
	vec4 col = vec4(0, 0, 0, 0);
  vec4 border = vec4(1, 0, 0, 1);
  vec2 weight = vec2(2.0/1920, 2.0/1080);

  if (texture(ourTexture, TexCord).a == 0.0) {
    if (texture(ourTexture, TexCord + vec2(0.0,          weight.y)).a  != 0.0 ||
    texture(ourTexture, TexCord + vec2(0.0,         -weight.y)).a  != 0.0 ||
    texture(ourTexture, TexCord + vec2(weight.x,  0.0)).a          != 0.0 ||
    texture(ourTexture, TexCord + vec2(-weight.x, 0.0)).a          != 0.0 ||
    texture(ourTexture, TexCord + vec2(-weight.x, weight.y)).a  != 0.0 ||
    texture(ourTexture, TexCord + vec2(-weight.x, -weight.y)).a != 0.0 ||
    texture(ourTexture, TexCord + vec2(weight.x,  weight.y)).a  != 0.0 ||
    texture(ourTexture, TexCord + vec2(weight.x,  -weight.y)).a != 0.0)
    col = border;
  }

	fragColor = col;
}
