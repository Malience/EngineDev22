package math;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.nmemCallocChecked;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;

public class Matrix4fStack extends Matrix4f.Buffer {
	public static final int SIZEOF = Matrix4f.SIZEOF;
	public static final int ALIGNOF = Matrix4f.ALIGNOF;
	
	public Matrix4fStack(ByteBuffer container) {super(container);}
    public Matrix4fStack(long address, int cap) {super(address, null, -1, 0, cap, cap);}
    Matrix4fStack(long address, ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}
    
    //Buffer Allocation
    public static Matrix4fStack calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Matrix4fStack create(long address, int capacity) {return new Matrix4fStack(address, capacity);}
    public static Matrix4fStack createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    //Stack Buffer Allocation
    public static Matrix4fStack mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Matrix4fStack callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Matrix4fStack mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Matrix4fStack callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}
    
    public Matrix4f push() {
    	if(this.position == this.limit) return null;
    	this.position++;
    	Matrix4f out = this.get(position);
    	out.set(this.get(position - 1));
    	return out;
    }
    public Matrix4f pop() {
    	if(this.position == 0) return null;
    	this.position--;
    	return this.get(position);
    }
    
}
