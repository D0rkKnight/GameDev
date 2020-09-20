#version 330

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;
uniform float Time;

const float PI = 3.1415;

float rand(vec2 c){
	return fract(sin(dot(c.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float noise(vec2 p, float freq ){
	float unit = 1280/freq;
	vec2 ij = floor(p/unit);
	vec2 xy = mod(p,unit)/unit;
	//xy = 3.*xy*xy-2.*xy*xy*xy;
	xy = .5*(1.-cos(PI*xy));
	float a = rand((ij+vec2(0.,0.)));
	float b = rand((ij+vec2(1.,0.)));
	float c = rand((ij+vec2(0.,1.)));
	float d = rand((ij+vec2(1.,1.)));
	float x1 = mix(a, b, xy.x);
	float x2 = mix(c, d, xy.x);
	return mix(x1, x2, xy.y);
}

float pNoise(vec2 p, int res){
	float persistance = .5;
	float n = 0.;
	float normK = 0.;
	float f = 4.;
	float amp = 1.;
	int iCount = 0;
	for (int i = 0; i<50; i++){
		n+=amp*noise(p, f);
		f*=2.;
		normK+=amp;
		amp*=persistance;
		if (iCount == res) break;
		iCount++;
	}
	float nf = n/normK;
	return nf*nf*nf*nf;
}

void main() {
	vec4 texCol = texture(ourTexture, TexCord);
	float n = pNoise(vec2(gl_FragCoord) + vec2(0, Time/10), 5);
	
	vec3 col;
	
	if (n > 0.2) col = vec3(0.4, 0.25, 0.4);
	else if (n > 0.05) col = vec3(0.2, 0.1, 0.2);
	else col = vec3(0.1, 0.05, 0.1);

	fragColor = vec4(col.x, col.y, col.z, texCol.a);
}



