package math;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType("struct matrix4f")
public class Matrix4f extends Struct implements NativeResource {
	public static final int IDENTITY = 0b1;
	public static final int AFFINE = 0b10;
	public static final int TRANSLATION = 0b100;
	public static final int PERSPECTIVE = 0b1000;
	public static final int ORTHOGRAPHIC = 0b10000;
    
    private int prop; //Optimization field
    public Matrix4f() {
    	this(BufferUtils.createByteBuffer(SIZEOF));
    	this.identity();
    }
    //Safe Memory Retrieval
    public float m00() {return nm00(address());} public float m01() {return nm01(address());} public float m02() {return nm02(address());} public float m03() {return nm03(address());}
    public float m10() {return nm10(address());} public float m11() {return nm11(address());} public float m12() {return nm12(address());} public float m13() {return nm13(address());}
    public float m20() {return nm20(address());} public float m21() {return nm21(address());} public float m22() {return nm22(address());} public float m23() {return nm23(address());}
    public float m30() {return nm30(address());} public float m31() {return nm31(address());} public float m32() {return nm32(address());} public float m33() {return nm33(address());}
    //Safe Memory Assignment
    public void m00(float value) {nm00(address(), value);} public void m01(float value) {nm01(address(), value);} 
    public void m02(float value) {nm02(address(), value);} public void m03(float value) {nm03(address(), value);}
    public void m10(float value) {nm10(address(), value);} public void m11(float value) {nm11(address(), value);} 
    public void m12(float value) {nm12(address(), value);} public void m13(float value) {nm13(address(), value);}
    public void m20(float value) {nm20(address(), value);} public void m21(float value) {nm21(address(), value);} 
    public void m22(float value) {nm22(address(), value);} public void m23(float value) {nm23(address(), value);}
    public void m30(float value) {nm30(address(), value);} public void m31(float value) {nm31(address(), value);} 
    public void m32(float value) {nm32(address(), value);} public void m33(float value) {nm33(address(), value);}
	//Property Retrieval and Assignment
	public int prop() {return  prop;}
	public void prop(int value) {prop = value;}
	
	//~~~~~~~~~~~SETTERS~~~~~~~~~~~\\
	public Matrix4f set(Matrix4f src) {
		memCopy(src.address(), address(), SIZEOF);
		prop = src.prop;
		return this;
	}
	public Matrix4f set(float m00, float m01, float m02, float m03,
						float m10, float m11, float m12, float m13,
						float m20, float m21, float m22, float m23,
						float m30, float m31, float m32, float m33) {
		return set(
				m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				m30, m31, m32, m33,
				0);
	}
	public Matrix4f set(float m00, float m01, float m02, float m03,
						float m10, float m11, float m12, float m13,
						float m20, float m21, float m22, float m23,
						float m30, float m31, float m32, float m33,
						int prop) {
		m00(m00); m01(m01); m02(m02); m03(m03);
		m10(m10); m11(m11); m12(m12); m13(m13);
		m20(m20); m21(m21); m22(m22); m23(m23);
		m30(m30); m31(m31); m32(m32); m33(m33);
		prop(prop);
		return this;
	}
	
	public Matrix4f set(float m00, float m01, float m02, float m03,
						float m10, float m11, float m12, float m13,
						float m20, float m21, float m22, float m23,
						int prop) {
		m00(m00); m01(m01); m02(m02); m03(m03);
		m10(m10); m11(m11); m12(m12); m13(m13);
		m20(m20); m21(m21); m22(m22); m23(m23);
		prop(prop);
		return this;
	}
	
	public Matrix4f identity() {
		if((prop() & IDENTITY) != 0) return this;
		return set(
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1,
				IDENTITY | AFFINE | TRANSLATION);
	}
	
	public Matrix4f set(Matrix3f m) {
		return set(
				m.m00(), m.m01(), m.m02(), 0,
				m.m10(), m.m11(), m.m12(), 0,
				m.m20(), m.m21(), m.m22(), 0,
				0, 0, 0, 1, 
				AFFINE);
	}
	//Unsafe quick look at matrix
	//Assumes up is 0 1 0
	public Matrix4f mat4(Vector3f dir) {
		float angle2 = (float) Math.atan2(dir.x(), dir.z()) * 0.5f;
		float y = (float) Math.sin(angle2);
		float yy = y * y, yw = y * (float) Math.cos(angle2);
		
		float m00 = 1 - 2 * yy;	float m01 = 0f;	float m02 = 2 * yw ;	float m03 = 0f;
		float m10 = 0f;			float m11 = 1f;	float m12 = 0f;			float m13 = 0f;
		float m20 = 2 * -yw;	float m21 = 0f;	float m22 = 1 - 2 * yy;	float m23 = 0f;
		float m30 = 0f;			float m31 = 0f;	float m32 = 0f;			float m33 = 1f;
		
		return set(
				m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				m30, m31, m32, m33,
				AFFINE);
	}
	
	public Matrix4f mat4(float m00, float m01, float m02,
			float m10, float m11, float m12,
			float m20, float m21, float m22,
			int prop) {
		m00(m00); m01(m01); m02(m02);
		m10(m10); m11(m11); m12(m12);
		m20(m20); m21(m21); m22(m22);
		prop(prop);
		return this;
	}
	
	//~~~~~~~~~~~TRANSLATION~~~~~~~~~~~\\
	
	public Matrix4f setTranslation(Vector3f v) {return translation(v.x(), v.y(), v.z());}
	public Matrix4f setTranslation(float x, float y, float z) {
		return set(
				1, 0, 0, x,
				0, 1, 0, y,
				0, 0, 1, z,
				0, 0, 0, 1,
				AFFINE | TRANSLATION);
	}
	public Matrix4f translation(Vector3f v) {return setTranslation(v.x(), v.y(), v.z());}
	public Matrix4f translation(float x, float y, float z) {
		m03(x); m13(y); m23(z);
		prop(prop() & ~(PERSPECTIVE | IDENTITY));
        return this;
    }
	
	//TODO: Probably optimize
	public Matrix4f translate(Vector3f v) {return translate(v.x(), v.y(), v.z());}
	public Matrix4f translate(float x, float y, float z) {
		m03(m00() * x + m01() * y + m02() * z + m03());
		m13(m10() * x + m11() * y + m12() * z + m13());
		m23(m20() * x + m21() * y + m22() * z + m23());
		if((prop & AFFINE) != 0) m33(m30() * x + m31() * y + m32() * z + m33());
		prop(prop() & ~(PERSPECTIVE | IDENTITY));
		return this;
	}
	//Yeah not sure about this left translate shenanigans
	public Matrix4f leftTranslate(Vector3f v) {return leftTranslate(v.x(), v.y(), v.z());}
	public Matrix4f leftTranslate(float x, float y, float z) {
		m30(m00() * x + m10() * y + m20() * z + m30());
		m31(m01() * x + m11() * y + m21() * z + m31());
		m32(m02() * x + m12() * y + m22() * z + m32());
		m33(m03() * x + m13() * y + m23() * z + m33());
		prop(prop() & ~(PERSPECTIVE | IDENTITY));
		return this;
	}
	
	//~~~~~~~~~~~ROTATION~~~~~~~~~~~\\
	
	public Matrix4f setRotation(Vector3f axis, float angle) {return setRotation(axis.x(), axis.y(), axis.z(), angle);}
	public Matrix4f setRotation(float x, float y, float z, float angle) {
		float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        float cos2 = 1.0f - cos;
        float xyc = x * y * cos2, xzc = x * z * cos2, yzc = y * z * cos2;
        float xs = x * sin, ys = y * sin, zs = z * sin;
        float m00 = cos + x * x * cos2; 	float m01 = xyc - zs; 				float m02 = xzc + ys;
		float m10 = xyc + zs; 				float m11 = cos + y * y * cos2; 	float m12 = yzc - xs;
		float m20 = xzc - ys; 				float m21 = yzc + xs; 				float m22 = cos + z * z * cos2;
		return set(
				m00, m01, m02, 0f,
				m10, m11, m12, 0f,
				m20, m21, m22, 0f,
				0f, 0f, 0f, 1f,
				AFFINE);
	}
	public Matrix4f setRotation(Quaternion q) {
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
				m00, m01, m02, 0f,
				m10, m11, m12, 0f,
				m20, m21, m22, 0f,
				0f, 0f, 0f, 1f,
				AFFINE);
	}
	
	//~~~~~~~~~~~SCALE~~~~~~~~~~~\\
	
	public Matrix4f setScale(float scale) {return setScale(scale, scale ,scale);}
	public Matrix4f setScale(Vector3f v) {return setScale(v.x(), v.y(), v.z());}
	public Matrix4f setScale(float x, float y, float z) {
		return set(
				x, 0, 0, 0,
				0, y, 0, 0,
				0, 0, z, 0,
				0, 0, 0, 1,
				AFFINE);
	}
	
	public Matrix4f scale(float scale) {return scale(scale, scale, scale, this);}
	public Matrix4f scale(float x, float y, float z) {return scale(x, y, z, this);}
	public Matrix4f scale(Vector3f v) {return scale(v.x(), v.y(), v.z(), this);}
	public Matrix4f scale(float scale, Matrix4f out) {return scale(scale, scale, scale, out);}
	public Matrix4f scale(Vector3f v, Matrix4f out) {return scale(v.x(), v.y(), v.z(), out);}
	public Matrix4f scale(float x, float y, float z, Matrix4f out) {
		if ((prop() & IDENTITY) != 0) return out.setScale(x, y, z);
		//TODO: Scaling Optimizations
		//Load from memory
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		//Calculate
		float nm00 = m00 * x; float nm01 = m01 * y; float nm02 = m02 * z; float nm03 = m03;
        float nm10 = m10 * x; float nm11 = m11 * y; float nm12 = m12 * z; float nm13 = m13;
        float nm20 = m20 * x; float nm21 = m21 * y; float nm22 = m22 * z; float nm23 = m23;
        float nm30 = m30 * x; float nm31 = m31 * y; float nm32 = m32 * z; float nm33 = m33;
        //Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				nm30, nm31, nm32, nm33,
				prop() & ~(IDENTITY | TRANSLATION | PERSPECTIVE));
	}
	
	//~~~~~~~~~~~TRANSFORMATION~~~~~~~~~~~\\
	//TODO: Stop myself from optimizing this any further
	public Matrix4f translationRotateScale(Vector3f t, Quaternion r, float scale) {return translationRotateScale(t.x(), t.y(), t.z(), r.x(), r.y(), r.z(), r.w(), scale, scale, scale);}
	public Matrix4f translationRotateScale(Vector3f t, Quaternion r, Vector3f s) {return translationRotateScale(t.x(), t.y(), t.z(), r.x(), r.y(), r.z(), r.w(), s.x(), s.y(), s.z());}
	public Matrix4f translationRotateScale( float tx, float ty, float tz, 
    										float qx, float qy, float qz, float qw, 
    										float sx, float sy, float sz) {
		float dqx = qx + qx, dqy = qy + qy, dqz = qz + qz;
		float q00 = dqx * qx, q11 = dqy * qy, q22 = dqz * qz;
		float q01 = dqx * qy, q02 = dqx * qz, q03 = dqx * qw;
		float q12 = dqy * qz, q13 = dqy * qw, q23 = dqz * qw;
		float m00 = sx - (q11 + q22) * sx; 	float m01 = (q01 - q23) * sy; 		float m02 = (q02 + q13) * sz;
		float m10 = (q01 + q23) * sx; 		float m11 = sy - (q22 + q00) * sy; 	float m12 = (q12 - q03) * sz;
		float m20 = (q02 - q13) * sx; 		float m21 = (q12 + q03) * sy; 		float m22 = sz - (q11 + q00) * sz;
		return set(
				m00, m01, m02, tx,
				m10, m11, m12, ty,
				m20, m21, m22, tz,
				0f, 0f, 0f, 1f,
				AFFINE);//TODO: Add transformation matrix property
	}
	//May not actually be as efficient as I once thought
	public Matrix4f inverseTRS(Vector3f t, Quaternion r, float scale) {return inverseTRS(t.x(), t.y(), t.z(), r.x(), r.y(), r.z(), r.w(), scale, scale, scale);}
	public Matrix4f inverseTRS(Vector3f t, Quaternion r, Vector3f s) {return inverseTRS(t.x(), t.y(), t.z(), r.x(), r.y(), r.z(), r.w(), s.x(), s.y(), s.z());}
	public Matrix4f inverseTRS( float tx, float ty, float tz, 
								float qx, float qy, float qz, float qw, 
								float sx, float sy, float sz) {
		float nqx = -qx, nqy = -qy, nqz = -qz;
        float dqx = nqx + nqx, dqy = nqy + nqy, dqz = nqz + nqz;
        float q00 = dqx * nqx, q11 = dqy * nqy, q22 = dqz * nqz;
        float q01 = dqx * nqy, q02 = dqx * nqz, q03 = dqx * qw;
        float q12 = dqy * nqz, q13 = dqy * qw, q23 = dqz * qw;
        float isx = 1/sx, isy = 1/sy, isz = 1/sz;
        float m00 = isx * (1.0f - q11 - q22); float m01 = isx * (q01 - q23); 		float m02 = isx * (q02 + q13); 		
		float m10 = isy * (q01 + q23); 		float m11 = isy * (1.0f - q22 - q00); float m12 = isy * (q12 - q03); 		
		float m20 = isz * (q02 - q13); 		float m21 = isz * (q12 + q03); 		float m22 = isz * (1.0f - q11 - q00); 
		float m03 = -m00 * tx - m01 * ty - m02 * tz; 
		float m13 = -m10 * tx - m11 * ty - m12 * tz; 
		float m23 = -m20 * tx - m21 * ty - m22 * tz;
		return set(
				m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				0f, 0f, 0f, 1f,
				AFFINE);
	}
	
	
	//~~~~~~~~~~~MULTIPLICATION~~~~~~~~~~~\\
	
	public Matrix4f mul(Matrix4f right) {return mul(right, this);}
	public Matrix4f mul(Matrix4f right, Matrix4f out) {
		int prop = prop();
		if ((prop & IDENTITY) != 0) return out.set(right);
		if ((right.prop() & IDENTITY) != 0) return out.set(this);
        if ((right.prop() & AFFINE) != 0) {
        	if((prop & TRANSLATION) != 0) 	return multiplyTranslation(right, out);
        	if((prop & ORTHOGRAPHIC) != 0)  return multiplyOrthographic(right, out);
        	if((prop & AFFINE) != 0)		return multiplyAffine(right, out);
        	if((prop & PERSPECTIVE) != 0) 	return multiplyPerspective(right, out);
        }
        return multiplyGeneric(right, out);
	}	
	public Matrix4f multiplyGeneric(Matrix4f right, Matrix4f out) {
		//Load from memory
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		
		float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02(), rm03 = right.m03();
		float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12(), rm13 = right.m13();
		float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22(), rm23 = right.m23();
		float rm30 = right.m30(), rm31 = right.m31(), rm32 = right.m32(), rm33 = right.m33();
		//Calculate
		float nm00 = m00 * rm00 + m01 * rm10 + m02 * rm20 + m03 * rm30;
		float nm10 = m10 * rm00 + m11 * rm10 + m12 * rm20 + m13 * rm30;
		float nm20 = m20 * rm00 + m21 * rm10 + m22 * rm20 + m23 * rm30;
		float nm30 = m30 * rm00 + m31 * rm10 + m32 * rm20 + m33 * rm30;
		float nm01 = m00 * rm01 + m01 * rm11 + m02 * rm21 + m03 * rm31;
		float nm11 = m10 * rm01 + m11 * rm11 + m12 * rm21 + m13 * rm31;
		float nm21 = m20 * rm01 + m21 * rm11 + m22 * rm21 + m23 * rm31;
		float nm31 = m30 * rm01 + m31 * rm11 + m32 * rm21 + m33 * rm31;
		float nm02 = m00 * rm02 + m01 * rm12 + m02 * rm22 + m03 * rm32;
		float nm12 = m10 * rm02 + m11 * rm12 + m12 * rm22 + m13 * rm32;
		float nm22 = m20 * rm02 + m21 * rm12 + m22 * rm22 + m23 * rm32;
		float nm32 = m30 * rm02 + m31 * rm12 + m32 * rm22 + m33 * rm32;
		float nm03 = m00 * rm03 + m01 * rm13 + m02 * rm23 + m03 * rm33;
		float nm13 = m10 * rm03 + m11 * rm13 + m12 * rm23 + m13 * rm33;
		float nm23 = m20 * rm03 + m21 * rm13 + m22 * rm23 + m23 * rm33;
		float nm33 = m30 * rm03 + m31 * rm13 + m32 * rm23 + m33 * rm33;
		//Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				nm30, nm31, nm32, nm33,
				0);
	}	
	public Matrix4f multiplyAffine(Matrix4f right, Matrix4f out) {
		//Load from memory
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		
		float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02(), rm03 = right.m03();
		float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12(), rm13 = right.m13();
		float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22(), rm23 = right.m23();
		//Calculate
		float nm00 = m00 * rm00 + m01 * rm10 + m02 * rm20;
        float nm10 = m10 * rm00 + m11 * rm10 + m12 * rm20;
        float nm20 = m20 * rm00 + m21 * rm10 + m22 * rm20;
        float nm30 = m30;
        float nm01 = m00 * rm01 + m01 * rm11 + m02 * rm21;
        float nm11 = m10 * rm01 + m11 * rm11 + m12 * rm21;
        float nm21 = m20 * rm01 + m21 * rm11 + m22 * rm21;
        float nm31 = m31;
        float nm02 = m00 * rm02 + m01 * rm12 + m02 * rm22;
        float nm12 = m10 * rm02 + m11 * rm12 + m12 * rm22;
        float nm22 = m20 * rm02 + m21 * rm12 + m22 * rm22;
        float nm32 = m32;
        float nm03 = m00 * rm03 + m01 * rm13 + m02 * rm23 + m03;
        float nm13 = m10 * rm03 + m11 * rm13 + m12 * rm23 + m13;
        float nm23 = m20 * rm03 + m21 * rm13 + m22 * rm23 + m23;
        float nm33 = m33;
        
      //Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				nm30, nm31, nm32, nm33,
				AFFINE);
	}	
	public Matrix4f multiplyTranslation(Matrix4f right, Matrix4f out) {
		//Load from memory
		float m03 = m03();
		float m13 = m13();
		float m23 = m23();
		float m33 = m33();
		
		float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02(), rm03 = right.m03();
		float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12(), rm13 = right.m13();
		float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22(), rm23 = right.m23();
		float rm30 = right.m30(), rm31 = right.m31(), rm32 = right.m32();
        //Save to memory
        return out.set(
				rm00, rm01, rm02, m03 + rm03,
				rm10, rm11, rm12, m13 + rm13,
				rm20, rm21, rm22, m23 + rm23,
				rm30, rm31, rm32, m33,
				AFFINE);
	}
	public Matrix4f multiplyPerspective(Matrix4f right, Matrix4f out) {
		//Load from memory
		float m00 = m00(), m11 = m11(), m22 = m22(), m23 = m23(), m32 = m32();
		
		float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02(), rm03 = right.m03();
		float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12(), rm13 = right.m13();
		float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22(), rm23 = right.m23();
		//Calculate
		float nm00 = m00 * rm00;
        float nm10 = m11 * rm10;
        float nm20 = m22 * rm20;
        float nm30 = m32 * rm20;
        float nm01 = m00 * rm01;
        float nm11 = m11 * rm11;
        float nm21 = m22 * rm21;
        float nm31 = m32 * rm21;
        float nm02 = m00 * rm02;
        float nm12 = m11 * rm12;
        float nm22 = m22 * rm22;
        float nm32 = m32 * rm22;
        float nm03 = m00 * rm03;
        float nm13 = m11 * rm13;
        float nm23 = m22 * rm23 + m23;
        float nm33 = m32 * rm23;
        //Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				nm30, nm31, nm32, nm33,
				0);
	}
	public Matrix4f multiplyOrthographic(Matrix4f right, Matrix4f out) {
		//Load from memory
		float m00 = m00(), m03 = m03();
		float m11 = m11(), m13 = m13();
		float m22 = m22(), m23 = m23();
		
		float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02(), rm03 = right.m03();
		float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12(), rm13 = right.m13();
		float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22(), rm23 = right.m23();
		//Calculate
		float nm00 = m00 * rm00;
        float nm10 = m11 * rm10;
        float nm20 = m22 * rm20;
        float nm01 = m00 * rm01;
        float nm11 = m11 * rm11;
        float nm21 = m22 * rm21;
        float nm02 = m00 * rm02;
        float nm12 = m11 * rm12;
        float nm22 = m22 * rm22;
        float nm03 = m00 * rm03 + m03;
        float nm13 = m11 * rm13 + m13;
        float nm23 = m22 * rm23 + m23;
        //Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				0f, 0f, 0f, 1f,
				AFFINE);
	}
	
	//~~~~~~~~~~~TRANSFORM~~~~~~~~~~~\\
	
	public Vector3f transform(Vector3f v) {return transform(v, v);}
	public Vector3f transform(Vector3f v, Vector3f out) {return transform(v.x(), v.y(), v.z(), out);}
	public Vector3f transform(float x, float y, float z, Vector3f out) {
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		
		return out.set(m00 * x + m01 * y + m02 * z + m03, m10 * x + m11 * y + m12 * z + m13, m20 * x + m21 * y + m22 * z + m23);
	}
	
	//~~~~~~~~~~~INVERSE~~~~~~~~~~~\\
	//NEVER USE INVERSE EVER IT SUCKSSSSSSSS
	
	public Matrix4f invert() {return invert(this);}
	public Matrix4f invert(Matrix4f out) {
		int prop = prop();
        if ((prop & IDENTITY) != 0) return out.identity();
        //if ((prop & AFFINE) != 0) return invertAffine(out);
        //if ((m.prop & PERSPECTIVE) != 0) return invertPerspective(m, out);
        return invertGeneric(out);
    }
	public Matrix4f invertGeneric(Matrix4f out) {
		//Load from memory
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		
		float a = m22 * m33 - m23 * m32;
		float b = m21 * m33 - m23 * m31;
		float c = m21 * m32 - m22 * m31;
		float d = m20 * m33 - m23 * m30;
		float e = m20 * m32 - m22 * m30;
		float f = m20 * m31 - m21 * m30;
		
		float aM00 = (m11 * a - m12 * b + m13 * c);//+
		float aM01 = (m10 * a - m12 * d + m13 * e);//-
		float aM02 = (m10 * b - m11 * d + m13 * f);//+
		float aM03 = (m10 * c - m11 * e + m12 * f);//-
		
		float det = 1.0f / (m00 * aM00 - m01 * aM01 + m02 * aM02 - m03 * aM03);
		
		float aM10 = (m01 * a - m02 * b + m03 * c);//+
		float aM11 = (m00 * a - m02 * d + m03 * e);//-
		float aM12 = (m00 * b - m01 * d + m03 * f);//+
		float aM13 = (m00 * c - m01 * e + m02 * f);//-
		
		float g = (m02 * m13 - m03 * m12) * det;//Applying the determinant here saves 2 multiplications
		float h = (m01 * m13 - m03 * m11) * det;
		float i = (m01 * m12 - m02 * m11) * det;
		float j = (m00 * m13 - m03 * m10) * det;
		float k = (m00 * m12 - m02 * m10) * det;
		float l = (m00 * m11 - m01 * m10) * det;
		
		float aM20 = (m31 * g - m32 * h + m33 * i);//+
		float aM21 = (m30 * g - m32 * j + m33 * k);//-
		float aM22 = (m30 * h - m31 * j + m33 * l);//+
		float aM23 = (m30 * i - m31 * k + m32 * l);//-
		
		float aM30 = (m21 * g - m22 * h + m23 * i);//+
		float aM31 = (m20 * g - m22 * j + m23 * k);//-
		float aM32 = (m20 * h - m21 * j + m23 * l);//+
		float aM33 = (m20 * i - m21 * k + m22 * l);//-
		
		float nm00 = aM00 * det, nm01 = -aM10 * det, nm02 = aM20, nm03 = -aM30;
		float nm10 = -aM01 * det, nm11 = aM11 * det, nm12 = -aM21, nm13 = aM31;
		float nm20 = aM02 * det, nm21 = -aM12 * det, nm22 = aM22, nm23 = -aM32;
		float nm30 = -aM03 * det, nm31 = aM13 * det, nm32 = -aM23, nm33 = aM33;
		
		//Save to memory
        return out.set(
				nm00, nm01, nm02, nm03,
				nm10, nm11, nm12, nm13,
				nm20, nm21, nm22, nm23,
				nm30, nm31, nm32, nm33,
				0);
	}
	
	//~~~~~~~~~~~DETERMINANT~~~~~~~~~~~\\
	
	public float determinant() {
//		int prop = prop();
//        if ((prop & AFFINE) != 0) return determinantAffine();
        return determinantGeneric();
    }
	public float determinantGeneric() {
		//Load from memory
        float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		float a = m22 * m33 - m23 * m32, b = m21 * m33 - m23 * m31, c = m21 * m32 - m22 * m31;
		float d = m20 * m33 - m23 * m30, e = m20 * m32 - m22 * m30, f = m20 * m31 - m21 * m30;
		//Calculate and return
        return 	m00 * (m11 * a - m12 * b + m13 * c)
        	- 	m01 * (m10 * a - m12 * d + m13 * e)
        	+ 	m02 * (m10 * b - m11 * d + m13 * f)
        	- 	m03 * (m10 * c - m11 * e + m12 * f);
	}
//	public float determinantAffine() {
//		float m00 = m00(), m01 = m01(), m02 = m02();
//		float m10 = m10(), m11 = m11(), m12 = m12();
//		float m20 = m20(), m21 = m21(), m22 = m22();
//        return (m00 * m11 - m01 * m10) * m22
//             + (m20 * m01 - m00 * m21) * m12
//             + (m10 * m21 - m20 * m11) * m02;
//    }
	//TODO: More optimizations
	
	//~~~~~~~~~~~PROJECTION~~~~~~~~~~~\\
	
	public Matrix4f perspective(float fov, float aspect, float near, float far) {return perspective(fov, aspect, near, far, this);}
	public Matrix4f perspective(float fov, float aspect, float near, float far, Matrix4f out){
		float tanFov2 = 1.0f / (float)Math.tan((fov * 0.5f) * Constants.RADIAN), 
				range = 1.0f / (near - far);
		return out.set(
				tanFov2 / aspect, 0f, 0f, 0f,
				0f, -tanFov2, 0f, 0f,
				0f, 0f, far * range, far * near * range,
				0f, 0f, -1.0f, 0f,
				PERSPECTIVE);
	}
	//TODO: Update for Vulkan
	public Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {return orthographic(left, right, bottom, top, near, far, this);}
	public Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far, Matrix4f out){
		float width = 1.0f / (right - left), height = 1.0f / (top - bottom), depth = 1.0f / (far - near);
		return out.set(
				width + width, 0f, 0f, -(right + left) * width,
				0f, height + height, 0f, -(top + bottom) * height,
				0f, 0f, -(depth + depth), -(far + near) * depth,
				0f, 0f, 0f, 1f,
				AFFINE | ORTHOGRAPHIC);
	}
	
	//~~~~~~~~~~~LOOK AT~~~~~~~~~~~\\
	
	public Matrix4f setLookAt(Vector3f pos, Vector3f target, Vector3f up) {
		//Load from memory
		float pX = pos.x(), pY = pos.y(), pZ = pos.z();
		float tX = target.x(), tY = target.y(), tZ = target.z();
		float uX = up.x(), uY = up.y(), uZ = up.z();
		//Calculate forward vector
		float fX = tX - pX, fY = tY - pY, fZ = tZ - pZ;
		//Normalize
		float lenF = 1.0f / (float) Math.sqrt(fX * fX + fY * fY + fZ * fZ);
		fX *= lenF; fY *= lenF; fZ *= lenF;
		float lenU = 1.0f / (float) Math.sqrt(uX * uX + uY * uY + uZ * uZ);
		uX *= lenU; uY *= lenU; uZ *= lenU;
		//right = up x forward
		float rX = uY * fZ - uZ * fY, rY  = uZ * fX - uX * fZ, rZ = uX * fY - uY * fX;
		//normalize right
		float lenR = 1.0f / (float) Math.sqrt(rX * rX + rY * rY + rZ * rZ);
		rX *= lenR; rY *= lenR; rZ *= lenR;
		//upNew = forward x right
		float unX = fY * rZ - fZ * rY, unY = fZ * rX - fX * rZ, unZ = fX * rY - fY * rX;
		//normalize upNew
		float lenUn = 1.0f / (float) Math.sqrt(unX * unX + unY * unY + unZ * unZ);
		unX *= lenUn; unY *= lenUn; unZ *= lenUn;
		
		return set(
				rX, rY, rZ, 0f,
				unX, unY, unZ, 0f,
				fX, fY, fZ, 0f,
				0f, 0f, 0f, 1f,
				AFFINE);
	}
	
	//~~~~~~~~~~~TRANSPOSE~~~~~~~~~~~\\
	
	public Matrix4f transpose() {return transpose(this);}
	public Matrix4f transpose(Matrix4f out) {
		 return out.set(
					m00(), m10(), m20(), m30(),
					m01(), m11(), m21(), m31(),
					m02(), m12(), m22(), m32(),
					m03(), m13(), m23(), m33(),
					prop() & ~PERSPECTIVE);
	}
	
	//~~~~~~~~~~~MISC~~~~~~~~~~~\\
	
	public Vector4f transform(Vector4f v) {return transform(v, v);}
	public Vector4f transform(Vector4f v, Vector4f out) {
		float x = v.x(), y = v.y(), z = v.z(), w = v.w();
		
		float m00 = m00(), m01 = m01(), m02 = m02(), m03 = m03();
		float m10 = m10(), m11 = m11(), m12 = m12(), m13 = m13();
		float m20 = m20(), m21 = m21(), m22 = m22(), m23 = m23();
		float m30 = m30(), m31 = m31(), m32 = m32(), m33 = m33();
		
		float nx = m00 * x + m01 * y + m02 * z + m03 * w;
		float ny = m10 * x + m11 * y + m12 * z + m13 * w;
		float nz = m20 * x + m21 * y + m22 * z + m23 * w;
		float nw = m30 * x + m31 * y + m32 * z + m33 * w;
		return out.xyzw(nx, ny, nz, nw);
	}
	
	public String toString() {
		return 	m00() + ", " + m01() + ", " + m02() + ", " + m03() + "\n" + 
				m10() + ", " + m11() + ", " + m12() + ", " + m13() + "\n" + 
				m20() + ", " + m21() + ", " + m22() + ", " + m23() + "\n" + 
				m30() + ", " + m31() + ", " + m32() + ", " + m33();
	}
	
	//Stupid generated boilerplate code\\
	
	public static final int SIZEOF;
	public static final int ALIGNOF;
    public static final int
        M00, M01, M02, M03,
        M10, M11, M12, M13,
        M20, M21, M22, M23,
        M30, M31, M32, M33;
    
    static {
    	Layout layout = __struct(
            __member(4), __member(4), __member(4), __member(4),
            __member(4), __member(4), __member(4), __member(4),
            __member(4), __member(4), __member(4), __member(4),
            __member(4), __member(4), __member(4), __member(4)
        );
    	SIZEOF = layout.getSize();
    	ALIGNOF = layout.getAlignment();
    	
    	//OpenGL memory layout?
//        M00 = layout.offsetof(0); M01 = layout.offsetof(1); M02 = layout.offsetof(2); M03 = layout.offsetof(3);
//        M10 = layout.offsetof(4); M11 = layout.offsetof(5); M12 = layout.offsetof(6); M13 = layout.offsetof(7);
//        M20 = layout.offsetof(8); M21 = layout.offsetof(9); M22 = layout.offsetof(10); M23 = layout.offsetof(12);
//        M30 = layout.offsetof(11); M31 = layout.offsetof(13); M32 = layout.offsetof(14); M33 = layout.offsetof(15);
        
    	//Vulkan memory layout
        //TRANSPOSED
        M00 = layout.offsetof(0); M01 = layout.offsetof(4); M02 = layout.offsetof(8); M03 = layout.offsetof(12);
        M10 = layout.offsetof(1); M11 = layout.offsetof(5); M12 = layout.offsetof(9); M13 = layout.offsetof(13);
        M20 = layout.offsetof(2); M21 = layout.offsetof(6); M22 = layout.offsetof(10); M23 = layout.offsetof(14);
        M30 = layout.offsetof(3); M31 = layout.offsetof(7); M32 = layout.offsetof(11); M33 = layout.offsetof(15);
        
    }
	
	Matrix4f(long address, @Nullable ByteBuffer container) {super(address, container);}
    public Matrix4f(ByteBuffer container) {this(memAddress(container), __checkContainer(container, SIZEOF));}
    
    @Override
    public int sizeof() {return SIZEOF;}
    //Standard Allocation
    public static Matrix4f malloc() {return create(nmemAllocChecked(SIZEOF));}
    public static Matrix4f calloc() {return create(nmemCallocChecked(1, SIZEOF));}
    public static Matrix4f create() {return new Matrix4f(BufferUtils.createByteBuffer(SIZEOF));}
    public static Matrix4f create(long address) {return new Matrix4f(address, null);}
    public static Matrix4f createSafe(long address) {return address == NULL ? null : create(address);}
    //Buffer Allocation
    public static Matrix4f.Buffer malloc(int capacity) {return create(__checkMalloc(capacity, SIZEOF), capacity);}
    public static Matrix4f.Buffer calloc(int capacity) {return create(nmemCallocChecked(capacity, SIZEOF), capacity);}
    public static Matrix4f.Buffer create(int capacity) {return new Buffer(__create(capacity, SIZEOF));}
    public static Matrix4f.Buffer create(long address, int capacity) {return new Buffer(address, capacity);}
    public static Matrix4f.@Nullable Buffer createSafe(long address, int capacity) {return address == NULL ? null : create(address, capacity);}
    //Stack Allocation
    public static Matrix4f mallocStack() {return mallocStack(stackGet());}
    public static Matrix4f callocStack() {return callocStack(stackGet());}
    public static Matrix4f mallocStack(MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, SIZEOF));}
    public static Matrix4f callocStack(MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, 1, SIZEOF));}
    //Stack Buffer Allocation
    public static Matrix4f.Buffer mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static Matrix4f.Buffer callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static Matrix4f.Buffer mallocStack(int capacity, MemoryStack stack) {return create(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static Matrix4f.Buffer callocStack(int capacity, MemoryStack stack) {return create(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}
    
    //Unsafe memory retrieval
    public static float nm00(long struct) {return MemoryUtil.memGetFloat(struct + M00);} public static float nm01(long struct) {return MemoryUtil.memGetFloat(struct + M01);} 
	public static float nm02(long struct) {return MemoryUtil.memGetFloat(struct + M02);} public static float nm03(long struct) {return MemoryUtil.memGetFloat(struct + M03);}
	public static float nm10(long struct) {return MemoryUtil.memGetFloat(struct + M10);} public static float nm11(long struct) {return MemoryUtil.memGetFloat(struct + M11);} 
	public static float nm12(long struct) {return MemoryUtil.memGetFloat(struct + M12);} public static float nm13(long struct) {return MemoryUtil.memGetFloat(struct + M13);}
	public static float nm20(long struct) {return MemoryUtil.memGetFloat(struct + M20);} public static float nm21(long struct) {return MemoryUtil.memGetFloat(struct + M21);} 
	public static float nm22(long struct) {return MemoryUtil.memGetFloat(struct + M22);} public static float nm23(long struct) {return MemoryUtil.memGetFloat(struct + M23);}
	public static float nm30(long struct) {return MemoryUtil.memGetFloat(struct + M30);} public static float nm31(long struct) {return MemoryUtil.memGetFloat(struct + M31);} 
	public static float nm32(long struct) {return MemoryUtil.memGetFloat(struct + M32);} public static float nm33(long struct) {return MemoryUtil.memGetFloat(struct + M33);}
	//Unsafe memory assignment
	public static void nm00(long struct, float value) {MemoryUtil.memPutFloat(struct + M00, value);} public static void nm01(long struct, float value) {MemoryUtil.memPutFloat(struct + M01, value);} 
	public static void nm02(long struct, float value) {MemoryUtil.memPutFloat(struct + M02, value);} public static void nm03(long struct, float value) {MemoryUtil.memPutFloat(struct + M03, value);}
	public static void nm10(long struct, float value) {MemoryUtil.memPutFloat(struct + M10, value);} public static void nm11(long struct, float value) {MemoryUtil.memPutFloat(struct + M11, value);} 
	public static void nm12(long struct, float value) {MemoryUtil.memPutFloat(struct + M12, value);} public static void nm13(long struct, float value) {MemoryUtil.memPutFloat(struct + M13, value);}
	public static void nm20(long struct, float value) {MemoryUtil.memPutFloat(struct + M20, value);} public static void nm21(long struct, float value) {MemoryUtil.memPutFloat(struct + M21, value);} 
	public static void nm22(long struct, float value) {MemoryUtil.memPutFloat(struct + M22, value);} public static void nm23(long struct, float value) {MemoryUtil.memPutFloat(struct + M23, value);}
	public static void nm30(long struct, float value) {MemoryUtil.memPutFloat(struct + M30, value);} public static void nm31(long struct, float value) {MemoryUtil.memPutFloat(struct + M31, value);} 
	public static void nm32(long struct, float value) {MemoryUtil.memPutFloat(struct + M32, value);} public static void nm33(long struct, float value) {MemoryUtil.memPutFloat(struct + M33, value);}

    public static class Buffer extends StructBuffer<Matrix4f, Buffer> implements NativeResource {
    	private static final Matrix4f ELEMENT_FACTORY = Matrix4f.create(-1L);
    	
        public Buffer(ByteBuffer container) {super(container, container.remaining() / SIZEOF);}
        public Buffer(long address, int cap) {super(address, null, -1, 0, cap, cap);}
        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {super(address, container, mark, pos, lim, cap);}
        @Override
        protected Buffer self() {return this;}
        @Override
        public int sizeof() {return SIZEOF;}
		@Override
		protected Matrix4f getElementFactory() {return ELEMENT_FACTORY;}

    }
	
}
