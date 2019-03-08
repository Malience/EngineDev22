package primitives;

import math.Vector3f;

public class OBB extends Primitive {
	public Vector3f pos, xaxis, yaxis;
	public float hx, hy, hz;
	
	public OBB() {super(Primitive.OBB); }
	public OBB(float hx, float hy, float hz) {
		this();
		this.hx = hx; this.hy = hy; this.hz = hz;
		pos = new Vector3f();
		xaxis = new Vector3f(1, 0, 0);
		yaxis = new Vector3f(0, 1, 0);
	}
}
