package primitives;

import math.Vector3f;

public class Triangle extends Primitive {
	public Vector3f p0, p1, p2;

	public Triangle(Vector3f p0, Vector3f p1, Vector3f p2) {super(Primitive.TRIANGLE); this.p0 = p0;this.p1 = p1;this.p2 = p2;}
	
	

}
