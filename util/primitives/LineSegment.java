package primitives;

import math.Vector3f;

public class LineSegment extends Primitive {
	public Vector3f a, b;
	
	public LineSegment(Vector3f a, Vector3f b) {super(Primitive.LINE_SEGMENT); this.a = a; this.b = b;}

}
