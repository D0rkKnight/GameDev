#version 330

//Source: https://www.shadertoy.com/view/4s23zz

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;
uniform float Time;

// //Expensive variant
// // int numOctaves = 3;
// // float fbm(vec2 x) {
// // 	float H = 2;
// // 	float t = 0.0;
// //
// // 	for (int i=0; i<numOctaves; i++) {
// // 		float f = pow(2.0, float(i));
// // 		float a = pow(f, -H);
// //
// // 		t += a*twoDimPerlin(f*x);
// // 	}
// //
// // 	return t;
// // }
//
// //Testing out with this different function
// const mat2 mtx = mat2( 0.80,  0.60, -0.60,  0.80 );
// float fbm4( vec2 p )
// {
//     float f = 0.0;
//
//     f += 0.5000*twoDimPerlin( p ); p = mtx*p*2.02;
//     f += 0.2500*twoDimPerlin( p ); p = mtx*p*2.03;
//     f += 0.1250*twoDimPerlin( p ); p = mtx*p*2.01;
//     f += 0.0625*twoDimPerlin( p );
//
//     return f/0.9375;
// }
//
// float domainWarp(vec2 p) {
// 	vec2 q = vec2(fbm4(p), fbm4(p+vec2(3.4, 7.2)));
//
// 	vec2 r = vec2( fbm4( p + 4.0*q + vec2(1.7,9.2) ),
//                    fbm4( p + 4.0*q + vec2(8.3,2.8) ) );
//
// 	return fbm4(p + r*4.0);
// }
//
// void main() {
// 	vec2 uv = vec2(gl_FragCoord.x + (Time/10), gl_FragCoord.y);
//
// 	float mul = domainWarp(uv);
//
// 	vec4 texCol = vec4(mul);
// 	//texture(ourTexture, TexCord + vec2(Time/10));
// 	texCol.w = 1;
//
// 	// float n = pNoise(vec2(gl_FragCoord) + vec2(0, Time/10), 5);
// 	//
// 	// vec3 col;
// 	//
// 	// if (n > 0.2) col = vec3(0.4, 0.25, 0.4);
// 	// else if (n > 0.05) col = vec3(0.2, 0.1, 0.2);
// 	// else col = vec3(0.1, 0.05, 0.1);
//
// 	//fragColor = vec4(col.x, col.y, col.z, texCol.a);
//
// 	fragColor = texCol;
// }

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float lerp(float x1, float x2, float l) {
	return mix(x1, x2, l * l * l * (l * (l * 6.0 - 15.0) + 10.0));
}

float period = 1.0;
float noise(vec2 v)
{
    //Clamp into period
	float lx = mod(v.x, period) / period;
	float ly = mod(v.y, period) / period;
	float flooredx = v.x - mod(v.x, period);
	float flooredy = v.y - mod(v.y, period);

	//Generate two random numbers
	float rul = rand(vec2(flooredx, flooredy));
	float rur = rand(vec2(flooredx+period, flooredy));
	float rbl = rand(vec2(flooredx, flooredy+period));
	float rbr = rand(vec2(flooredx+period, flooredy+period));

	//Retrieve r1 of next segment for testing
	// float nextFloored = f + period - mod(f + period, period);
	// float nextr1 = rand(nextFloored);
	// if (nextFloored != floored+period) return 1;
	// else return 0;

	//lerp
	float ot = lerp(rul, rur, lx);
	float ob = lerp(rbl, rbr, lx);

	return lerp(ot, ob, ly);
}

const mat2 mtx = mat2( 0.80,  0.60, -0.60,  0.80 );

float fbm4( vec2 p )
{
    float f = 0.0;

    f += 0.5000*(-1.0+2.0*noise( p )); p = mtx*p*2.02;
    f += 0.2500*(-1.0+2.0*noise( p )); p = mtx*p*2.03;
    f += 0.1250*(-1.0+2.0*noise( p )); p = mtx*p*2.01;
    f += 0.0625*(-1.0+2.0*noise( p ));

    return f/0.9375;
}

float fbm6( vec2 p )
{
    float f = 0.0;

    f += 0.500000*noise( p ); p = mtx*p*2.02;
    f += 0.250000*noise( p ); p = mtx*p*2.03;
    f += 0.125000*noise( p ); p = mtx*p*2.01;
    f += 0.062500*noise( p ); p = mtx*p*2.04;
    f += 0.031250*noise( p ); p = mtx*p*2.01;
    f += 0.015625*noise( p );

    return f/0.96875;
}

vec2 fbm4_2( vec2 p )
{
    return vec2( fbm4(p+vec2(1.0)), fbm4(p+vec2(6.2)) );
}

vec2 fbm6_2( vec2 p )
{
    return vec2( fbm6(p+vec2(9.2)), fbm6(p+vec2(5.7)) );
}


float func( vec2 q, out vec2 o, out vec2 n )
{
		float slowTime = Time / 500;

    q += 0.05*sin(vec2(0.11,0.13)*slowTime + length( q )*4.0);

    q *= 0.7 + 0.2*cos(0.05*slowTime);

    o = 0.5 + 0.5*fbm4_2( q );

    o += 0.02*sin(vec2(0.11,0.13)*slowTime*length( o ));

    n = fbm6_2( 4.0*o );

    vec2 p = q + 2.0*n + 1.0;

    float f = 0.5 + 0.5*fbm4( 2.0*p );

    f = mix( f, f*f*f*3.5, f*abs(n.x) );

    f *= 1.0-0.5*pow( 0.5+0.5*sin(8.0*p.x)*sin(8.0*p.y), 8.0 );

    return f;
}

float funcs(in vec2 q )
{
    vec2 t1, t2;
    return func(q,t1,t2);
}


void main()
{
	vec2 v = vec2(gl_FragCoord.x, gl_FragCoord.y);
  float f = funcs(v/100.0);

	fragColor = vec4( f, f, f, 1.0 );
}
