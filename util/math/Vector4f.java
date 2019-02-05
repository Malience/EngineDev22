package math;

public interface Vector4f {
	
	public float x(); public float y(); public float z(); float w();
	public void x(float value); public void y(float value); public void z(float value); void w(float value);
	
	public default Vector4f xyzw(Vector4f r) {return xyzw(r.x(), r.y(), r.z(), r.w());}
	public default Vector4f xyzw(float x, float y, float z, float w) {
		x(x); y(y); z(z); w(w); 
		return this;
	}
}