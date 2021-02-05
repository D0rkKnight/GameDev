#version 330

//Source: https://www.shadertoy.com/view/4s23zz

layout(location = 0) out vec4 fragColor;

in vec2 TexCord;
in vec4 vertexColor;

uniform sampler2D ourTexture;
uniform float Time;
uniform vec2 viewport;

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

// q: input location, o: sin pattern used to produce n, n: second warp layer position delta
float func( vec2 q, out vec2 o, out vec2 n )
{
		float slowTime = Time / 500;

    // These two effects seem to cancel each other out a bit -----------------------------------------------------
    // Ripple effect
    //q += 0.05*sin(vec2(0.11,0.13)*slowTime + length( q )*4.0);

    // Looping scale effect
    //q *= 0.7 + 0.2*cos(0.05*slowTime);
    //-------------------------------------------------------------------------------------------------------------

    // Get new point to sample for the second layer of warp
    o = 0.5 + 0.5*fbm4_2( q );

    // Add sin wave to that point. This combined with the first two lines are where time is applied.
    o += 0.02*sin(vec2(0.11,0.13)*slowTime*length( o ));

    // Get delta to add to the first warp layer
    n = fbm6_2( 4.0*o );

    // Apply delta
    vec2 p = q + 2.0*n + 1.0;

    // Resample at new point, first warp layer is scrunched in there.
    float f = 0.5 + 0.5*fbm4( 2.0*p );

    // Some voodoo recoloring magic
    f = mix( f, f*f*f*3.5, f*abs(n.x) );

    // Seemingly random scaling, raised to 8th power to make very steep and short curve
    // Makes the dark parts darker
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
  vec3 tot = vec3(0.0);

  //Ripped straight from https://www.iquilezles.org/www/articles/warp/warp.htm
  vec2 q = (2.0*gl_FragCoord.xy-viewport.xy)/viewport.y;
  vec2 o, n;
  float f = func(q, o, n);

  vec3 col = vec3(0.2,0.1,0.4);
  col = mix( col, vec3(0.3,0.05,0.05), f );
  col = mix( col, vec3(0.9,0.9,0.9), dot(n,n) );
  col = mix( col, vec3(0.5,0.2,0.2), 0.5*o.y*o.y );
  col = mix( col, vec3(0.0,0.2,0.4), 0.5*smoothstep(1.2,1.3,abs(n.y)+abs(n.x)) );
  col *= f*2.0;

  vec2 ex = vec2( 1.0 / viewport.x, 0.0 );
  vec2 ey = vec2( 0.0, 1.0 / viewport.y );
  vec3 nor = normalize( vec3( funcs(q+ex) - f, ex.x, funcs(q+ey) - f ) );

  vec3 lig = normalize( vec3( 0.9, -0.2, -0.4 ) );
  float dif = clamp( 0.3+0.7*dot( nor, lig ), 0.0, 1.0 );

  vec3 bdrf;
  bdrf  = vec3(0.85,0.90,0.95)*(nor.y*0.5+0.5);
  bdrf += vec3(0.15,0.10,0.05)*dif;
  bdrf  = vec3(0.85,0.90,0.95)*(nor.y*0.5+0.5);
  bdrf += vec3(0.15,0.10,0.05)*dif;

  col *= bdrf;
  col = vec3(1.0)-col;
  col = col*col;
  col *= vec3(1.2,1.25,1.2);

  tot += col;

  vec2 p = gl_FragCoord.xy / viewport.xy;
	tot *= 0.5 + 0.5 * sqrt(16.0*p.x*p.y*(1.0-p.x)*(1.0-p.y));

	fragColor = vec4( tot, 1.0 );


}
