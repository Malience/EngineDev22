package primitives;

import math.Vector3f;

public class Slab extends Primitive {
	public Vector3f normal;
	public float near, far;
	
	public Slab() {super(Primitive.SLAB); }
}
