package Rendering;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/**
 * Simply holds data for renderers.
 * @author Hanzen Shou
 *
 */
public class Mesh {
	
	public float[] data;
	
	public Mesh(int size) {
		data = new float[size];
	}
	
	public void write(float[] d, Renderer.Attribute attrib) {
		write(d, attrib.groupSize, attrib.stride, attrib.offset);
	}
	
	//write data
	public void write(float[] d, int groupSize, int stride, int offset) {
		for (int i=0; i<d.length; i++) {
			int group = i/groupSize;
			int placeInGroup = i - (group*groupSize);
			int index = (group * stride) + offset + placeInGroup;
			
			data[index] = d[i];
		}
	}
	
	protected FloatBuffer toBuffer() {
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(data.length);
		verticesBuffer.put(data);
		verticesBuffer.flip();
		return verticesBuffer;
	}
	
	public String toString() {
		String str = "";
		str += "[";
		for (int i=0; i<data.length; i++) {
			str += data[i];
			if (i != data.length-1) str += ", ";
		}
		str += "]";
		return str;
	}
}
