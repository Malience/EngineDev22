package primitives;

import math.Vector3f;

public class Capsule extends Primitive {
	public Vector3f a, b;
	public float r;
	public Capsule(Vector3f a, Vector3f b, float r) {
		super(Primitive.CAPSULE); 
		this.a = a;
		this.b = b;
		this.r = r;
	}
	
	
}
