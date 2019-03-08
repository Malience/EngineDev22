package primitives;

import math.Vector3f;

public class Ray extends Primitive {
	public Vector3f p0, p1;
	
	public Ray(Vector3f p0, Vector3f p1) {super(Primitive.RAY); this.p0 = p0; this.p1 = p1;}

}
