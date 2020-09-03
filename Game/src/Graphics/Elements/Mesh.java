package Graphics.Elements;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import Graphics.Rendering.Renderer;
import Graphics.Rendering.Renderer.Attribute;

/**
 * Simply holds data for renderers.
 * @author Hanzen Shou
 *
 */
public class Mesh {
	
	public float[] data;
	private FloatBuffer buff;
	
	public Mesh(int size) {
		data = new float[size];
		buff = BufferUtils.createFloatBuffer(data.length);
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
	
	public FloatBuffer toBuffer() {
		buff.clear();
		buff.put(data);
		buff.flip();
		return buff;
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
