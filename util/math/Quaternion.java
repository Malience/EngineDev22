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

@NativeType("struct quaternion")
public class Quaternion extends Struct implements NativeResource {

	//~~~~~~~~~~~CONSTRUCTOR~~~~~~~~~~~\\
	public Quaternion() {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(0, 0, 0, 1);
    }
    
    public Quaternion(Vector3f r) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(r);
    	this.w(1);
    }
    
    public Quaternion(float x, float y, float z) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(x, y, z, 1);
    }
	
	public Quaternion axisAngle(Vector3f r, float a){return axisAngle(r.x(), r.y(), r.z(), a);}
	public Quaternion axisAngle(float rx, float ry, float rz, float a){
		a *= 0.5f;
		float sinHalfAngle = (float)Math.sin(a);
		return set(rx * sinHalfAngle, ry * sinHalfAngle, rz * sinHalfAngle, (float)Math.cos(a));
	}
	
	//~~~~~~~~~~~MULTIPLICATION~~~~~~~~~~~\\
	
	public Quaternion mul(Quaternion q) {return mul(q, this);}
	public Quaternion mul(Quaternion q, Quaternion out) {
		float x = x(), y = y(), z = z(), w = w(), qx = q.x(), qy = q.y(), qz = q.z(), qw = q.w();
		float nx = w * qx + x * qw + y * qz - z * qy;
		float ny = w * qy - x * qz + y * qw + z * qx;
		float nz = w * qz + x * qy - y * qx + z * qw;
		float nw = w * qw - x * qx - y * qy - z * qz;
		return out.set(nx, ny, nz, nw);
	}
	
	//~~~~~~~~~~~MULTIPLICATION~~~~~~~~~~~\\
	
	public Vector3f transform(Vector3f r) {return transform(r.x(), r.y(), r.z(), r);}
	public Vector3f transform(Vector3f r, Vector3f out) {return transform(r.x(), r.y(), r.z(), out);}
	public Vector3f transform(float rx, float ry, float rz, Vector3f out) {
		float x = x(), y = y(), z = z(), w = w();
		//Precalculate, saves 9-27 multiplications
		float xx = x * x, yy = y * y, zz = z * z;
		float xy = x * y, xz = x * z, xw = x * w;
		float yz = y * z, yw = y * w, zw = z * w;
		//Save 9 additions
		xx += xx; yy += yy; zz += zz;
		xy += xy; xz += xz; xw += xw;
		yz += yz; yw += yw; zw += zw;
		//Convert to matrix
		float m00 = 1 - yy - zz;	float m01 = xy - zw;		float m02 = xz + yw;
		float m10 = xy + zw;		float m11 = 1 - xx - zz;	float m12 = yz - xw;
		float m20 = xz - yw;		float m21 = yz + xw;		float m22 = 1 - xx - yy;
		//Apply to Vector
        float nx = m00 * x + m10 * y + m20 * z;
        float ny = m01 * x + m11 * y + m21 * z;
        float nz = m02 * x + m12 * y + m22 * z;
        return out.set(nx, ny, nz);
    }
	
	//~~~~~~~~~~~CONJUGATE~~~~~~~~~~~\\
	
	public Quaternion conjugate() {
		float x = x(), y = y(), z = z(), w = w();
		return set(-x, -y, -z, w);
	}
	public Quaternion conjugate(Quaternion out) {
		float x = x(), y = y(), z = z(), w = w();
		return out.set(-x, -y, -z, w);
	}
	
	public float length() {
		float x = x(), y = y(), z = z(), w = w();
		return (float) Math.sqrt(x * x + y * y + z * z + w * w);
	}
	public float lengthSquared() {
		float x = x(), y = y(), z = z(), w = w();
		return (float) x * x + y * y + z * z + w * w;
	}
	
	public Quaternion normalize() {
		float x = x(), y = y(), z = z(), w = w();
		float length = 1.0f / length();
        x *= length; y *= length; z *= length; w *= length;
        return set(x * length, y * length, z * length, w * length);
	}
	
	//~~~~~~~~~~~SLERP~~~~~~~~~~~\\
	//TODO: Redo Slerp
//	public Quaternion slerp(Quaternion b, float t) {return slerp(b, t, this);}
//	public Quaternion slerp(Quaternion b, float t, Quaternion out) {
//		float x = x(), y = y(), z = z(), w = w();
//	}
	
	//Stupid lwjgl generated boilerplate code\\
	
	/** The struct size in bytes. */
    public static final int SIZEOF;
    public static final int ALIGNOF;
    /** The struct member offsets. */
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

    Quaternion(long address, @Nullable ByteBuffer container) {super(address, container);}

    /**
     * Creates a {@link Vector3f} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public Quaternion(ByteBuffer container) {
        this(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() { return SIZEOF; }

    public float x() { return nx(address()); }
    public float y() { return ny(address()); }
    public float z() { return nz(address()); }
    public float w() { return nw(address()); }

    public Quaternion x(float value) { nx(address(), value); return this; }
    public Quaternion y(float value) { ny(address(), value); return this; }
    public Quaternion z(float value) { nz(address(), value); return this; }
    public Quaternion w(float value) { nw(address(), value); return this; }

    public Quaternion set(Vector3f r) {x(r.x()); y(r.y()); z(r.z()); return this;}
    public Quaternion set(float x, float y, float z) {x(x); y(y); z(z); return this;}
    public Quaternion set(float x, float y, float z, float w) {x(x); y(y); z(z); w(w); return this;}
    public Quaternion set(Quaternion src) {memCopy(src.address(), address(), SIZEOF);return this;}
    public static Quaternion malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Quaternion calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Quaternion create() {return new Quaternion(BufferUtils.createByteBuffer(SIZEOF));}
    public static Quaternion create(long address) {return new Quaternion(address, null);}
    @Nullable
    public static Quaternion createSafe(long address) {return address == NULL ? null : create(address);}
    public static Quaternion.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Quaternion.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Quaternion.Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Quaternion.Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Quaternion.@Nullable Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    public static Quaternion mallocStack() {return mallocStack(stackGet());}
    public static Quaternion callocStack() {return callocStack(stackGet());}
    public static Quaternion mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Quaternion callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    public static Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}

    public static float nx(long struct) { return memGetFloat(struct + X); }
    public static float ny(long struct) { return memGetFloat(struct + Y); }
    public static float nz(long struct) { return memGetFloat(struct + Z); }
    public static float nw(long struct) { return memGetFloat(struct + W); }

    public static void nx(long struct, float value) { memPutFloat(struct + X, value); }
    public static void ny(long struct, float value) { memPutFloat(struct + Y, value); }
    public static void nz(long struct, float value) { memPutFloat(struct + Z, value); }
    public static void nw(long struct, float value) { memPutFloat(struct + W, value); }

    public static class Buffer extends StructBuffer<Quaternion, Buffer> implements NativeResource {
        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}

        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}

        public float x() { return nx(address()); }
        public float y() { return ny(address()); }
        public float z() { return nz(address()); }
        public float w() { return nw(address()); }

        public Buffer x(float value) { nx(address(), value); return this; }
        public Buffer y(float value) { ny(address(), value); return this; }
        public Buffer z(float value) { nz(address(), value); return this; }
        public Buffer w(float value) { nw(address(), value); return this; }
		@Override
		protected Quaternion getElementFactory() {
			// TODO Auto-generated method stub
			return null;
		}

    }
}
