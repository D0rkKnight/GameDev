package Rendering;

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
	
	//write data
	public void write(float[] d, int groupSize, int stride, int offset) {
		for (int i=0; i<d.length; i++) {
			int group = i/groupSize;
			int placeInGroup = i - (group*groupSize);
			int index = (group * stride) + offset + placeInGroup;
			
			data[index] = d[i];
		}
		
		System.out.println("written");
	}
}
