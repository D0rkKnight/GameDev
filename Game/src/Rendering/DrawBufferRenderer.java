package Rendering;

import org.joml.Vector2f;

public class DrawBufferRenderer extends WavyRenderer{

	public DrawBufferRenderer(Shader shader) {
		super(shader);
		// TODO Auto-generated constructor stub
	}
	
	
	//This class exists only to flip the uvs.
	protected float[] genUVs(Vector2f[] uvs) {
		float[] out = super.genUVs(uvs);
		
		for (int i=0; i<out.length/2; i++) {
			out[i*2+1] = 1-out[i*2+1];
		}
		
		return out;
	}
}
