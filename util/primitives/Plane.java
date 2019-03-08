package primitives;

import math.Vector3f;

public class Plane extends Primitive {
	public Vector3f normal;
	public float  d;
	
	public Plane(Vector3f v0, Vector3f v1, Vector3f v2){this(v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z());}
	
	public Plane(float v0x, float v0y, float v0z, float v1x, float v1y, float v1z, float v2x, float v2y, float v2z){
		super(Primitive.PLANE); 
		float v01x = v0x - v1x, v01y = v0y - v1y, v01z = v0z - v1z;
		float v02x = v0x - v2x, v02y = v0y - v2y, v02z = v0z - v2z;
		float a = v01y * v02z - v01z * v02y, b = v01z * v02x - v01x * v02z, c = v01x * v02y - v01y * v02x;
		float length = 1f/(float)Math.sqrt(a * a + b * b + c * c);
		a *= length; b *= length; c *= length;
		normal = new Vector3f(a, b, c);
		d = -a * v0x - b * v0y - c * v0z;
	}
	
	public Plane(Vector3f normal, float offset){
		super(Primitive.PLANE); 
		this.normal = new Vector3f(normal);
		d = offset;
	}

	public Plane(float a, float b, float c, float d) {super(Primitive.PLANE); normal = new Vector3f(a, b, c); this.d = d; normalize();}
	
	public void normalize() {normal.normalize();}
	
	public String toString() {return "Normal: " + normal + " Dist: " + d;}
}
