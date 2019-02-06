package math;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memGetFloat;
import static org.lwjgl.system.MemoryUtil.memPutFloat;
import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;
import static org.lwjgl.system.MemoryUtil.nmemCallocChecked;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType("struct vector4f")
public class Vector4f extends Struct implements NativeResource {
    public Vector4f() {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(0, 0, 0, 0);
    }
    
    public Vector4f(Vector4f r) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(r);
    }
    
    public Vector4f(float x, float y, float z, float w) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(x, y, z, w);
    }
	
	
	public Vector4f duplicate() {return new Vector4f(this);}
	
	//Stupid lwjgl generated boilerplate code\\
	
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X, Y, Z, W;

    static {
        Layout layout = __struct(__member(4), __member(4), __member(4), __member(4));

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        Z = layout.offsetof(2);
        W = layout.offsetof(3);
    }

    Vector4f(long address, @Nullable ByteBuffer container) {super(address, container);}
    public Vector4f(ByteBuffer container) {
        this(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {return SIZEOF;}

    public float x() {return nx(address());}
    public float y() {return ny(address());}
    public float z() {return nz(address());}
    public float w() {return nw(address());}

    public Vector4f x(float value) {nx(address(), value); return this;}
    public Vector4f y(float value) {ny(address(), value); return this;}
    public Vector4f z(float value) {nz(address(), value); return this;}
    public Vector4f w(float value) {nw(address(), value); return this;}
    
    public Vector4f set(float x, float y, float z) {x(x); y(y); z(z); return this;}
    public Vector4f set(float x, float y, float z, float w) {x(x); y(y); z(z); w(w); return this;}
    public Vector4f set(Vector4f src) {memCopy(src.address(), address(), SIZEOF);return this;}
    //Standard Allocation
    public static Vector4f malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Vector4f calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Vector4f create() {return new Vector4f(BufferUtils.createByteBuffer(SIZEOF));}
    public static Vector4f create(long address) {return new Vector4f(address, null);}
    public static Vector4f createSafe(long address) {return address == NULL ? null : create(address);}
    //Buffer Allocation
    public static Vector4f.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Vector4f.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Vector4f.Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Vector4f.Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Vector4f.@Nullable Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    //Stack Allocation
    public static Vector4f mallocStack() {return mallocStack(stackGet());}
    public static Vector4f callocStack() {return callocStack(stackGet());}
    public static Vector4f mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Vector4f callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    //Stack Buffer Allocation
    public static Vector4f.Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Vector4f.Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Vector4f.Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Vector4f.Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}

    public static float nx(long struct) {return memGetFloat(struct + X);}
    public static float ny(long struct) {return memGetFloat(struct + Y);}
    public static float nz(long struct) {return memGetFloat(struct + Z);}
    public static float nw(long struct) {return memGetFloat(struct + W);}

    public static void nx(long struct, float value) {memPutFloat(struct + X, value);}
    public static void ny(long struct, float value) {memPutFloat(struct + Y, value);}
    public static void nz(long struct, float value) {memPutFloat(struct + Z, value);}
    public static void nw(long struct, float value) {memPutFloat(struct + W, value);}
    
    public static class Buffer extends StructBuffer<Vector4f, Buffer> implements NativeResource {
    	private static final Vector4f ELEMENT_FACTORY = Vector4f.create(-1L);
    	
        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}

        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}

        public float x() {return nx(address());}
        public float y() {return ny(address());}
        public float z() {return nz(address());}
        public float w() {return nw(address());}

        public Buffer x(float value) {nx(address(), value); return this;}
        public Buffer y(float value) {ny(address(), value); return this;}
        public Buffer z(float value) {nz(address(), value); return this;}
        public Buffer w(float value) {nw(address(), value); return this;}
		@Override
		protected Vector4f getElementFactory() {return ELEMENT_FACTORY;}

    }
}
