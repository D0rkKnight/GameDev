#version 330

out vec4 fragColor;

in vec2 texCord;
in vec4 vertexColor;
in vec4 pos;

uniform sampler2D tex;
uniform float Time;
uniform vec2 center; //Ranges between -1 and 1

// Range: -1 to 1
float hash(vec2 p) {
    p  = 50.0*fract( p*0.3183099 + vec2(0.71,0.113));
    return -1.0+2.0*fract( p.x*p.y*(p.x+p.y) );
}

float perlin(vec2 p) {
    vec2 i = floor(p);
    vec2 r = fract(p);

    vec2 u = r*r*(3.0-2.0*r); //Cubic interpolation

    return mix(mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
               u.y);
}

// Range is between -1 and 1
float fbm(vec2 uv) {

    float a = 0.5;
    float r = 2.0;
    float o = 0.0;
    mat2 m = mat2(0.8, 0.6, -0.6, 0.8);

    for (int i=0; i<5; i++) {
        o += perlin(uv) * a;

        uv *= m * r;
        a *= 0.5;
    }

    return o;
}

float warp(vec2 uv) {
    vec2 a = vec2(fbm(uv), fbm(uv + vec2(5)));
    vec2 b = vec2(fbm(uv + a*4.0 + vec2(3, 4)),
                  fbm(uv + a*4.0 + vec2(-1, -2)));

    return fbm(uv + 4.0 * b);
}

float conic (vec2 uv, vec2 c) {
    vec2 sc = vec2(1280, 720) * c;
    uv = uv * vec2(1280, 720);

    return -length(uv-sc)/200.0+1.0;
}

void main() {

	// Normalized pixel coordinates (from 0 to 1)
  vec2 cent = (center+1.0)/2.0; // UV coordinate of the center of the bleed effect
  vec2 uv = (pos.xy+1.0)/2.0;
	vec2 wuv = uv - cent; // World coordinate normalized with UV ratios


	// Draw wobble
	float fps = 10.0;
	float stepT = floor(Time * fps) / fps;
	float wob = fbm((wuv + vec2(7.0, 2.0)) * 10.0 + hash(vec2(stepT)) * 100.0) * 0.005;
	//uv += wob;

	// Warp
	float w = warp(wuv);

	// Conic gradient
	float g = conic(uv, cent);

	// Inverse gradient
	float gi = -conic(uv, cent) + 4.0;

	float sec = Time / 1000.0;
  float dt = sec*1;
	float gt = min(g+dt, gi-dt);


	// Reading noise
	float n = fbm(wuv * 10.0);
	g = g * (n*0.25 + 0.75);

	// Color ramp


	// Color ramp
	float s = w + gt - 1.0;
	vec3 c = texture(tex, texCord).xyz;
	if (s > 0.5) {
			float t = length(texture(tex, texCord))/3.0;
			if (t < 0.4) c = vec3(0);
			else /*if (t < 0.6)*/ c = vec3(0.9, 0.05, 0.1);
	}


	fragColor = vec4(c, 1.0);
}
