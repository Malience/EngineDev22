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

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType("struct vector3f")
public class Vector3f extends Struct implements NativeResource {
    public Vector3f() {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(0, 0, 0);
    }
    
    public Vector3f(Vector3f r) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(r);
    }
    
    public Vector3f(float x, float y, float z) {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.set(x, y, z);
    }
	
	
	public Vector3f duplicate() {return new Vector3f(this);}
	
	//MATH
	//Basic Vector Functions
	public Vector3f normal() {return this.duplicate().normalize();}
	public Vector3f normalize() {
		float x = x(), y = y(), z = z(); 
		float length = 1.0f/length(); 
		return set(x * length, y * length, z * length);
	}
	public Vector3f normalize(Vector3f out) {
		float x = x(), y = y(), z = z(); 
		float length = 1.0f/length(); 
		return out.set(x * length, y * length, z * length);
	}
	
	public float distance(Vector3f r) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z(); 
		float xrx = x - rx, yry = y - ry, zrz = z - rz;
		return (float) Math.sqrt(xrx * xrx + yry * yry + zrz * zrz);
	}
	//Use this over distance if you can
	public float distanceSquared(Vector3f r) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z(); 
		float xrx = x - rx, yry = y - ry, zrz = z - rz;
		return xrx * xrx + yry * yry + zrz * zrz;
	}
	
	public float length() { //Also known as the magnitude
		float x = x(), y = y(), z = z();
		return (float)Math.sqrt(x * x + y * y + z * z);
	}
	public float lengthSquared() {
		float x = x(), y = y(), z = z();
		return x * x + y * y + z * z;
	}
	public float magnitude() { //Also known as the length
		float x = x(), y = y(), z = z();
		return (float)Math.sqrt(x * x + y * y + z * z);
	}
	public float magnitudeSquared() {
		float x = x(), y = y(), z = z();
		return x * x + y * y + z * z;
	}
	
	public Vector3f cross(Vector3f r) {return this.cross(r, this);}
	public Vector3f cross(Vector3f r, Vector3f out) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return out.set(y * rz - z * ry, z * rx - x * rz, x * ry - y * rx);
	}
	//TODO: Refactor for modern coding conventions
	public Vector3f projection(Vector3f onto) {
		return onto.mul(onto.dot(this)/onto.length());
	}

	//Basic Arithmetic Operations (Addition, Subtraction, Multiplication, Division)
	public Vector3f add(float r) {
		float x = x(), y = y(), z = z();
		return set(x + r, y + r, z + r);
	}
	public Vector3f add(float r, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x + r, y + r, z + r);
	}
	public Vector3f add(float rx, float ry, float rz) {
		float x = x(), y = y(), z = z();
		return set(x + rx, y + ry, z + rz);
	}
	public Vector3f add(float rx, float ry, float rz, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x + rx, y + ry, z + rz);
	}
	public Vector3f add(Vector3f r) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return set(x + rx, y + ry, z + rz);
	}
	public Vector3f add(Vector3f r, Vector3f out) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return out.set(x + rx, y + ry, z + rz);
	}
	
	public Vector3f sub(float r) {
		float x = x(), y = y(), z = z();
		return set(x - r, y - r, z - r);
	}
	public Vector3f sub(float r, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x - r, y - r, z - r);
	}
	public Vector3f sub(float rx, float ry, float rz) {
		float x = x(), y = y(), z = z();
		return set(x - rx, y - ry, z - rz);
	}
	public Vector3f sub(float rx, float ry, float rz, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x - rx, y - ry, z - rz);
	}
	public Vector3f sub(Vector3f r) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return set(x - rx, y - ry, z - rz);
	}
	public Vector3f sub(Vector3f r, Vector3f out) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return out.set(x - rx, y - ry, z - rz);
	}
	
	//~~~~~~~~~~~Something Else~~~~~~~~~~~\\
	
	public Vector3f mul(float r) {
		float x = x(), y = y(), z = z();
		return set(x * r, y * r, z * r);
	}
	public Vector3f mul(float r, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x * r, y * r, z * r);
	}
	public Vector3f mul(Vector3f r) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return set(x * rx, y * ry, z * rz);
	}
	public Vector3f mul(Vector3f r, Vector3f out) {
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return out.set(x * rx, y * ry, z * rz);
	}
	public Vector3f mul(float rx, float ry, float rz) {
		float x = x(), y = y(), z = z();
		return set(x * rx, y * ry, z * rz);
	}
	public Vector3f mul(float rx, float ry, float rz, Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(x * rx, y * ry, z * rz);
	}
	
	//Basic Algebra Operations
	public Vector3f abs() {
		float x = x(), y = y(), z = z();
		return set(Math.abs(x), Math.abs(y), Math.abs(z));
	}
	public Vector3f abs(Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(Math.abs(x), Math.abs(y), Math.abs(z));
	}
	public Vector3f floor() {
		float x = x(), y = y(), z = z();
		return set((float) Math.floor(x), (float) Math.floor(y), (float) Math.floor(z));
	}
	public Vector3f floor(Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set((float) Math.floor(x), (float) Math.floor(y), (float) Math.floor(z));
	}
	public Vector3f ceil() {
		float x = x(), y = y(), z = z();
		return set((float) Math.ceil(x), (float) Math.ceil(y), (float) Math.ceil(z));
	}
	
	//Rotations
	
	public Vector3f rotate(float yaw, float pitch){
		yaw = (float)Math.toRadians(yaw); pitch = (float)Math.toRadians(pitch);;
		return set((float)(Math.cos(yaw) * Math.cos(pitch)), (float)(Math.sin(pitch)), (-(float)(Math.sin(yaw) * Math.cos(pitch))));
	}
	
	//Basic Game Operations
	public Vector3f inverse() {return duplicate().invert();}
	public Vector3f invert() {
		float x = x(), y = y(), z = z();
		return set(1.0f / x, 1.0f / y, 1.0f / z);
	}
	public Vector3f invert(Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(1.0f / x, 1.0f / y, 1.0f / z);
	}
	public Vector3f zero(){return set(0, 0, 0);}
	public Vector3f lerp(Vector3f r, float lerp){return this.lerp(r, lerp, this);}
	public Vector3f lerp(Vector3f r, float lerp, Vector3f out){
		float x = x(), y = y(), z = z(), rx = r.x(), ry = r.y(), rz = r.z();
		return out.set(x + lerp * (rx - x), y + lerp * (ry - y), z + lerp * (rz - z));
	}
	
	
	////~~~~~~~~~~~~~~~~~~YOU DON'T NEED TO MESS WITH ANYTHING BELOW THIS LINE!!!!!~~~~~~~~~~~~~~~~~~\\\\\\\\\\\\\\\
	public Vector3f negative() {return duplicate().negate();}
	public Vector3f negate() {
		float x = x(), y = y(), z = z();
		return set(-x, -y, -z);
	}
	public Vector3f negate(Vector3f out) {
		float x = x(), y = y(), z = z();
		return out.set(-x, -y, -z);
	}
	
	//~~~~~~~~~~~DOT PRODUCT~~~~~~~~~~\\
	public float dot() {return ndot(address());}
	public float dot(Vector3f r) {return ndot(address(), r.address());}
	public float dot(float rx, float ry, float rz) {
		float x = x(), y = y(), z = z();
		return x * rx + y * ry + z * rz;
	}
	public float dot(float rx, float ry, float rz, float rw) {
		float x = x(), y = y(), z = z();
		return x * rx + y * ry + z * rz + rw;
	}
	
	public static float ndot(long v) {float x = nx(v), y = ny(v), z = nz(v); return x * x + y * y + z * z;}
	public static float ndot(long v1, long v2) {return nx(v1) * nx(v2) + ny(v1) * ny(v2) + nz(v1)  * nz(v2);}
	
	//Stupid lwjgl generated boilerplate code\\
	
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X, Y, Z;

    static {
        Layout layout = __struct(__member(4), __member(4), __member(4));

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        Z = layout.offsetof(2);
    }

    Vector3f(long address, ByteBuffer container) {super(address, container);}
    public Vector3f(ByteBuffer container) {
        this(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {return SIZEOF;}

    public float x() {return nx(address());}
    public float y() {return ny(address());}
    public float z() {return nz(address());}

    public Vector3f x(float value) {nx(address(), value); return this;}
    public Vector3f y(float value) {ny(address(), value); return this;}
    public Vector3f z(float value) {nz(address(), value); return this;}

    public Vector3f set(float x, float y, float z) {x(x); y(y); z(z); return this;}
    public Vector3f set(Vector3f src) {memCopy(src.address(), address(), SIZEOF);return this;}
    //Standard Allocation
    public static Vector3f malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Vector3f calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Vector3f create() {return new Vector3f(BufferUtils.createByteBuffer(SIZEOF));}
    public static Vector3f create(long address) {return new Vector3f(address, null);}
    public static Vector3f createSafe(long address) {return address == NULL ? null : create(address);}
    //Buffer Allocation
    public static Vector3f.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Vector3f.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Vector3f.Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Vector3f.Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Vector3f.Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    //Stack Allocation
    public static Vector3f mallocStack() {return mallocStack(stackGet());}
    public static Vector3f callocStack() {return callocStack(stackGet());}
    public static Vector3f mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Vector3f callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    //Stack Buffer Allocation
    public static Vector3f.Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Vector3f.Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Vector3f.Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Vector3f.Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}

    public static float nx(long struct) {return memGetFloat(struct + Vector3f.X);}
    public static float ny(long struct) {return memGetFloat(struct + Vector3f.Y);}
    public static float nz(long struct) {return memGetFloat(struct + Vector3f.Z);}

    public static void nx(long struct, float value) {memPutFloat(struct + Vector3f.X, value);}
    public static void ny(long struct, float value) {memPutFloat(struct + Vector3f.Y, value);}
    public static void nz(long struct, float value) {memPutFloat(struct + Vector3f.Z, value);}

    public static class Buffer extends StructBuffer<Vector3f, Buffer> implements NativeResource {
    	private static final Vector3f ELEMENT_FACTORY = Vector3f.create(-1L);
    	
        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}

        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}

        public float x() {return Vector3f.nx(address());}
        public float y() {return Vector3f.ny(address());}
        public float z() {return Vector3f.nz(address());}

        public Vector3f.Buffer x(float value) {Vector3f.nx(address(), value); return this;}
        public Vector3f.Buffer y(float value) {Vector3f.ny(address(), value); return this;}
        public Vector3f.Buffer z(float value) {Vector3f.nz(address(), value); return this;}
		@Override
		protected Vector3f getElementFactory() {return ELEMENT_FACTORY;}

    }
}
