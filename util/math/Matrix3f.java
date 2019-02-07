package math;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;
import static org.lwjgl.system.MemoryUtil.nmemCallocChecked;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType("struct matrix3f")
public class Matrix3f extends Struct implements NativeResource {
	//Unused
	public static final int IDENTITY = 0b1;
	public static final int AFFINE = 0b10;
	//public static final int TRANSLATION = 0b100;
	//public static final int PERSPECTIVE = 0b1000;
	//public static final int ORTHOGRAPHIC = 0b10000;
	
	private int prop;
	public Matrix3f() {
	  	this(BufferUtils.createByteBuffer(SIZEOF));
	  	this.identity();
	}
	public float m00() {return nm00(address());} public float m01() {return nm01(address());} public float m02() {return nm02(address());}
	public float m10() {return nm10(address());} public float m11() {return nm11(address());} public float m12() {return nm12(address());}
	public float m20() {return nm20(address());} public float m21() {return nm21(address());} public float m22() {return nm22(address());}
	
	public void m00(float value) {nm00(address(), value);} public void m01(float value) {nm01(address(), value);} 
	public void m02(float value) {nm02(address(), value);}
	public void m10(float value) {nm10(address(), value);} public void m11(float value) {nm11(address(), value);} 
	public void m12(float value) {nm12(address(), value);}
	public void m20(float value) {nm20(address(), value);} public void m21(float value) {nm21(address(), value);} 
	public void m22(float value) {nm22(address(), value);}
	
	public int prop() {return prop;}
	public void prop(int value) {prop = value;}
	
	//~~~~~~~~~~~SETTERS~~~~~~~~~~~\\
	public Matrix3f set(Matrix3f m) {
		return set(
			m.m00(), m.m01(), m.m02(),
			m.m10(), m.m11(), m.m12(),
			m.m20(), m.m21(), m.m22(),
			m.prop());
	}
	public Matrix3f set(float m00, float m01, float m02,
								float m10, float m11, float m12,
								float m20, float m21, float m22) {
		return set(
				m00, m01, m02,
				m10, m11, m12,
				m20, m21, m22,
				0);
	}
	
	public Matrix3f set(float m00, float m01, float m02,
								float m10, float m11, float m12,
								float m20, float m21, float m22,
								int prop) {
		m00(m00); m01(m01); m02(m02);
		m10(m10); m11(m11); m12(m12);
		m20(m20); m21(m21); m22(m22);
		prop(prop);
		return this;
	}
	
	public Matrix3f identity() {
		if((prop() & IDENTITY) != 0) return this;
		return set(
				1, 0, 0,
				0, 1, 0,
				0, 0, 1,
				IDENTITY | AFFINE);// | TRANSLATION);
	}
	
	//~~~~~~~~~~~ROTATION~~~~~~~~~~~\\
	
	public Matrix3f setRotation(Vector3f axis, float angle) {return setRotation(axis.x(), axis.y(), axis.z(), angle);}
	public Matrix3f setRotation(float x, float y, float z, float angle) {
		float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        float cos2 = 1.0f - cos;
        float xyc = x * y * cos2, xzc = x * z * cos2, yzc = y * z * cos2;
        float xs = x * sin, ys = y * sin, zs = z * sin;
        float m00 = cos + x * x * cos2; 	float m01 = xyc - zs; 				float m02 = xzc + ys;
		float m10 = xyc + zs; 				float m11 = cos + y * y * cos2; 	float m12 = yzc - xs;
		float m20 = xzc - ys; 				float m21 = yzc + xs; 				float m22 = cos + z * z * cos2;
		return set(
				m00, m01, m02,
				m10, m11, m12,
				m20, m21, m22,
				AFFINE);
	}
	
	public Matrix3f setRotation(Quaternion q) {
		//9 Multiplications 21 Additions
		//Load from memory
		float x = q.x(), y = q.y(), z = q.z(), w = q.w();
		//Precalculate, saves 9-27 multiplications
		float xx = x * x, yy = y * y, zz = z * z;
		float xy = x * y, xz = x * z, xw = x * w;
		float yz = y * z, yw = y * w, zw = z * w;
		//Save 9 additions
		xx += xx; yy += yy; zz += zz;
		xy += xy; xz += xz; xw += xw;
		yz += yz; yw += yw; zw += zw;
		
		float m00 = 1 - yy - zz;	float m01 = xy - zw;		float m02 = xz + yw;
		float m10 = xy + zw;		float m11 = 1 - xx - zz;	float m12 = yz - xw;
		float m20 = xz - yw;		float m21 = yz + xw;		float m22 = 1 - xx - yy;
		//Save to memory
		return set(
				m00, m01, m02,
				m10, m11, m12,
				m20, m21, m22,
				AFFINE);
	}
	
	//~~~~~~~~~~~TRANSPOSE~~~~~~~~~~~\\
	
	public Matrix3f transpose() {return transpose(this);}
	public Matrix3f transpose(Matrix3f out) {
		return out.set(
				m00(), m10(), m20(),
				m01(), m11(), m21(),
				m02(), m12(), m22(),
				0);
	}
	
	//~~~~~~~~~~~TRANSFORM~~~~~~~~~~~\\
	
	public Vector3f transform(Vector3f v){return transform(v, v);}
	public Vector3f transform(Vector3f v, Vector3f out) {
		float x = v.x(), y = v.y(), z = v.z();
		float nx = m00() * x + m01() * y + m02() * z;
		float ny = m10() * x + m11() * y + m12() * z;
		float nz = m20() * x + m21() * y + m22() * z;
		return out.set(nx, ny, nz);
	}
	
	public Vector3f transformTranspose(Vector3f v){return transformTranspose(v, v);}
	public Vector3f transformTranspose(Vector3f v, Vector3f out) {
		float x = v.x(), y = v.y(), z = v.z();
		float nx = m00() * x + m10() * y + m20() * z;
		float ny = m01() * x + m11() * y + m21() * z;
		float nz = m02() * x + m12() * y + m22() * z;
		return out.set(nx, ny, nz);
	}
	
	//~~~~~~~~~~~MULTIPLICATION~~~~~~~~~~~\\
	
	public Matrix3f mul(float scale) {return mul(scale, this);}
	public Matrix3f mul(float scale, Matrix3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		return out.set(
				m00 * scale, m10 * scale, m20 * scale,
				m01 * scale, m11 * scale, m21 * scale,
				m02 * scale, m12 * scale, m22 * scale,
				0);
	}
	
	public Matrix3f mul(Vector3f r) {return mul(r, this);}
	public Matrix3f mul(Vector3f r, Matrix3f out) {
		float x = r.x(), y = r.y(), z = r.z();
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		return out.set(
				m00 * x, m10 * y, m20 * z,
				m01 * x, m11 * y, m21 * z,
				m02 * x, m12 * y, m22 * z,
				0);
	}
	
	public Matrix3f mul(Matrix3f m) {return mul(m, this);}
	public Matrix3f mul(Matrix3f m, Matrix3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		
		float rm00 = m.m00(), rm01 = m.m01(), rm02 = m.m02();
		float rm10 = m.m10(), rm11 = m.m11(), rm12 = m.m12();
		float rm20 = m.m20(), rm21 = m.m21(), rm22 = m.m22();
		
		float nm00 = m00 * rm00 + m01 * rm10 + m02 * rm20;
		float nm10 = m10 * rm00 + m11 * rm10 + m12 * rm20;
		float nm20 = m20 * rm00 + m21 * rm10 + m22 * rm20;
		float nm01 = m00 * rm01 + m01 * rm11 + m02 * rm21;
		float nm11 = m10 * rm01 + m11 * rm11 + m12 * rm21;
		float nm21 = m20 * rm01 + m21 * rm11 + m22 * rm21;
		float nm02 = m00 * rm02 + m01 * rm12 + m02 * rm22;
		float nm12 = m10 * rm02 + m11 * rm12 + m12 * rm22;
		float nm22 = m20 * rm02 + m21 * rm12 + m22 * rm22;
		
		return out.set(
				nm00, nm10, nm20,
				nm01, nm11, nm21,
				nm02, nm12, nm22,
				0);
	}
	
	public Matrix3f mulTranspose(Matrix3f m) {return mulTranspose(m, this);}
	public Matrix3f mulTranspose(Matrix3f m, Matrix3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		
		float rm00 = m.m00(), rm01 = m.m01(), rm02 = m.m02();
		float rm10 = m.m10(), rm11 = m.m11(), rm12 = m.m12();
		float rm20 = m.m20(), rm21 = m.m21(), rm22 = m.m22();
		
		float nm00 = m00 * rm00 + m01 * rm10 + m02 * rm20;
		float nm10 = m10 * rm00 + m11 * rm10 + m12 * rm20;
		float nm20 = m20 * rm00 + m21 * rm10 + m22 * rm20;
		float nm01 = m00 * rm01 + m01 * rm11 + m02 * rm21;
		float nm11 = m10 * rm01 + m11 * rm11 + m12 * rm21;
		float nm21 = m20 * rm01 + m21 * rm11 + m22 * rm21;
		float nm02 = m00 * rm02 + m01 * rm12 + m02 * rm22;
		float nm12 = m10 * rm02 + m11 * rm12 + m12 * rm22;
		float nm22 = m20 * rm02 + m21 * rm12 + m22 * rm22;
		
		return out.set(
				nm00, nm10, nm20,
				nm01, nm11, nm21,
				nm02, nm12, nm22,
				0);
	}
	
	public Matrix3f add(Matrix3f m) {return add(m, this);}
	public Matrix3f add(Matrix3f m, Matrix3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		
		float rm00 = m.m00(), rm01 = m.m01(), rm02 = m.m02();
		float rm10 = m.m10(), rm11 = m.m11(), rm12 = m.m12();
		float rm20 = m.m20(), rm21 = m.m21(), rm22 = m.m22();
		
		float nm00 = m00 + rm00, nm01 = m01 + rm01, nm02 = m02 + rm02;
		float nm10 = m10 + rm10, nm11 = m11 + rm11, nm12 = m12 + rm12;
		float nm20 = m20 + rm20, nm21 = m21 + rm21, nm22 = m22 + rm22;

		return out.set(
				nm00, nm10, nm20,
				nm01, nm11, nm21,
				nm02, nm12, nm22,
				0);
	}
	
	//~~~~~~~~~~~INVERSE~~~~~~~~~~~\\
	
	public float determinant() {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20) + m02 * (m10 * m21 - m11 * m20); 
	}
	
	//TODO: Optimize
	public Matrix3f invert() {return invert(this);}
	public Matrix3f invert(Matrix3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02();
		float m10 = m10(), m11 = m11(), m12 = m12();
		float m20 = m20(), m21 = m21(), m22 = m22();
		float a = m11 * m22 - m12 * m21, b = m12 * m20 - m10 * m22, c = m10 * m21 - m11 * m20;
		float det = 1.0f / (m00 * a + m01 * b + m02 * c);
		return out.set(
				a * det, (m02 * m21 - m01 * m22) * det, (m01 * m22 - m02 * m21) * det,
				b * det, (m00 * m22 - m02 * m20) * det, (m02 * m10 - m00 * m12) * det,
				c * det, (m01 * m20 - m00 * m21) * det, (m00 * m11 - m01 * m10) * det,
				0);
	}
	
	//~~~~~~~~~~~PHYSICS CALCULATIONS~~~~~~~~~~~\\
	
		public Matrix3f calculateContactBasis(Vector3f normal) {
			float x = normal.x(), y = normal.y(), z = normal.z();
			m00(x); m01(y); m02(z);
			if(Math.abs(x) > Math.abs(y)) {
				float s = 1.0f / (float)Math.sqrt(z * z + x * x);
				float m10 = z * s, m12 = -x * s, m20 = y * m10;
				m10(m10); m11(0); m12(m12);
				m20(m20); m21(z * m10 - x * m12); m22(-m20);
			} else {
				float s = 1.0f / (float)Math.sqrt(z * z + y * y);
				float m11 = -z * s, m12 = x * s;
				m10(0f); m11(m11); m12(m12);
				m20(y * m12 - z * m11); m21(-x * m12); m22(x * m11);
			}
			return transpose();
		}
		
		public Matrix3f mulInverseInertiaTensor(Vector3f iit) {
			float x = iit.x(), y = iit.y(), z = iit.z();
			float m00 = m00(), m01 = m01(), m02 = m02();
			float m10 = m10(), m11 = m11(), m12 = m12();
			float m20 = m20(), m21 = m21(), m22 = m22();
			return set(
					m00 * x, m10 * y, m20 * z,
					m01 * x, m11 * y, m21 * z,
					m02 * x, m12 * y, m22 * z,
					0);
		}
		
		public Matrix3f impulseToTorque(Vector3f iit) {return impulseToTorque(iit, this);}
		public Matrix3f impulseToTorque(Vector3f iit, Matrix3f out) {//TODO: IF THERE IS AN ERROR IT IS PROBABLY HERE!!!!!!
			float x = iit.x(), y = iit.y(), z = iit.z();
			float m00 = m00(), m01 = m01(), m02 = m02();
			float m10 = m10(), m11 = m11(), m12 = m12();
			float m20 = m20(), m21 = m21(), m22 = m22();
			
			float nm00 = m00 * x, nm01 = m01 * y, nm02 = m02 * z;
			float nm10 = m10 * x, nm11 = m11 * y, nm12 = m12 * z;
			float nm20 = m20 * x, nm21 = m21 * y, nm22 = m22 * z;
			
			float nnm00 = nm00 * m00 + nm01 * m10 + nm02 * m20, nnm01 = nm00 * m01 + nm01 * m11 + nm02 * m21, nnm02 = nm00 * m02 + nm01 * m12 + nm02 * m22;
			float nnm10 = nm10 * m00 + nm11 * m10 + nm12 * m20, nnm11 = nm10 * m01 + nm11 * m11 + nm12 * m21, nnm12 = nm10 * m02 + nm11 * m12 + nm12 * m22;
			float nnm20 = nm20 * m00 + nm21 * m10 + nm22 * m20, nnm21 = nm20 * m01 + nm21 * m11 + nm22 * m21, nnm22 = nm20 * m02 + nm21 * m12 + nm22 * m22;
			
			return out.set(
					-nnm00, -nnm10, -nnm20,
					-nnm01, -nnm11, -nnm21,
					-nnm02, -nnm12, -nnm22,
					0);
		}
		
		//skew = x y z, iit = u v w
		public static Matrix3f impulseToTorque(Vector3f skew, Vector3f iit, Matrix3f out) {
			float x = skew.x(), y = skew.y(), z = skew.z();
			float u = iit.x(), v = iit.y(), w = iit.z();
			
			float wxy = -w * x * y;
			float vxz = -v * x * z;
			float uyz = -u * y * z;
			float x2 = x * x, y2 = y * y, z2 = z * z;
			
			float m00 = v * z2 + w * y2, m11 = u * z2 + w * x2, m22 = u * y2 + v * x2;
			
			return out.set(
					m00, wxy, vxz,
					wxy, m11, uyz,
					vxz, uyz, m22,
					0);
		}
		
		public Matrix3f setSkewSymmetric(Vector3f r) {
			float x = r.x(), y = r.y(), z = r.z();
			return set(
					0f, -z, y,
					z, 0f, -x,
					-y, x, 0f,
					0);
		}
	
	//Stupid generated boilerplate code\\

	public static final int SIZEOF;
	public static final int ALIGNOF;
    public static final int
        M00, M01, M02,
        M10, M11, M12,
        M20, M21, M22;
    
    static {
    	Layout layout = __struct(
            __member(4), __member(4), __member(4),
            __member(4), __member(4), __member(4),
            __member(4), __member(4), __member(4)
        );
    	SIZEOF = layout.getSize();
    	ALIGNOF = layout.getAlignment();

        M00 = layout.offsetof(0); M01 = layout.offsetof(1); M02 = layout.offsetof(2);
        M10 = layout.offsetof(3); M11 = layout.offsetof(4); M12 = layout.offsetof(5);
        M20 = layout.offsetof(6); M21 = layout.offsetof(7); M22 = layout.offsetof(8);
    }
	
	Matrix3f(long address, ByteBuffer container) { super(address, container); }
	
    public Matrix3f(ByteBuffer container) {
        this(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() { return SIZEOF; }
    
    public static Matrix3f malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Matrix3f calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Matrix3f create() {return new Matrix3f(BufferUtils.createByteBuffer(SIZEOF));}
    public static Matrix3f create(long address) {return new Matrix3f(address, null);}
    public static Matrix3f createSafe(long address) {return address == NULL ? null : create(address);}
    public static Matrix3f.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Matrix3f.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    public static Matrix3f mallocStack() {return mallocStack(stackGet());}
    public static Matrix3f callocStack() {return callocStack(stackGet());}
    public static Matrix3f mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Matrix3f callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    public static Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}

    
    public float nm00(long struct) {return MemoryUtil.memGetFloat(struct + M00);} public float nm01(long struct) {return MemoryUtil.memGetFloat(struct + M01);} 
	public float nm02(long struct) {return MemoryUtil.memGetFloat(struct + M02);}
	public float nm10(long struct) {return MemoryUtil.memGetFloat(struct + M10);} public float nm11(long struct) {return MemoryUtil.memGetFloat(struct + M11);} 
	public float nm12(long struct) {return MemoryUtil.memGetFloat(struct + M12);}
	public float nm20(long struct) {return MemoryUtil.memGetFloat(struct + M20);} public float nm21(long struct) {return MemoryUtil.memGetFloat(struct + M21);} 
	public float nm22(long struct) {return MemoryUtil.memGetFloat(struct + M22);}

	public void nm00(long struct, float value) {MemoryUtil.memPutFloat(struct + M00, value);} public void nm01(long struct, float value) {MemoryUtil.memPutFloat(struct + M01, value);} 
	public void nm02(long struct, float value) {MemoryUtil.memPutFloat(struct + M02, value);}
	public void nm10(long struct, float value) {MemoryUtil.memPutFloat(struct + M10, value);} public void nm11(long struct, float value) {MemoryUtil.memPutFloat(struct + M11, value);} 
	public void nm12(long struct, float value) {MemoryUtil.memPutFloat(struct + M12, value);}
	public void nm20(long struct, float value) {MemoryUtil.memPutFloat(struct + M20, value);} public void nm21(long struct, float value) {MemoryUtil.memPutFloat(struct + M21, value);} 
	public void nm22(long struct, float value) {MemoryUtil.memPutFloat(struct + M22, value);}

    public static class Buffer extends StructBuffer<Matrix3f, Buffer> implements NativeResource {
    	private static final Matrix3f ELEMENT_FACTORY = Matrix3f.create(-1L);

        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}
        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}
		@Override
		protected Matrix3f getElementFactory() {return ELEMENT_FACTORY;}
    }
}
