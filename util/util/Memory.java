package util;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;


public class Memory {
	public static PointerBuffer stackPointers(MemoryStack stack, ArrayList<String> arraylist) {
		int length = 0;
		for(String s : arraylist) length += memLengthUTF8(s, false);
		ByteBuffer bytebuffer = stack.malloc(length);
		PointerBuffer target = stack.mallocPointer(arraylist.size());
		long pointer = memAddress(bytebuffer);
		int offset = 0;
		for(String s : arraylist) {
			memUTF8(s, false, bytebuffer, offset);
			target.put(pointer + offset);
			offset += memLengthUTF8(s, false);
		}
		PointerBuffer out = stack.mallocPointer(arraylist.size());
		for(String s : arraylist) {
			out.put(stack.ASCII(s));
		}
		return out.flip();
	}
	
	public static ArrayList<String> add(ArrayList<String> target, PointerBuffer buffer) {
		int len = buffer.limit();
		for(int i = 0; i < len; i++) target.add(buffer.getStringUTF8(i));
		return target;
	}
}
