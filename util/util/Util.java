package util;

import java.nio.IntBuffer;

public class Util {
	public static void printIntBuffer(IntBuffer b) {
		int limit = b.limit();
		String out = "";
		for(int i = 0; i < limit; i++) out += b.get(i) + " ";
		System.out.println(out);
	}
}
