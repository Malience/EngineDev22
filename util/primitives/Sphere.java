package primitives;

import math.Vector3f;

public class Sphere extends Primitive {
	public Vector3f pos;
	public float r;
	
	public Sphere(float x, float y, float z, float radius) {this(new Vector3f(x, y, z), radius);}
	public Sphere(Vector3f pos, float radius) {super(Primitive.SPHERE); this.pos = pos; this.r = radius;}
	
}
