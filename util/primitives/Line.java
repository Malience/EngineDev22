package primitives;

import math.Vector3f;

public class Line extends Primitive {
	public Vector3f p0, p1;
	
	public Line(Vector3f p0, Vector3f p1) {super(Primitive.LINE); this.p0 = p0; this.p1 = p1;}

}
