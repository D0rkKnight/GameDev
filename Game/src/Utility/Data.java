package Utility;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Data {

	public static FloatBuffer genGLBuff(float[] arr) {
		FloatBuffer buff = BufferUtils.createFloatBuffer(arr.length);
		buff.put(arr);
		buff.flip();

		return buff;
	}

}
