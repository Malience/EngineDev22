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

@NativeType("struct vector2f")
public class Vector2f extends Struct implements NativeResource {
	public Vector2f() {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(0, 0);
    }
    
    public Vector2f(Vector2f r) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(r);
    }
    
    public Vector2f(float x, float y) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(x, y);
    }

	//Basic Vector Functions
//	public default Vector2f normal() {float length = length(); return xy(x() / length, y() / length);}
//	public default float distance(Vector2f r) {
//		float xx = x() - r.x(), yy = y() - r.y();
//		return (float) Math.sqrt(xx * xx + yy * yy);
//	}
//	public default float distanceSquared(Vector2f r) {
//		float xx = x() - r.x(), yy = y() - r.y();
//		return xx * xx + yy * yy;
//	}
//	public default float length(){float x = x(), y = y(); return (float)Math.sqrt(x * x + y * y);}
//	public default float lengthSquared(){float x = x(), y = y(); return x * x + y * y;}
//	public default float dot(Vector2f r){return x() * r.x() + y() * r.y();}
//	public default float cross(Vector2f r){return x() * r.y() - y() * r.x();}
	
	//Basic Arithmetic Operations (Addition, Subtraction, Multiplication, Division)
	//TODO
	
	//Rotations
//	public default Vector2f rotate(float angle) {
//		float x = x(), y = y();
//		float rad = angle * Constants.RAD;
//		float cos = (float) Math.cos(rad);
//		float sin = (float) Math.sin(rad);
//		return xy(x * cos - y * sin, x * sin + y * cos);
//	}
	
////~~~~~~~~~~~~~~~~~~YOU DON'T NEED TO MESS WITH ANYTHING BELOW THIS LINE!!!!!~~~~~~~~~~~~~~~~~~\\\\\\\\\\\\\\\
	public Vector2f negate() {
		float x = x(), y = y();
		return set(-x, -y);
	}
	public Vector2f negate(Vector2f out) {
		float x = x(), y = y();
		return out.set(-x, -y);
	}
	
	//~~~~~~~~~~~DOT PRODUCT~~~~~~~~~~\\
	public float dot() {return ndot(address());}
	public float dot(Vector2f r) {return ndot(address(), r.address());}
	public float dot(float rx, float ry) {
		float x = x(), y = y();
		return x * rx + y * ry;
	}
	//High efficiency pure memory dot product!!!!
	public static float ndot(long v) {float x = nx(v), y = ny(v); return x * x + y * y;}
	public static float ndot(long v1, long v2) {return nx(v1) * nx(v2) + ny(v1) * ny(v2);}
	
	//Stupid lwjgl generated boilerplate code\\
	
	/** The struct size in bytes. */
    public static final int SIZEOF;
    public static final int ALIGNOF;
    /** The struct member offsets. */
    public static final int X, Y;

    static {
        Layout layout = __struct(__member(4), __member(4));

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        X = layout.offsetof(0);
        Y = layout.offsetof(1);
    }

    Vector2f(long address, @Nullable ByteBuffer container) {super(address, container);}

    public Vector2f(ByteBuffer container) {
        this(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() { return SIZEOF; }

    public float x() { return nx(address()); }
    public float y() { return ny(address()); }

    public Vector2f x(float value) { nx(address(), value); return this; }
    public Vector2f y(float value) { ny(address(), value); return this; }

    public Vector2f set(float x, float y) {x(x); y(y); return this;}
    public Vector2f set(Vector2f src) {memCopy(src.address(), address(), SIZEOF);return this;}
    public static Vector2f malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Vector2f calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Vector2f create() {return new Vector2f(BufferUtils.createByteBuffer(SIZEOF));}
    public static Vector2f create(long address) {return new Vector2f(address, null);}
    @Nullable
    public static Vector2f createSafe(long address) {return address == NULL ? null : create(address);}
    public static Vector2f.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Vector2f.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Vector2f.Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Vector2f.Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Vector2f.@Nullable Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    public static Vector2f mallocStack() {return mallocStack(stackGet());}
    public static Vector2f callocStack() {return callocStack(stackGet());}
    public static Vector2f mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Vector2f callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    public static Vector2f.Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Vector2f.Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Vector2f.Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Vector2f.Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}

    public static float nx(long struct) { return memGetFloat(struct + Vector2f.X); }
    public static float ny(long struct) { return memGetFloat(struct + Vector2f.Y); }

    public static void nx(long struct, float value) { memPutFloat(struct + Vector2f.X, value); }
    public static void ny(long struct, float value) { memPutFloat(struct + Vector2f.Y, value); }

    public static class Buffer extends StructBuffer<Vector2f, Buffer> implements NativeResource {
    	private static final Vector2f ELEMENT_FACTORY = Vector2f.create(-1L);
        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}

        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}

        public float x() { return Vector2f.nx(address()); }
        public float y() { return Vector2f.ny(address()); }

        public Vector2f.Buffer x(float value) { Vector2f.nx(address(), value); return this; }
        public Vector2f.Buffer y(float value) { Vector2f.ny(address(), value); return this; }
		@Override
		protected Vector2f getElementFactory() {return ELEMENT_FACTORY;}

    }
}